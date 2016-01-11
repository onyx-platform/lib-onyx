(ns lib-onyx.plugins.sql
  (:require [lib-onyx.job.utils :refer [add-to-job instrument-plugin-lifecycles
                                        find-task-by-key update-task]]
            [ragtime.jdbc :as jdbc]
            [clojure.set :refer [rename-keys]]
            [ragtime.repl :refer [migrate]]))

(comment
  "1. We need to be able to inject lifecycles for in and out tasks, as well as
      provide a similiar mechanism as the kafka plugin when it comes to furnishing
      catalog entries with configuration options

   2. Some of these configuration options will include target schema, we can
      key off of this in order to verify that the target table (in read or write mode)
      infact does have the proper schema, if it does not then we can throw an exception.

   3. ")

(defn extract-db-spec
  "Extracts a db spec from a sql plugin catalog entry"
  [catalog-entry]
  (rename-keys catalog-entry {:sql/classname   :classname
                              :sql/subprotocol :subprotocol
                              :sql/subname     :subname
                              :sql/user        :user
                              :sql/password    :password
                              :sq/migrations   :migrations}))

(defn add-sql-lifecycles
  "Instruments a jobs lifecycles with sql plugin reader and writer calls"
  [job]
  (instrument-plugin-lifecycles job
                                :onyx.plugin.sql/read-rows
                                :onyx.plugin.sql/write-rows
                                [{:lifecycle/calls :onyx.plugin.sql/read-rows-calls}]
                                [{:lifecycle/calls :onyx.plugin.sql/write-rows-calls}]))

(defn ensure-migration [event lifecycle]
  (let [{:keys [migrations classname subprotocol subname
                user password]} lifecycle
        config {:datastore (jdbc/sql-database {:subprotocol subprotocol
                                               :subname subname
                                               :user user
                                               :password password
                                               :classname classname})
                :migrations (jdbc/load-resources migrations)}]
    (migrate config)))

(def sql-migration-calls
  {:lifecycle/before-task-start ensure-migration})

(defn add-sql-migration-lifecycles
  [{:keys [catalog lifecycles] :as job}]
  (let [input  (find-task-by-key
                catalog :onyx/plugin :onyx.plugin.sql/read-rows)
        output (find-task-by-key
                catalog :onyx/plugin :onyx.plugin.sql/write-rows)
        modfn       (fn [job catalog-entry]
                      (if (:sql/migrations catalog-entry)
                        (add-to-job
                         job {:lifecycles [(merge (extract-db-spec catalog-entry) ;; Here you can configure
                                                  {:lifecycle/task (:onyx/name catalog-entry)
                                                   :lifecycle/calls ::sql-migration-calls})]})
                        job))]
    (-> job
        (modfn input)
        (modfn output))))



(defn add-sql-migrations
  "Instruments a jobs lifecycles when there are migrations present for a DB's
   catalog entry"
  [job]
  )

(comment
  (defn add-sql
    [{:keys [catalog lifecycles] :as job}]
    (let [sql-input (u/find-task-by-key catalog :onyx/plugin
                                        :onyx.plugin.sql/read-rows)
          sql-output (u/find-task-by-key catalog :onyx/plugin
                                         :onyx.plugin.sql/write-rows)]
      (-> job
          (u/add-to-job
           {:lifecycles
            (mapcat #(remove nil? %)
                    [(when-let [task-name (get sql-output :onyx/name)]
                       [{:lifecycle/task task-name
                         :lifecycle/calls :onyx.plugin.sql/write-rows-calls}])])})))))
