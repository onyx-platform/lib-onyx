(ns lib-onyx.plugins.logging
  (:require [taoensso.timbre :refer [info]]))


(defn log-batch [event lifecycle]
  (let [task-name (:onyx/name (:onyx.core/task-map event))]
    (doseq [m (map :message (mapcat :leaves (:tree (:onyx.core/results event))))]
      (info task-name " logging segment: " m)))
  {})

(def log-calls
  {:lifecycle/after-batch log-batch})

(defn add-logging
  "Add's logging output to a tasks output-batch. "
  [job task]
  (assert (and (keyword? task) (u/find-task (:catalog job) task)))
  (u/add-to-job job {:lifecycles [{:lifecycle/task task
                                   :lifecycle/calls ::log-calls
                                   :lifecycle/doc "Lifecycle for printing the output of a task's batch"}]}))
