(ns lib-onyx.replica-query
  (:require [onyx.log.commands.common :as common]))

(defn replica [log-subscriber]
  (:replica @(:state log-subscriber)))

(defn jobs [log-subscriber]
  (:jobs (replica log-subscriber)))

(defn killed-jobs [log-subscriber]
  (:killed-jobs (replica log-subscriber)))

(defn peers [log-subscriber]
  (:peers (replica log-subscriber)))

(defn tasks [log-subscriber job-id]
  (get-in (replica log-subscriber) [:tasks job-id]))

(defn job-allocations [log-subscriber]
  (:allocations (replica log-subscriber)))

(defn task-allocations [log-subscriber job-id]
  ((job-allocations log-subscriber) job-id))

(defn peer-site [log-subscriber peer-id]
  (get-in (replica log-subscriber) [:peer-sites peer-id]))

(defn peer-state [log-subscriber peer-id]
  (get-in (replica log-subscriber) [:peer-state peer-id]))

(defn peer-allocation [log-subscriber peer-id]
  (let [rep (replica log-subscriber)]
    (common/peer->allocated-job (:allocations rep) peer-id)))

(defn job-scheduler [log-subscriber]
  (:job-scheduler (replica log-subscriber)))

(defn task-scheduler [log-subscriber job-id]
  (get-in (replica log-subscriber) [:task-schedulers job-id]))
