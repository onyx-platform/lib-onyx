(ns lib-onyx.job.utils-test
  (:require [lib-onyx.job.utils :refer :all]
            [clojure.test :refer [is deftest testing]]))

(deftest find-task-by-key-test
  (testing "that we can find a taks by a certain key"
    (is (= (find-task-by-key [{:onyx/name :foo
                               :onyx/type :function}
                              {:onyx/name :bar
                               :onyx/type :plugin}
                              {:onyx/name :baz
                               :onyx/type :function}]
                             :onyx/name :foo)
           {:onyx/name :foo :onyx/type :function})))
  (testing "that we only return 1 catalog entry"
    (is (thrown? java.lang.AssertionError
                 (find-task-by-key [{:onyx/name :foobar
                                     :onyx/type :function}
                                    {:onyx/name :foobar
                                     :onyx/type :plugin}]
                                   :onyx/name :foobar)))))

(deftest find-task-test
  (testing "that given a task name, we can return that task"
    (is (= (find-task [{:onyx/name :foo
                        :onyx/type :function}
                       {:onyx/name :bar
                        :onyx/type :plugin}
                       {:onyx/name :baz
                        :onyx/type :function}]
                      :foo)
           {:onyx/name :foo :onyx/type :function})))
  (testing "that we assert there is only 1 task returned"
    (is (thrown? java.lang.AssertionError
                 (find-task [{:onyx/name :foobar
                              :onyx/type :function}
                             {:onyx/name :foobar
                              :onyx/type :plugin}]
                            :foobar)))))

(deftest add-to-job-test
  (let [sample-job
        {:catalog    [{:onyx/name   :in
                       :onyx/plugin :onyx.plugin.core-async/input
                       :onyx/type   :input}
                      {:onyx/name :inc
                       :onyx/fn   :flat-workflow.core/my-inc}
                      {:onyx/name   :out
                       :onyx/plugin :onyx.plugin.core-async/output}]
         :lifecycles [{:lifecycle/task  :in
                       :lifecycle/calls :flat-workflow.core/in-calls}]}]
    (testing "that adding a new entry increases the count"
      (is (= (count (:catalog (add-to-job sample-job
                                          {:catalog
                                           [{:onyx/name   :newtask
                                             :onyx/plugin :foo}]})))
             (inc (count (:catalog sample-job)))))
      (is (= (count (:lifecycles (add-to-job sample-job
                                             {:lifecycles
                                              [{:lifecycle/task  :in
                                                :lifecycle/calls :flat-workflow.core/in-calls}]})))
             (inc (count (:lifecycles sample-job))))))))

(deftest n-peers-test
  (testing "We can get proper min peer count"
    (is (= (n-peers [{:onyx/name      :in
                      :onyx/min-peers 10}
                     {:onyx/name      :ident
                      :onyx/min-peers 3}
                     {:onyx/name      :out
                      :onyx/min-peers 2}]
                    [[:in :ident]
                     [:ident :out]]) 15)))
  (testing "that we take into account when a catalog entry is not part of the task"
    (is (= (n-peers [{:onyx/name      :in
                      :onyx/min-peers 10}
                     {:onyx/name      :ident
                      :onyx/min-peers 3}
                     {:onyx/name      :out
                      :onyx/min-peers 2}]
                    [[:in :out]])
           12))))


(deftest instrument-plugin-lifecycles-test
  (testing "that we can identify catalog entries that require lifecycle's, and add them"
    (is (some #{{:lifecycle/task :out :bar :baz}
                {:lifecycle/task :in  :foo :baz}}
              (:lifecycles (instrument-plugin-lifecycles
                            {:catalog [{:onyx/name :in
                                        :onyx/plugin :input}
                                       {:onyx/name :out
                                        :onyx/plugin :output}]} :input :output
                            [{:foo :bar}]
                            [{:bar :baz}])))))
  (testing "That we can add only one type, input or output, to the lifecycle"
    (is (= '({:lifecycle/task :out :bar :baz})
           (:lifecycles (instrument-plugin-lifecycles
                         {:catalog [{:onyx/name :in
                                     :onyx/plugin :input}
                                    {:onyx/name :out
                                     :onyx/plugin :output}]}
                         nil :output
                         nil [{:bar :baz}]))))))

(deftest update-task-test
  (testing "that we can add {:hello :world} to a task"
    (is (:hello
         (first (:catalog
                 (update-task {:catalog [{:onyx/name :read-segments}]}
                              :read-segments (fn [m] (merge {:hello :world} m)))))))))
