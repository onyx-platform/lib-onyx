(ns lib-onyx.peer
  (:require [onyx.api]))

(defn start-peer [n peer-config env-config]
  (let [n-peers (if-some [n-peers n] 
                         (Integer/parseInt (.toString n-peers)) 
                         1)
        _ (println "Starting peer-group")
        peer-group (onyx.api/start-peer-group peer-config)
        _ (println "Starting env")
        env (onyx.api/start-env env-config)
        _ (println "Starting peers")
        peers (onyx.api/start-peers n-peers peer-group)]
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
