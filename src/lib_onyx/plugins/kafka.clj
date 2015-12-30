(ns lib-onyx.plugins.kafka
  (:require [lib-onyx.job.utils :refer [add-to-job instrument-plugin-lifecycles
                                        find-task update-task]]
            [cheshire.core :as json]))

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


(defn add-kafka-lifecycles
  "Instruments a jobs lifecycles with kafka plugin reader and writer calls"
  [job]
  (instrument-plugin-lifecycles job
                                :onyx.plugin.kafka/read-messages
                                :onyx.plugin.kafka/write-messages
                                [{:lifecycle/calls :onyx.plugin.kafka/read-messages-calls}]
                                [{:lifecycle/calls :onyx.plugin.kafka/write-messages-calls}]))

(defn get-serializer-fn [task]
  (update-in task [:kafka/serializer-fn]
             (fn [v]
               (condp = v
                 :json    ::serialize-message-json
                 :edn     ::serialize-message-edn
                 nil      (throw (java.lang.IllegalArgumentException.
                                  ":kafka/serializer-fn not supplied"))
                 v))))

(defn get-deserializer-fn [task]
  (update-in task [:kafka/deserializer-fn]
             (fn [v]
               (condp = v
                 :json    ::deserialize-message-json
                 :edn     ::deserialize-message-edn
                 nil      (throw (java.lang.IllegalArgumentException.
                                  ":kafka/deserializer-fn not supplied"))
                 v))))

(defn add-kafka-to-task
  "Instrument a jobs catalog entry with Kafka options
  opts are of the following form for Kafka consumers AND producers

  :kafka/topic               - Name of a topic
  :kafka/partition           -- Optional: partition to read from if
                                 auto-assignment is not used
  :kafka/group-id            -- The consumer identity to store in ZooKeeper
  :kafka/zookeeper           - The ZooKeeper connection string
  :kafka/offset-reset        -- Offset bound to seek to when not found
                                 - :smallest or :largest
  :kafka/force-reset?        -- Force to read from the beginning or end of the
                                 log, as specified by :kafka/offset-reset.
                                 If false, reads from the last acknowledged
                                 messsage if it exists
  :kafka/serializer-fn       - :json or :edn for default serializers, a
                                custom fn can also be supplied
  :kafka/deserializer-fn     -- :json or :edn for default deserializers, a
                                custom fn can also be supplied

  ========================== Optional Settings =================================
  :kafka/chan-capacity
  :kafka/fetch-size
  :kafka/empty-read-back-off
  :kafka/commit-interval
  :kafka/request-size        -
  "
  [job task-name opts]
  (let [kafka-task (find-task (:catalog job) task-name)
        task-type  (:onyx/type kafka-task)]
    (-> job
        (update-task task-name (fn [m] (merge m opts)))
        (update-task task-name get-serializer-fn))))
