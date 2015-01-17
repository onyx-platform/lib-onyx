(ns lib-onyx.join
  (:require [onyx.planning :refer [find-task]]
            [onyx.peer.task-lifecycle-extensions :as l-ext]))

(defmethod l-ext/inject-lifecycle-resources :lib-onyx.join/join-segments
  [_ {:keys [onyx.core/catalog onyx.core/task]}]
  (let [t (find-task catalog task)]
    (when-not (:lib-onyx.join/by t)
      (throw (ex-info ":lib-onyx.join/by not specified in catalog entry" {:entry t})))
    (let [k (:lib-onyx.join/by t)
          updated-catalog (map #(if (= % t) (assoc t :onyx.core/group-by-key k) %) catalog)]
      {:onyx.core/params [(atom {}) :id]
       :onyx.core/catalog updated-catalog})))

(defn join-segments [local-state key segment]
  (let [state @local-state]
    (if-let [record (get state (get segment key))]
      (let [result (merge record segment)]
        (swap! local-state dissoc (get segment key))
        result)
      (do (swap! local-state assoc (get segment key) (dissoc segment key))
          []))))

