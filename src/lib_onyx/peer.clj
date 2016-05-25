(ns lib-onyx.peer
  (:require [onyx.api])
  (:import [uk.co.real_logic.aeron Aeron$Context]
           [uk.co.real_logic.aeron.driver MediaDriver MediaDriver$Context ThreadingMode]))

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

(defn pick-threading-mode [threading-mode]
  (assert (#{"shared" "dedicated" "shared-network"} threading-mode) (str "threading-mode must be one of shared, dedicated, or shared-network"))
  (cond
    (or (nil? threading-mode)
        (= threading-mode "shared")) ThreadingMode/SHARED
    (= threading-mode "dedicated") ThreadingMode/DEDICATED
    (= threading-mode "shared-network") ThreadingMode/SHARED_NETWORK))

(defn start-aeron-media-driver [threading-mode delete-dirs?]
  (let [threading-mode-obj (pick-threading-mode threading-mode)
        ctx (cond-> (MediaDriver$Context.)
              threading-mode (.threadingMode threading-mode-obj)
              delete-dirs? (.dirsDeleteOnStart delete-dirs?))]
    (try (MediaDriver/launch ctx)
         (catch IllegalStateException ise
           (throw (Exception. "Error starting media driver.
                               This may be due to a media driver data
                               incompatibility between versions.
                               Check that no other media driver has been
                               started and then use -d to delete the directory
                               on startup" ise))))
    (println "Launched the Media Driver. Blocking forever...")
    (.join (Thread/currentThread))))
