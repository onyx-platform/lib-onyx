(ns lib-onyx.interval
  (:require [clojure.core.async :refer [chan timeout thread alts!! close!]]
            [onyx.planning :refer [find-task]]
            [onyx.peer.task-lifecycle-extensions :as l-ext]
            [taoensso.timbre :refer [fatal]]))

(defn resolve-f [kw]
  (try
    (let [user-ns (symbol (name (namespace kw)))
          user-fn (symbol (name kw))]
      (or (ns-resolve user-ns user-fn) (throw (Exception.))))
    (catch Exception e
      (throw (ex-info "Could not resolve function value for :lib-onyx.interval/fn" {:fn kw})))))

(defmethod l-ext/inject-lifecycle-resources :lib-onyx.interval/recurring-action
  [_ {:keys [onyx.core/catalog onyx.core/task] :as event}]
  (let [t (find-task catalog task)
        f (resolve-f (:lib-onyx.interval/fn t))
        kill-ch (chan)]
    (when-not (:lib-onyx.interval/ms t)
      (throw (ex-info ":lib-onyx.interval/ms not specified in catalog entry"
                      {:lib-onyx.interval/ms (:lib-onyx.interval/ms t)})))
    (thread
     (try
       (loop []
         (let [[v ch] (alts!! [(timeout (:lib-onyx.interval/ms t)) kill-ch])]
           (when-not (= ch kill-ch)
             (f event)
             (recur))))
       (catch Exception e
         (fatal e))))
    {:lib-onyx.interval/kill-ch kill-ch}))

(defmethod l-ext/close-lifecycle-resources :lib-onyx.interval/recurring-action
  [_ {:keys [lib-onyx.interval/kill-ch] :as event}]
  (close! kill-ch)
  {})

