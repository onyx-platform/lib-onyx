(ns lib-onyx.plugins.kafka-test
  (:require [lib-onyx.plugins.kafka :refer :all]
            [clojure.test :refer [is deftest testing]]))

(def sample-job
  {:catalog
   [{:onyx/name          :read-segments
     :onyx/plugin        :onyx.plugin.kafka/read-messages
     :onyx/type          :input}

    {:onyx/name          :write-segments
     :onyx/plugin        :onyx.plugin.kafka/write-messages
     :onyx/type          :output}]
   :lifecycles [{:dont :touch}]})

(deftest add-kafka-lifecycles-test
  (let [instr-job (-> sample-job
                      (add-kafka-lifecycles :read-segments)
                      (add-kafka-lifecycles :write-segments))]
    (testing "that we can add the necessary lifecycles to a job for kafka"
      (is (some #{{:lifecycle/task :read-segments, :lifecycle/calls :onyx.plugin.kafka/read-messages-calls}
                  {:lifecycle/task :write-segments, :lifecycle/calls :onyx.plugin.kafka/write-messages-calls}}
                (:lifecycles instr-job))))
    (testing "that we didnt clobber any other lifecycles in the workflow"
      (is (some #{{:dont :touch}} (:lifecycles instr-job))))
    (testing "that we throw an exception when an :onyx/type is unspecified or invalid"
      (is (thrown? Exception
                   (add-kafka-lifecycles {:catalog [{:onyx/name :read-segments
                                                     :onyx/type :function}]}
                                         :read-segments)))
      (is (thrown? java.lang.AssertionError
                   (add-kafka-lifecycles {:catalog [{:onyx/name :read-segments}]}
                                         :read-segments))))))

(deftest serialization-fns-test
  (testing "EDN and JSON types"
    (is (= {:kafka/serializer-fn :lib-onyx.plugins.kafka/serialize-message-json}
           (expand-serializer-fn {:kafka/serializer-fn :json})))
    (is (= {:kafka/serializer-fn :lib-onyx.plugins.kafka/serialize-message-edn}
           (expand-serializer-fn {:kafka/serializer-fn :edn})))
    (is (= {:kafka/deserializer-fn :lib-onyx.plugins.kafka/deserialize-message-json}
           (expand-deserializer-fn {:kafka/deserializer-fn :json})))
    (is (= {:kafka/deserializer-fn :lib-onyx.plugins.kafka/deserialize-message-edn}
           (expand-deserializer-fn {:kafka/deserializer-fn :edn})))))

(deftest add-kafka-to-task-test
  (testing "that we can add kafka ops to a task"
    (= (first (:catalog (add-kafka-catalog sample-job :read-segments {:kafka/serializer-fn :json
                                                                      :kafka/topic "mytopic"})))
       {:onyx/name :read-segments
        :onyx/plugin :onyx.plugin.kafka/read-messages
        :onyx/type :input
        :kafka/serializer-fn :json
        :kafka/topic "mytopic"})))
