(ns lib-onyx.job-query
  (:require [onyx.log.commands.common :as common]
            [onyx.log.zookeeper :as zk]
            [onyx.static.planning :refer [find-task]]
            [onyx.extensions :as extensions]))

(defn ^{:no-doc true} get-log [log-subscriber]
  (:log (:env log-subscriber)))

(defn ^{:no-doc true} find-in-seq [coll k v]
  (first (filter #(= v (k %)) coll)))

(defn workflow
  "Given a job id, returns workflow for this job."
  [log-subscriber job-id]
  (let [log (get-log log-subscriber)]
    (extensions/read-chunk log :workflow job-id)))

(defn catalog
  "Given a job id, returns catalog for this job."
  [log-subscriber job-id]
  (let [log (get-log log-subscriber)]
    (extensions/read-chunk log :catalog job-id)))

(defn flow-conditions
  "Given a job id, returns flow conditions for this job."
  [log-subscriber job-id]
  (let [log (get-log log-subscriber)]
    (extensions/read-chunk log :flow-conditions job-id)))

(defn lifecycles
  "Given a job id, returns lifecycles for this job."
  [log-subscriber job-id]
  (let [log (get-log log-subscriber)]
    (extensions/read-chunk log :lifecycles job-id)))

(defn windows
  "Given a job id, returns windows for this job."
  [log-subscriber job-id]
  (let [log (get-log log-subscriber)]
    (extensions/read-chunk log :windows job-id)))

(defn triggers
  "Given a job id, returns triggers for this job."
  [log-subscriber job-id]
  (let [log (get-log log-subscriber)]
    (extensions/read-chunk log :triggers job-id)))

(defn task-information
  "Given a job id and task id, returns catalog entry for this task."
  [log-subscriber job-id task-id]
  (let [log (get-log log-subscriber)
        task-name (:name (extensions/read-chunk log :task task-id))]
    (find-task (extensions/read-chunk log :catalog job-id) task-name)))
