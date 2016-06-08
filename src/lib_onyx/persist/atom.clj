(ns lib-onyx.persist.atom
  (:require [clojure.set :refer [join]]))

;; Place to store our atom's
(defonce dbs (atom {}))

(defn get-store
  "Fetches a db by id, creates one if there is not one."
  [id]
  (if-let [db (get @dbs id)]
    db
    (get (swap! dbs assoc id (atom {})) id)))

(defn get-stores
  "Walks through triggers with a :testing-trigger/db-atom present,
  returns a map of trigger :sync atoms."
  [{:keys [catalog windows triggers]}]
  (reduce (fn [acc itm]
            (if-let [id (::atom-id itm)]
              (assoc acc id (get-store id))
              acc)) {} triggers))

(defn inject-store
  [_ lifecycle]
  {::store (get-store (::atom-id lifecycle))})

(def calls
  {:lifecycle/before-task-start inject-store})

(defn persist-trending
  [event window trigger {:keys [group-key trigger-update] :as state-event} state]
  (let [store-id (get trigger ::atom-id)
        store (get-store store-id)]
    (swap! store assoc-in [[(:lower-bound state-event) (:upper-bound state-event)]
                           group-key] state)))

(defn with-trigger-to-atom
  [window-id atom-id]
  (fn [task-definition]
    (let [task-name (get-in task-definition [:task :task-map :onyx/name])]
      (-> task-definition
          (update-in [:task :triggers] conj
                     {:trigger/window-id window-id
                      :trigger/refinement :onyx.refinements/accumulating
                      :trigger/on :onyx.triggers/segment
                      :trigger/threshold [5 :elements]
                      ::atom-id atom-id
                      :trigger/sync ::persist-trending})
          (update-in [:task :lifecycles] conj
                     {:lifecycle/task task-name
                      ::atom-id atom-id
                      :lifecycle/calls ::calls})))))
