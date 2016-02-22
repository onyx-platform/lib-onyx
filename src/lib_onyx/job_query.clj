(ns lib-onyx.job-query
  (:require [onyx.log.commands.common :as common]
            [onyx.log.zookeeper :as zk]
            [onyx.static.planning :refer [find-task]]
            [onyx.extensions :as extensions]))

(defn get-log [log-subscriber]
  (:log (:env log-subscriber)))

(defn catalog [log-subscriber job-id]
  (let [log (get-log log-subscriber)]
    (extensions/read-chunk log :catalog job-id)))

(defn task-information [log-subscriber job-id task-id]
  (let [log (get-log log-subscriber)
        task-name (:name (extensions/read-chunk log :task task-id))]
    (find-task (extensions/read-chunk log :catalog job-id) task-name)))
