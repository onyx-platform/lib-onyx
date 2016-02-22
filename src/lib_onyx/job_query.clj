(ns lib-onyx.job-query
  (:require [onyx.log.commands.common :as common]
            [onyx.log.zookeeper :as zk]
            [onyx.static.planning :refer [find-task]]
            [onyx.extensions :as extensions]))

(defn get-log [log-subscriber]
  (:log (:env log-subscriber)))

(defn find-in-seq [coll k v]
  (first (filter #(= v (k %)) coll)))

(defn workflow [log-subscriber job-id]
  (let [log (get-log log-subscriber)]
    (extensions/read-chunk log :workflow job-id)))

(defn catalog [log-subscriber job-id]
  (let [log (get-log log-subscriber)]
    (extensions/read-chunk log :catalog job-id)))

(defn flow-conditions [log-subscriber job-id]
  (let [log (get-log log-subscriber)]
    (extensions/read-chunk log :flow-conditions job-id)))

(defn lifecycles [log-subscriber job-id]
  (let [log (get-log log-subscriber)]
    (extensions/read-chunk log :lifecycles job-id)))

(defn windows [log-subscriber job-id]
  (let [log (get-log log-subscriber)]
    (extensions/read-chunk log :windows job-id)))

(defn triggers [log-subscriber job-id]
  (let [log (get-log log-subscriber)]
    (extensions/read-chunk log :triggers job-id)))

(defn task-information [log-subscriber job-id task-id]
  (let [log (get-log log-subscriber)
        task-name (:name (extensions/read-chunk log :task task-id))]
    (find-task (extensions/read-chunk log :catalog job-id) task-name)))
