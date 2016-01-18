(ns lib-onyx.plugins.sql-test
  (:require [lib-onyx.plugins.sql :refer :all]
            [clojure.test :refer [is deftest testing]]))

(def sample-job
  {:catalog
   [{:onyx/name :read-rows
     :onyx/plugin :onyx.plugin.sql/read-rows
     :sql/classname   "classname"
     :sql/subprotocol "subprotocol"
     :sql/subname     "subname"
     :sql/user        "user"
     :sql/password    "password"
     :sql/migrations   "migrations"}

    {:onyx/name :write-rows
     :onyx/plugin :onyx.plugin.sql/write-rows
     :sql/classname   "classname"
     :sql/subprotocol "subprotocol"
     :sql/subname     "subname"
     :sql/user        "user"
     :sql/password    "password"
     :sql/migrations   "migrations"}]
   })
