(ns lib-onyx.plugins.core-async-test
  (:require [lib-onyx.plugins.core-async :refer :all]
            [clojure.test :refer [is deftest testing]]
            [lib-onyx.job.utils :refer [unit-lens]]
            [traversy.lens :refer :all :rename {update lupdate}])
  (:import (java.util UUID)))

(def sample-job
  {:catalog
   [{:onyx/name          :read-segments
     :onyx/plugin        :onyx.plugin.core-async/input
     :onyx/type          :input
     :onyx/medium        :core.async
     :onyx/max-peers     1
     :onyx/doc           "Reads segments from a core.async channel"}

    {:onyx/name          :write-segments
     :onyx/plugin        :onyx.plugin.core-async/output
     :onyx/type          :output
     :onyx/medium        :core.async
     :onyx/max-peers     1
     :onyx/doc           "Writes segments to a core.async channel"}]
   :lifecycles [{:dont :touch}]})

(deftest add-core-async-input-test
  (let [instr-job (-> (add-core-async-input sample-job :read-segments)
                      (add-core-async-output :write-segments))]
    (testing "That we can add core.async references to read-segments, and get back a channel"
      (let [uuid (first (view instr-job
                              (*> (unit-lens [:lifecycles] :lifecycle/task :read-segments)
                                  (in [:core.async/id])
                                  maybe)))]
        (is (instance? UUID uuid))
        (is (get-channel uuid))))
    (testing "that we can also retrieve a map of different channels"
      (let [{:keys [read-segments write-segments]} (get-core-async-channels instr-job)]
        (is (not (= read-segments write-segments)))
        (is (not (nil? read-segments)))
        (is (not (nil? write-segments)))))))
