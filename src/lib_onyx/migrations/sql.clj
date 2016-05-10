(ns lib-onyx.migrations.sql
  (:require [joplin.core :as joplin]
            [schema.core :as s]
            [onyx.schema :as os]
            [taoensso.timbre :refer [debug info warn]]))

(defn get-task-map-lifecycle-config
  "Tries to get a piece of data in the task map or lifecycle"
  [lifecycle event k]
  (or (get-in event [:onyx.core/task-map k])
      (get-in lifecycle [k])
      (throw (Exception. (str k " not specified")))))

(defn no-pending-migrations?
  [event lifecycle]
  (let [joplin-config (get-task-map-lifecycle-config lifecycle event :joplin/config)
        joplin-db-env (get-task-map-lifecycle-config lifecycle event :joplin/environment)]
    (debug "Using joplin environment: " (get-in joplin-config [:environments joplin-db-env]))
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

(def joplin-migrations
  {:lifecycle/start-task? no-pending-migrations?})

                                        ;((comp #{:f} :type) {:type})
(def supported-plugins
  (s/enum :jdbc :sql :es :zk :dt :cass))

(def JoplinDatabaseDescriptor
  (s/conditional
   (comp #{:sql}    :type) {:type supported-plugins
                            :url s/Str
                            (s/optional-key :migrations-table) s/Str}
   (comp #{:jdbc}   :type) {:type supported-plugins
                            :url s/Str
                            (s/optional-key :migrations-table) s/Str}
   (comp #{:es}     :type) {:type supported-plugins
                            :host s/Str :port s/Num
                            (s/optional-key :migrations-index) s/Str}
   (comp #{:zk}     :type) {:type supported-plugins
                            :host s/Str
                            :port s/Num
                            :client (s/enum :curator :exhibitor)}
   (comp #{:dt}     :type) {:type supported-plugins
                            :host s/Str}
   (comp #{:cass}   :type) {:type supported-plugins
                            :hosts s/Str
                            :keyspace s/Str}))

(def JoplinConfigSchema
  {(s/optional-key :databases) {s/Any JoplinDatabaseDescriptor}
   (s/optional-key :migrators) {s/Any s/Str}
   (s/optional-key :seeds) s/Any
   :environments {s/Keyword [{:db JoplinDatabaseDescriptor
                              :migrator s/Str}]}})

(defn with-joplin-migrations
  "This task bundle modifier will continously try to apply data migrations
  before allowing the task to start.

  - joplin-config structure can be discovered from the schema and is specific
  to your database(s). It is generally of this structure

  {:environments {:dev [{:db {:type :sql
                              :url 'jdbc-url'}
                         :migrator 'resources/migrators/sql'}]}}

  - joplin-environment is a selector for a specific config.
  The only valid selector for the above example would be ':dev'.

  The datastructure is expected in this form to make interop with the joplin
  command line tools simple, since they share an expected shape. More can be
  read about Joplin here https://github.com/juxt/joplin.

  If you are providing this config data another way, either manually or through
  a seperate mechanism, you may elide either-or the joplin-config or
  joplin-environment.

  This does not apply data seeders."
  ([] (with-joplin-migrations nil nil))
  ([joplin-environment] (with-joplin-migrations joplin-environment nil))
  ([joplin-environment joplin-config]
   (fn [task-definition]
     (let [task-name (get-in task-definition [:task :task-map :onyx/name])]
       (cond-> task-definition
         joplin-config (assoc-in [:task :task-map :joplin/config] joplin-config)
         joplin-environment (assoc-in [:task :task-map :joplin/environment] joplin-environment)
         true (->
               (update-in [:task :lifecycles] conj {:lifecycle/task task-name
                                                    :lifecycle/calls ::joplin-migrations})
               (assoc-in [:schema :task-map :joplin/config] JoplinConfigSchema)
               (assoc-in [:schema :task-map :joplin/environment] s/Keyword)
               (assoc-in [:schema :task-map (os/restricted-ns :joplin)] s/Any)))))))
