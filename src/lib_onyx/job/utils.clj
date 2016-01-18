(ns lib-onyx.job.utils
  (:require [traversy.lens :refer :all :rename {update lupdate}]))

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

(defn instrument-plugin-lifecycles
  "Finds input and output plugin catalog entries, and creates lifecycles
  corresponding to those entries"
  [{:keys [catalog lifecycles] :as job}
   input-plugin-key output-plugin-key
   input-lifecycles output-lifecycles]
  (let [inputs (find-task-by-key
                catalog :onyx/plugin input-plugin-key)
        outputs (find-task-by-key
                 catalog :onyx/plugin output-plugin-key)]
    (add-to-job job
                {:lifecycles
                 (mapcat #(remove nil? %)
                         [(when-let [input-task-name (get inputs :onyx/name)]
                            (mapv (partial merge {:lifecycle/task input-task-name})
                                  input-lifecycles))
                          (when-let [output-task-name (get outputs :onyx/name)]
                            (mapv (partial merge {:lifecycle/task output-task-name})
                                  output-lifecycles))])})))

(defn update-task
  "Merges a map of opts into a catalog task"
  [job task-name f]
  (update-in job [:catalog]
             (fn [catalog]
               (let [task (find-task catalog task-name)]
                 (replace
                  {task (f task)} catalog)))))

(defn module-lens
  "Constructs a lens designed for the Onyx job map layout to aid extracting
   a single entry.
   {:catalog [{...} {...}]
    :lifecycles [{...} {...}]
    ...}
   Where section is one of [:catalog :lifecycles :workflows :flow-conditions :windows :triggers]
   Where key and val are used to match a single entry"
  [section key val]
  (*>
   (combine (in section) each)
   (conditionally (fn [foci] (= val (get foci key))))))
