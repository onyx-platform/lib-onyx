(ns lib-onyx.normalize-test
  (:require [lib-onyx.normalize :refer :all]
            [clojure.test :refer [is deftest testing]]))

(def norm-job
  {:task/by-name {:read-lines {:onyx/name :read-lines
                               :onyx/plugin :onyx.plugin.sql/read-rows
                               :sql/uri "mysql://mydb/db"
                               :sql/migrations "migrations"
                               :lifecycles [[:lifecycles/by-name :add-logging]
                                            [:lifecycles/by-name :verify-schema]]}

                  :identity   {:onyx/name :identity
                               :onyx/fn :clojure.core/identity
                               :onyx/doc "Do nothing with the segment"
                               :flow-conditions []}

                  :write-lines {:onyx/name :write-lines
                                :onyx/plugin :onyx.plugin.sql/write-rows
                                :sql/uri "mysql://my-other-db/db"
                                :sql/migrations "migrations"
                                :lifecycles [[:lifecycles/by-name :verify-schema]]
                                :windows []}}

   :lifecycles/by-name {:add-logging {:lifecycle/name :add-logging
                                      :lifecycle/task [[:task/by-name :read-lines :onyx/name]]
                                      :lifecycle/calls :lib-onyx.plugins.logging/log-calls
                                      :lifecycle/doc "Add's logging to a task"}

                        :verify-schema {:lifecycle/task [[:task/by-name :read-lines :onyx/name]
                                                         [:task/by-name :write-lines :onyx/name]]
                                        :lifecycle/calls :lib-onyx.plugins.sql/verify-schema
                                        :lifecycle/doc "Applies sql migrations to a db"}}

   :workflow [[[:task/by-name :read-lines :onyx/name][:task/by-name :identity :onyx/name]]
              [[:task/by-name :identity :onyx/name]  [:task/by-name :write-lines :onyx/name]]]})

(def sample-job
  {:catalog [{:onyx/name :read-lines
              :onyx/plugin :onyx.plugin.sql/read-rows
              :sql/uri "mysql://mydb/db"
              :sql/migrations "migrations"}

             {:onyx/name :write-lines
              :onyx/plugin :onyx.plugin.sql/write-rows
              :sql/uri "mysql://my-other-db/db"
              :sql/migrations "migrations"}

             {:onyx/name :identity
              :onyx/fn :clojure.core/identity
              :onyx/doc "Do nothing with the segment"}]

   :lifecycles [{:lifecycle/task :read-lines
                 :sample-param 1
                 :lifecycle/calls :lib-onyx.plugins.logging/log-calls
                 :lifecycle/doc "Add's logging to a task"}

                {:lifecycle/task :read-lines
                 :lifecycle/calls :lib-onyx.plugins.sql/verify-schema
                 :lifecycle/doc "Applies sql migrations to a db"}

                {:lifecycle/task :write-lines
                 :lifecycle/calls :lib-onyx.plugins.sql/verify-schema
                 :lifecycle/doc "Applies sql migrations to a db"}]
   :workflow [[:read-lines :identity]
              [:identity :write-lines]]})

(deftest task-by-name-test
  (testing "That a normlized catalog can be accessed"
    (is (= "migrations"
           (get-in (task-by-name sample-job) [:task/by-name :read-lines :sql/migrations])))
    (is (= :read-lines
           (get-in (task-by-name sample-job) [:task/by-name :read-lines :onyx/name])))))

(deftest lifecycles-by-name-test
  (testing "That lifecycles can be normalized"
    (is (= 1
           (get-in (lifecycles-by-name sample-job)
                   [:lifecycle/by-name :lib-onyx.plugins.logging/log-calls :sample-param])))))
