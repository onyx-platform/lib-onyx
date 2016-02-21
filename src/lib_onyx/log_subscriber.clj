(ns lib-onyx.log-subscriber
  (:require [clojure.core.async :refer [chan <!! close! thread]]
            [com.stuartsierra.component :as component]
            [onyx.extensions :as extensions]
            [onyx.api]))

(defn apply-entries! [ch replica-state]
  (thread
    (loop []
      (when-let [entry (<!! ch)]
        (let [result (extensions/apply-log-entry entry (:replica @replica-state))
              rep {:as-of-entry (:message-id entry)
                   :as-of-timestamp (:created-at entry)
                   :replica result}]
          (reset! replica-state rep)
          (recur))))))

(defrecord LogSubscriber [peer-config inbox-capacity]
  component/Lifecycle

  (start [component]
    (let [ch (chan inbox-capacity)
          {:keys [replica env]} (onyx.api/subscribe-to-log peer-config ch)
          state (atom {:as-of-entry nil
                       :as-of-timestamp nil
                       :replica replica})
          apply-thread (apply-entries! ch state)]
      (assoc component
             :ch ch :env env :state state
             :apply-thread apply-thread)))

  (stop [component]
    (close! (:ch component))
    (close! (:apply-thread component))
    (onyx.api/shutdown-env (:env component))))

(defn log-subscriber-component [peer-config inbox-capacity]
  (->LogSubscriber peer-config inbox-capacity))

(defn start-log-subscriber
  "Takes a peer config map, and an args map. Args accepts keys
   :inbox-capacity, representing the buffer size of the core.async
   channel reading from ZooKeeper. This function returns a Component
   with key :state. :state is an atom of a map of keys :replica, :as-of-entry,
   and :as-of-timestamp. :replica is the current replica. :as-of-entry
   is the sequential ID in ZooKeeper for the last entry read by this subscriber,
   and :as-of-timestamp is the timestamp of the entry, as known by ZooKeeper."
  ([peer-config]
   (start-log-subscriber peer-config {:inbox-capacity 1000}))
  ([peer-config args]
   (let [c (log-subscriber-component peer-config (:inbox-capacity args))]
     (component/start c))))

(defn stop-log-subscriber
  "Shuts down the log subscriber Component."
  [log-subscriber]
  (component/stop log-subscriber))
