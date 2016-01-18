(ns lib-onyx.plugins.kafka-test
  (:require [lib-onyx.plugins.kafka :refer :all]
            [clojure.test :refer [is deftest testing]]
            [lib-onyx.job.utils :refer [catalog-entrys-by-name]]
            [traversy.lens :refer :all :rename {update lupdate}]))

(def sample-job
  {:catalog
   [{:onyx/name          :read-segments
     :onyx/plugin        :onyx.plugin.kafka/read-messages
     :onyx/type          :input}

    {:onyx/name          :write-segments
     :onyx/plugin        :onyx.plugin.kafka/write-messages
     :onyx/type          :output}]
   :lifecycles [{:dont :touch}]})

(deftest add-kafka-to-input-and-output-test
  (let [instr-job (-> sample-job
                      (add-kafka-input :read-segments)
                      (add-kafka-output :write-segments {:kafka/topic "meetup"}))]
    (testing "that we can add the necessary lifecycles to a job for kafka"
      (is (some #{{:lifecycle/task :read-segments, :lifecycle/calls :onyx.plugin.kafka/read-messages-calls}
                  {:lifecycle/task :write-segments, :lifecycle/calls :onyx.plugin.kafka/write-messages-calls}}
                (:lifecycles instr-job))))
    (testing "that we didnt clobber any other lifecycles in the workflow"
      (is (some #{{:dont :touch}} (:lifecycles instr-job))))
    (testing "that we can also get opts merged in"
      (is (= "meetup"
           (:kafka/topic (-> instr-job (view-single (catalog-entrys-by-name :write-segments)))))))))

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

;(add-kafka-input sample-job :read-segments)
