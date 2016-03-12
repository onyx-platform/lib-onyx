(ns lib-onyx.replica-query
  (:require [onyx.log.commands.common :as common]
            [clojure.data]))

(defn deref-replica
  "Derefences the replica as an immutable value."
  [log-subscriber]
  (:replica @(:state log-subscriber)))

(defn jobs
  "Lists all non-killed, non-completed job ids."
  [replica]
  (:jobs replica))

(defn killed-jobs
  "Lists all the job ids that have been killed."
  [replica]
  (:killed-jobs replica))

(defn completed-jobs
  "Lists all the job ids that have been completed."
  [replica]
  (:completed-jobs replica))

(defn peers
  "Lists all the peer ids."
  [replica]
  (:peers replica))

(defn tasks
  "Given a job id, returns all the task ids for this job."
  [replica job-id]
  (get-in replica [:tasks job-id]))

(defn job-allocations
  "Returns a map of job id -> task id -> peer ids, denoting which peers are assigned to which tasks."
  [replica]
  (:allocations replica))

(defn task-allocations
  "Given a job id, returns a map of task id -> peer ids, 
  denoting which peers are assigned to which tasks for this job only."
  [replica job-id]
  ((job-allocations replica) job-id))

(defn peer-site
  "Given a peer id, returns the Aeron hostname and port that this peer advertises to the rest of the cluster."
  [replica peer-id]
  (get-in replica [:peer-sites peer-id]))

(defn task->peer-sites 
  "Given a job id and task-id returns {:peer-id ... :host ...} allocations for that task"
  [replica job-id task-id]
  (let [peers ((task-allocations replica job-id) task-id)]
    (zipmap peers 
            (map (fn [peer-id] 
                   (:aeron/external-addr (peer-site replica peer-id))) 
                 peers))))

(defn peer-state
  "Given a peer id, returns its current execution state (e.g. :idle, :active, etc)."
  [replica peer-id]
  (get-in replica [:peer-state peer-id]))

(defn peer-allocation
  "Given a peer id, returns the job id and task id that this peer is currently assigned to, if any."
  [replica peer-id]
  (let [rep replica]
    (common/peer->allocated-job (:allocations rep) peer-id)))

(defn job-scheduler
  "Returns the job scheduler for this tenancy of the cluster."
  [replica]
  (:job-scheduler replica))

(defn task-scheduler
  "Given a job id, returns the task scheduler for this job."
  [replica job-id]
  (get-in replica [:task-schedulers job-id]))
