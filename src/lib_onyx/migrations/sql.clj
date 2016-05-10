(ns lib-onyx.migrations.sql
  (:require [joplin.core :as joplin]
            [taoensso.timbre :refer [info warn]]))

(defn get-lifecycle-config
  "Tries to get a configuration key in the lifecycle or event map"
  [lifecycle event k]
  (or (get-in lifecycle [k])
      (get-in event [:onyx.core/task-map k])
      (throw (Exception. (str k " not specified")))))

(defn no-pending-migrations?
  [event lifecycle]
  (let [joplin-config (get-lifecycle-config lifecycle event :joplin/config)
        joplin-db-env (get-lifecycle-config lifecycle event :joplin/environment)]
    (mapv (fn [env]
            (joplin/migrate-db env))
          (get-in joplin-config [:environments joplin-db-env]))
    (if (every? nil? (mapv (fn [env]
                             (joplin/pending-migrations env))
                           (get-in joplin-config [:environments joplin-db-env])))
      (do (info "Migrations successful")
          true)
      (do (warn "Migrations unsuccessful, retrying")
          false))))

(def joplin
  {:lifecycle/start-task? no-pending-migrations?})

(defn with-joplin-migrations [joplin-environment joplin-config]
  (fn [task-definition]
    (let [task-name (get-in task-definition [:task :task-map :onyx/name])]
      (-> task-definition
          (assoc-in [:task :task-map :joplin/config] joplin-config)
          (assoc-in [:task :task-map :joplin/environment] joplin-environment)
          (update-in [:task :lifecycles] conj {:lifecycle/task task-name
                                               :lifecycle/calls ::joplin})))))
