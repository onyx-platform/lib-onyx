(ns lib-onyx.replica-query
  (:require [onyx.log.commands.common :as common]))

(defn deref-replica [log-subscriber]
  (:replica @(:state log-subscriber)))

(defn jobs [replica]
  (:jobs replica))

(defn killed-jobs [replica]
  (:killed-jobs replica))

(defn peers [replica]
  (:peers replica))

(defn tasks [replica job-id]
  (get-in replica [:tasks job-id]))

(defn job-allocations [replica]
  (:allocations replica))

(defn task-allocations [replica job-id]
  ((job-allocations replica) job-id))

(defn peer-site [replica peer-id]
  (get-in replica [:peer-sites peer-id]))

(defn peer-state [replica peer-id]
  (get-in replica [:peer-state peer-id]))

(defn peer-allocation [replica peer-id]
  (let [rep replica]
    (common/peer->allocated-job (:allocations rep) peer-id)))

(defn job-scheduler [replica]
  (:job-scheduler replica))

(defn task-scheduler [replica job-id]
  (get-in replica [:task-schedulers job-id]))
