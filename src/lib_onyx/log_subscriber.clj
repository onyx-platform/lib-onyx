(ns lib-onyx.log-subscriber
  (:require [clojure.core.async :refer [chan <!! close! thread]]
            [com.stuartsierra.component :as component]
            [onyx.extensions :as extensions]
            [onyx.api]))

(defn ^{:no-doc true} apply-entries! [ch replica-state sub-component callback-fn]
  (thread
    (loop []
      (when-let [entry (<!! ch)]
        (let [result (extensions/apply-log-entry entry (:replica @replica-state))
              rep {:as-of-entry (:message-id entry)
                   :as-of-timestamp (:created-at entry)
                   :entry entry
                   :replica result}]
          (when callback-fn
            (callback-fn sub-component rep))
          (reset! replica-state rep)
          (recur))))))

(defrecord LogSubscriber [peer-config inbox-capacity callback-fn]
  component/Lifecycle

  (start [component]
    (println "Starting LogSubscriber")
    (let [ch (chan inbox-capacity)
          {:keys [replica env]} (onyx.api/subscribe-to-log peer-config ch)
          state (atom {:as-of-entry nil
                       :as-of-timestamp nil
                       :entry nil
                       :replica replica})
          sub-component (assoc component :ch ch :env env :state state)
          apply-thread (apply-entries! ch state sub-component callback-fn)]
      (assoc sub-component :apply-thread apply-thread)))

  (stop [component]
    (println "Stopping LogSubscriber")
    (close! (:ch component))
    (close! (:apply-thread component))
    (onyx.api/shutdown-env (:env component))))

(defn log-subscriber-component 
  ([peer-config inbox-capacity]
   (->LogSubscriber peer-config inbox-capacity nil))
  ([peer-config inbox-capacity callback-fn]
   (->LogSubscriber peer-config inbox-capacity callback-fn)))

(defn start-log-subscriber
  "Takes a peer config map, and an args map. Args accepts keys
  :inbox-capacity, representing the buffer size of the core.async
  channel reading from ZooKeeper, and optionally :callback-fn which is called
  each time a log entry is read and applied to the replica. This function is supplied
  with the subscriber component and next replica state map.
  
  This function returns a Component with key :state. :state is an atom of a map
  of keys :replica, :as-of-entry, and :as-of-timestamp. :replica is the current
  replica.  :as-of-entry is the sequential ID in ZooKeeper for the last entry
  read by this subscriber, and :as-of-timestamp is the timestamp of the entry,
  as known by ZooKeeper."
  ([peer-config]
   (start-log-subscriber peer-config {}))
  ([peer-config args]
   (let [c (log-subscriber-component peer-config 
                                     (or (:inbox-capacity args) 1000) 
                                     (:callback-fn args))]
     (component/start c))))

(defn stop-log-subscriber
  "Shuts down the log subscriber Component."
  [log-subscriber]
  (component/stop log-subscriber))
