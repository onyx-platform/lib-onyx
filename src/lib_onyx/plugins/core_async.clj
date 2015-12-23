(ns lib-onyx.plugins.core-async
  (:require [clojure.core.async :refer [chan sliding-buffer >!!]]
            [lib-onyx.job.utils :refer [find-task-by-key add-to-job]]))

(defonce channels (atom {}))

(defn get-channel [id size]
  (or (get @channels id)
      (let [ch (chan size)]
        (swap! channels assoc id ch)
        ch)))

(defn get-input-channel
  ([id] (get-input-channel id 1000))
  ([id size]
   (get-channel id size)))

(defn get-output-channel
  ([id] (get-output-channel id 1001))
  ([id size]
   (get-channel id size)))

(defn inject-in-ch
  [_ lifecycle]
  {:core.async/chan (get-input-channel (:core.async/id lifecycle))})
(defn inject-out-ch
  [_ lifecycle]
  {:core.async/chan (get-output-channel (:core.async/id lifecycle))})
(def in-calls
  {:lifecycle/before-task-start inject-in-ch})
(def out-calls
  {:lifecycle/before-task-start inject-out-ch})

(defn get-core-async-channels
  "This takes a job and returns (by catalog name) input and output channels
  by their corresponding references. Will not handle multiple input and output channels.

  {:read-lines (chan...)
   :write-lines (chan...)}"
  [{:keys [lifecycles catalog]}]
  (let [inputs (:onyx/name (find-task-by-key catalog :onyx/plugin :onyx.plugin.core-async/input))
        outputs (:onyx/name (find-task-by-key catalog :onyx/plugin :onyx.plugin.core-async/output))]
    {inputs  (get-input-channel (:core.async/id
                                  (first (filter #(= inputs (:lifecycle/task %)) lifecycles))))
     outputs (get-output-channel (:core.async/id
                                   (first (filter #(= outputs (:lifecycle/task %)) lifecycles))))}))

(defn add-core-async
  "Instrument a lifecycle with serializeable references to core.async channels
   in the form of UUID's.
   Each catalog entry that is a :onyx/plugin of type:

     :onyx.plugin.core-async/input
       or
     :onyx.plugin.core-async/input

   Will have a reference to a channel contained in the channels atom above
   added under core.async/id. The corrosponding channel can be looked up
   manually by passing it's reference directly to get-channel or by using one
   of the convinience functions in this namespace.
   "
  [{:keys [catalog lifecycles] :as job}]
  (assert (and (sequential? catalog) (sequential? lifecycles)))
  (let [inputs (find-task-by-key catalog :onyx/plugin
                                 :onyx.plugin.core-async/input)
        outputs (find-task-by-key catalog :onyx/plugin
                                  :onyx.plugin.core-async/output)]
    (add-to-job job
                {:lifecycles
                 (mapcat #(remove nil? %)
                         [(when-let [input-task-name (get inputs :onyx/name)]
                            [{:lifecycle/task  input-task-name
                              :lifecycle/calls ::in-calls
                              :core.async/id   (java.util.UUID/randomUUID)}
                             {:lifecycle/task  input-task-name
                              :lifecycle/calls :onyx.plugin.core-async/reader-calls}])
                          (when-let [output-task-name (get outputs :onyx/name)]
                            [{:lifecycle/task  output-task-name
                              :lifecycle/calls ::out-calls
                              :core.async/id   (java.util.UUID/randomUUID)}
                             {:lifecycle/task  output-task-name
                              :lifecycle/calls :onyx.plugin.core-async/writer-calls}])])})))