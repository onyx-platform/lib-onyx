(ns lib-onyx.joplin-test
  (:require [clojure.test :refer [deftest is testing]]
            [lib-onyx.joplin :as sut]
            [onyx
             [job :as oj]
             [schema :as os]]
            [schema.core :as s]))

(def base-job {:workflow [[:in :inc]
                          [:inc :out]]
               :catalog []
               :lifecycles []
               :windows []
               :triggers []
               :flow-conditions []
               :task-scheduler :onyx.task-scheduler/balanced})

(def sample-joplin-config
  {:databases {:sql {:type :sql
                     :url "jdbc:mysql://192.168.99.100:3306/onyx?user=admin&password=mypass"
                     :migrations-table "ragtime_migrations"}}
   :migrators {:sql-migrator "resources/migrators/sql"}
   :environments {:dev [{:db {:type :sql
                              :url "jdbc:mysql://192.168.99.100:3306/onyx?user=admin&password=mypass"
                              :migrations-table "ragtime_migrations"}
                         :migrator "resources/migrators/sql"}]}})

(defn sample-task
  [task-name task-opts]
  {:task {:task-map (merge {:onyx/name task-name
                            :onyx/type :function
                            :onyx/fn :clojure.core/identity}
                           task-opts)}
   :schema {:task-map {:specialkey/mystring s/Str
                       (os/restricted-ns :specialkey) s/Any}}})

(deftest joplin-config-validation
  (is (s/validate sut/JoplinConfigSchema
                  sample-joplin-config)))

(deftest with-joplin-migrations-lifecycles-injection
  (is (= (:lifecycles (oj/add-task base-job
                                   (sample-task :in {:onyx/batch-size 10
                                                     :onyx/batch-timeout 10
                                                     :specialkey/mystring "hi"})
                                   (sut/with-joplin-migrations :dev sample-joplin-config)))
         [{:lifecycle/task :in
           :lifecycle/calls :lib-onyx.joplin/joplin-migrations}])))

(deftest end-to-end-validations
  (is (oj/add-task base-job
                   (sample-task :in {:onyx/batch-size 10
                                     :specialkey/mystring "special string"})
                   (sut/with-joplin-migrations :dev sample-joplin-config)))
  (is (thrown? Exception (oj/add-task base-job
                                      (sample-task :in {:onyx/batch-size 10
                                                        :joplin/illegal-arg 100
                                                        :specialkey/mystring "special string"})
                                      (sut/with-joplin-migrations :dev sample-joplin-config))))
  (is (thrown? Exception (oj/add-task base-job
                                      (sample-task :in {:onyx/batch-size 10
                                                        :specialkey/mystring "special"
                                                        :specialkey/also-illegal 10})
                                      (sut/with-joplin-migrations :dev sample-joplin-config))))
  (is (thrown? Exception (oj/add-task base-job
                                      (sample-task :in {:onyx/batch-size 10
                                                        :specialkey/mystring "special"})
                                      (sut/with-joplin-migrations :dev {}))))
  (testing "We can also just assoc the config directly into the task map"
    (is (oj/add-task base-job
                     (sample-task :in (merge {:onyx/batch-size 10
                                              :specialkey/mystring "special string"}
                                             {:joplin/config sample-joplin-config}))
                     (sut/with-joplin-migrations :dev))))
  (testing "We can elide the environment also"
    (is (oj/add-task base-job
                     (sample-task :in (merge {:onyx/batch-size 10
                                              :specialkey/mystring "special string"}
                                             {:joplin/config sample-joplin-config
                                              :joplin/environment :dev}))
                     (sut/with-joplin-migrations)))))
