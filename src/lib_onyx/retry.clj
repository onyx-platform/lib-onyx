(ns lib-onyx.retry
  (:require [onyx.planning :refer [find-task]]
            [onyx.peer.pipeline-extensions :as p-ext]
            [onyx.peer.task-lifecycle-extensions :as l-ext]
            [onyx.peer.operation :as operation]
            [onyx.extensions :as extensions]))

(defn retry-on-failure [f produce-f segment]
  (try
    (f segment)
    (catch Exception e
      (produce-f (:segment (ex-data e)))
      [])))

(defmethod l-ext/inject-temporal-resources :lib-onyx.join/requeue-and-retry
  [_ {:keys [onyx.core/queue onyx.core/ingress-queues onyx.core/task-map] :as context}]
  (let [session (extensions/create-tx-session queue)
        producers (doall (map (partial extensions/create-producer queue session) (vals ingress-queues)))
        n (:lib-onyx.retry/n task-map)
        f (fn [segment]
            (when (< (or (:failures segment) 0) n)
              (let [retry-segment (assoc segment :failures (inc (or (:failures segment) 0)))]
                (doseq [p producers]
                  (let [context {:onyx.core/results [retry-segment]}
                        compression-context (p-ext/compress-batch context)
                        retry-segment (-> compression-context :onyx.core/compressed first :compressed)]
                    (extensions/produce-message queue p session retry-segment)
                    (extensions/close-resource queue p))))))]
    {:onyx.core/session session
     :error-producers producers
     :produce-f f
     :onyx.core/params [f]}))

(defmethod l-ext/close-temporal-resources :lib-onyx.join/requeue-and-retry
  [_ context]
  (doseq [p (:error-producers context)]
    (extensions/close-resource (:onyx.core/queue context) p))
  {})

