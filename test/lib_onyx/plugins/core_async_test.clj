(ns lib-onyx.plugins.core-async-test
  (:require [lib-onyx.plugins.core-async :refer :all]
            [clojure.test :refer [is deftest testing]])
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


(deftest add-core-async-test
  (let [instr-job (add-core-async sample-job)]
    (testing "that we can add core.async references to read-segments and write-segments"
      (is (every? true? (reduce (fn [acc lc]
                                  (if (and (or (= (:lifecycle/task lc) :read-segments)
                                               (= (:lifecycle/task lc) :write-segments))
                                           (instance? UUID (:core.async/id lc)))
                                    (conj acc true)
                                    acc)) [] (:lifecycles instr-job)))))
    (testing "that we can resolve them to distinct channels"
      (is (every? nil? (map clojure.core.async/close!
                        (vals (get-core-async-channels instr-job))))))))