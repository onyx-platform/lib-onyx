(ns lib-onyx.normalize)

(defn map-by-key [k xs]
  (reduce (fn [acc item]
            (assoc acc (k item) item)) {} xs))

(defn task-by-name
  "Creates the :task/by-id entries in a catalog"
  [job]
  (let [catalog (get job :catalog)
        tasks-by-name (map-by-key :onyx/name (:catalog job))]
    (assoc job :task/by-name tasks-by-name)))

(defn lifecycles-by-hash
  [job]
  (let [task-name-hash     (atom {})
        lifecycles         (get job :lifecycles)
        hashed-lifecycles  (mapv
                            (fn [m]
                              (let [generic-map (dissoc m :lifecycle/task)
                                    hashv       (hash generic-map)]
                                (swap! task-name-hash
                                       update hashv
                                       (fn [records]
                                         (vec (conj records (:lifecycle/task m)))))
                                (assoc m ::hash (hash m))))
                            lifecycles)
        lifecycles-by-hashes (map-by-key ::hash hashed-lifecycles)]
    (assoc job
           :lifecycle/by-hash lifecycles-by-hashes
           :lifecycle/task-name-by-hash @task-name-hash)))

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
