(ns lib-onyx.plugins.kafka
  (:require [lib-onyx.job.utils :refer [add-to-job instrument-plugin-lifecycles
                                        find-task update-task unit-lens]]
            [cheshire.core :as json]
            [traversy.lens :refer :all :rename {update lupdate}]))

(defn deserialize-message-json [bytes]
  (try
    (json/parse-string (String. bytes "UTF-8"))
    (catch Exception e
      {:error e})))

(defn deserialize-message-edn [bytes]
  (try
    (read-string (String. bytes "UTF-8"))
    (catch Exception e
      {:error e})))

(defn serialize-message-json [segment]
  (.getBytes (json/generate-string segment)))

(defn serialize-message-edn [segment]
  (.getBytes segment))

(defn expand-serializer-fn [task]
  (update-in task [:kafka/serializer-fn]
             (fn [v]
               (condp = v
                 :json    ::serialize-message-json
                 :edn     ::serialize-message-edn
                 v))))

(defn expand-deserializer-fn [task]
  (update-in task [:kafka/deserializer-fn]
             (fn [v]
               (condp = v
                 :json    ::deserialize-message-json
                 :edn     ::deserialize-message-edn
                 v))))

(defn add-kafka-lifecycles
  "Add's read-messages-calls lifecycles for a Kafka task. If the task is not specified
   as an :input task in the catalog, throws an exception."
  [job task]
  (if-let [entry (view-single job (*> (unit-lens [:catalog] :onyx/name task)
                                      (conditionally :onyx/type)))]
    (update-in job [:lifecycles] conj (condp = (:onyx/type entry)
                                        :input {:lifecycle/task task
                                                :lifecycle/calls :onyx.plugin.kafka/read-messages-calls}
                                        :output {:lifecycle/task task
                                                 :lifecycle/calls :onyx.plugin.kafka/write-messages-calls}))
    (throw (java.lang.IllegalArgumentException "Catalog entry must specify :onyx/type"))))

(defn add-kafka-catalog
  "Instrument a jobs catalog entry with Kafka options
  opts are of the following form for Kafka consumers AND producers

  :kafka/topic               - Name of a topic
  :kafka/partition           - Optional: partition to read from if
                                 auto-assignment is not used
  :kafka/group-id            - The consumer identity to store in ZooKeeper
  :kafka/zookeeper           - The ZooKeeper connection string
  :kafka/offset-reset        - Offset bound to seek to when not found
                                 - :smallest or :largest
  :kafka/force-reset?        - Force to read from the beginning or end of the
                                 log, as specified by :kafka/offset-reset.
                                 If false, reads from the last acknowledged
                                 messsage if it exists
  :kafka/serializer-fn       - :json or :edn for default serializers, a
                                custom fn can also be supplied. Only for
                                :output tasks
  :kafka/deserializer-fn     - :json or :edn for default deserializers, a
                                custom fn can also be supplied. Only for
                                :input tasks

  ========================== Optional Settings =================================
  :kafka/chan-capacity
  :kafka/fetch-size
  :kafka/empty-read-back-off
  :kafka/commit-interval
  :kafka/request-size
  "
  [job task opts] ;; TODO: Catch this assertion error
  (if-let [entry (view-single job (*> (unit-lens [:catalog] :onyx/name task)
                                      (conditionally (fn [foci]
                                                       (let [t (get foci :onyx/plugin)]
                                                         (or (= t :onyx.plugin.kafka/read-messages)
                                                             (= t :onyx.plugin.kafka/write-messages)))))))]
    (lupdate job (unit-lens [:catalog] :onyx/name task) (comp expand-deserializer-fn
                                                                expand-serializer-fn))
    (throw (java.lang.IllegalArgumentException "Catalog entry must specify a Kafka plugin for :onyx/plugin"))))
