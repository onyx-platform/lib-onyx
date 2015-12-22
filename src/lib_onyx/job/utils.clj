(ns lib-onyx.job.utils)

(defn find-task-by-key
  "Finds the catalog entry where the key correspond to the value
  TODO: make this as robust as find-task"
  [catalog k value]
  (let [matches (filter #(= value (k %)) catalog)]
    (assert (< (count matches) 2) (str "Multiple catalog entries found for " k " = " value))
    (first matches)))

(defn find-task
  "Finds the catalog entry where the :onyx/name key equals task-name"
  [catalog task-name]
  (find-task-by-key catalog :onyx/name task-name))

(defn add-to-job
  "Adds to the catalog and lifecycles of a job in form
  {:workflow ...
   :catalog ...
   :lifecycles ...}"
  [job {:keys [catalog lifecycles]}]
  (-> job
      (update :catalog into catalog)
      (update :lifecycles into lifecycles)))

(defn n-peers
  "Takes a workflow and catalog, returns the minimum number of peers
   needed to execute this job."
  [catalog workflow]
  (let [task-set (into #{} (apply concat workflow))]
    (reduce
      (fn [sum t]
        (+ sum (or (:onyx/min-peers (find-task catalog t)) 1)))
      0 task-set)))