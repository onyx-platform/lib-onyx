(ns lib-onyx.normalize)

(defn task-name
  "Creates the :task/by-id entries in a catalog"
  [job]
  (let [catalog (get job :catalog)
        tasks-by-name (reduce (fn [acc item]
                                (assoc acc (:onyx/name item) item)) {} (:catalog job))]
    (assoc job :task/by-name tasks-by-name)))

(def job
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
