(ns lib-onyx.interval-test
  (:require [clojure.core.async :refer [chan >!! <!! close!]]
            [onyx.peer.task-lifecycle-extensions :as l-ext]
            [onyx.peer.pipeline-extensions :as p-ext]
            [onyx.extensions :as extensions]
            [onyx.plugin.core-async]
            [onyx.api]
            [lib-onyx.interval]))

(defn only-even-numbers [local-state {:keys [n] :as segment}]
  (Thread/sleep 50)
  (if (even? n)
    segment
    (do (swap! local-state conj segment)
        [])))

(defn log-and-purge [{:keys [interval-test/state]}]
  (prn "State is: " @state)
  (prn "Flushing local state")
  (reset! state []))

(def workflow
  [[:in :capitalize-names]
   [:capitalize-names :out]])

(def capacity 1000)

(def input-chan (chan capacity))

(def output-chan (chan capacity))

(defmethod l-ext/inject-lifecycle-resources :in
  [_ _] {:core-async/in-chan input-chan})

(defmethod l-ext/inject-lifecycle-resources :capitalize-names
  [_ _]
  (let [state (atom [])]
    {:onyx.core/params [state]
     :interval-test/state state}))

(defmethod l-ext/inject-lifecycle-resources :out
  [_ _] {:core-async/out-chan output-chan})

(def batch-size 1)

(def catalog
  [{:onyx/name :in
    :onyx/ident :core.async/read-from-chan
    :onyx/type :input
    :onyx/medium :core.async
    :onyx/consumption :sequential
    :onyx/batch-size batch-size
    :onyx/doc "Reads segments from a core.async channel"}

   {:onyx/name :capitalize-names
    :onyx/ident :lib-onyx.interval/recurring-action
    :onyx/fn :lib-onyx.interval-test/only-even-numbers
    :onyx/type :function
    :onyx/consumption :concurrent
    :lib-onyx.interval/fn :lib-onyx.interval-test/log-and-purge
    :lib-onyx.interval/ms 300
    :onyx/batch-size batch-size
    :onyx/doc "Calls function :lib-onyx.interval/fn every :lib-onyx.interval/ms milliseconds with the pipeline map"}

   {:onyx/name :out
    :onyx/ident :core.async/write-to-chan
    :onyx/type :output
    :onyx/medium :core.async
    :onyx/consumption :sequential
    :onyx/batch-size batch-size
    :onyx/doc "Writes segments to a core.async channel"}])

(def input-segments
  (conj (mapv (fn [n] {:n n}) (range 50)) :done))

(doseq [segment input-segments]
  (>!! input-chan segment))

(close! input-chan)

(def id (java.util.UUID/randomUUID))

(def coord-opts
  {:hornetq/mode :vm
   :hornetq/server? true
   :hornetq.server/type :vm
   :zookeeper/address "127.0.0.1:2186"
   :zookeeper/server? true
   :zookeeper.server/port 2186
   :onyx/id id
   :onyx.coordinator/revoke-delay 5000})

(def peer-opts
  {:hornetq/mode :vm
   :zookeeper/address "127.0.0.1:2186"
   :onyx/id id})

(def conn (onyx.api/connect :memory coord-opts))

(def v-peers (onyx.api/start-peers conn 1 peer-opts))

(onyx.api/submit-job conn {:catalog catalog :workflow workflow})

(def results (onyx.plugin.core-async/take-segments! output-chan))

(doseq [v-peer v-peers]
  ((:shutdown-fn v-peer)))

(onyx.api/shutdown conn)

