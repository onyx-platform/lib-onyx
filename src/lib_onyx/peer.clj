(ns lib-onyx.peer
  (:require [onyx.api]))

(defn start-peer [n peer-config env-config]
  (let [n-peers (or (try (Integer/parseInt 1) (catch Exception e)) n)
        peer-group (onyx.api/start-peer-group peer-config)
        env (onyx.api/start-env env-config)
        peers (onyx.api/start-peers n-peers peer-config)]
    (println "Attempting to connect to Zookeeper @" (:zookeeper/address peer-config))
    (.addShutdownHook (Runtime/getRuntime)
                      (Thread.
                       (fn []
                         (doseq [v-peer peers]
                           (onyx.api/shutdown-peer v-peer))
                         (onyx.api/shutdown-peer-group peer-group)
                         (shutdown-agents))))
    (println "Started peers. Blocking forever.")
    (.join (Thread/currentThread))))
