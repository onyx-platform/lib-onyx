(ns lib-onyx.plugins.kafka
  (:require [lib-onyx.job.utils :refer [add-to-job instrument-plugin-lifecycles
                                        find-task update-task unit-lens catalog-entrys-by-name]]
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

(defn add-kafka-input
  "Instrument a job with Kafka lifecycles and catalog entries.
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
  :kafka/deserializer-fn     - :json or :edn for default deserializers, a
                                custom fn can also be supplied. Only for
                                :input tasks

  ========================== Optional Settings =================================
  :kafka/chan-capacity
  :kafka/fetch-size
  :kafka/empty-read-back-off
  :kafka/commit-interval
  :kafka/request-size"
  ([job task] (add-kafka-input job task nil))
  ([job task opts]
   (if-let [entry (-> job (view-single (catalog-entrys-by-name task)))]
     (-> job
         (update-in [:lifecycles] conj {:lifecycle/task task
                                        :lifecycle/calls :onyx.plugin.kafka/read-messages-calls})
         (lupdate (catalog-entrys-by-name task) (comp (partial merge opts)
                                                      expand-deserializer-fn)))
     (throw (java.lang.IllegalArgumentException)))))

(defn add-kafka-output
  "Instrument a job with Kafka lifecycles and catalog entries.
  opts are of the following form for Kafka consumers AND producers

  :kafka/topic               - Name of a topic
  :kafka/zookeeper           - The ZooKeeper connection string
  :kafka/serializer-fn       - :json or :edn for default serializers, a
                                custom fn can also be supplied. Only for
                                :output tasks

  ========================== Optional Settings =================================
  :kafka/request-size"
  ([job task] (add-kafka-output job task nil))
  ([job task opts]
   (if-let [entry (-> job (view-single (catalog-entrys-by-name task)))]
     (-> job
         (update-in [:lifecycles] conj {:lifecycle/task task
                                        :lifecycle/calls :onyx.plugin.kafka/write-messages-calls})
         (lupdate (catalog-entrys-by-name task) (comp (partial merge opts)
                                                      expand-serializer-fn)))
     (throw (java.lang.IllegalArgumentException)))))
