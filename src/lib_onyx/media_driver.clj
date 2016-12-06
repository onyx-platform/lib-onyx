(ns lib-onyx.media-driver
  (:require [clojure.core.async :refer [chan <!!]]
            [clojure.tools.cli :refer [parse-opts]])
  (:import [io.aeron Aeron$Context]
           [io.aeron.driver MediaDriver MediaDriver$Context ThreadingMode])
  (:gen-class))

(defn pick-threading-mode [threading-mode]
  (assert (#{"shared" "dedicated" "shared-network"} threading-mode) (str "threading-mode must be one of shared, dedicated, or shared-network"))
  (cond
    (or (nil? threading-mode)
        (= threading-mode "shared")) ThreadingMode/SHARED
    (= threading-mode "dedicated") ThreadingMode/DEDICATED
    (= threading-mode "shared-network") ThreadingMode/SHARED_NETWORK))

(def cli-options
  [["-t" "--threading-mode THREADING-MODE" "Aeron media driver threading mode: shared or dedicated or shared-network"
    :default "shared"
    :validate [#{"shared" "dedicated" "shared-network"} "Must be shared, dedicated, or shared-network"]]
   ["-d" "--delete-dirs" "Delete the media drivers directory on startup" :default false]
   ["-h" "--help"]])

(defn -main [& args]
  (let [opts (parse-opts args cli-options)
        {:keys [help threading-mode delete-dirs]} (:options opts)
        _ (when help
            (run! (fn [opt]
                    (println (clojure.string/join " " (take 3 opt))))
                  cli-options)
            (System/exit 0))
        _ (when (and (System/getProperty "aeron.threading.mode") threading-mode)
            (throw (Exception. "Cannot set both aeron.threading.mode
                                property and threading-mode command-line arg")))
        threading-mode-obj (pick-threading-mode threading-mode)
        _ (println "Starting media driver with threading mode:" threading-mode".
                    Use -t to supply an alternative threading mode.")
        ctx (cond-> (MediaDriver$Context.)
              threading-mode (.threadingMode threading-mode-obj)
              delete-dirs (.dirsDeleteOnStart delete-dirs))
        media-driver (try (MediaDriver/launch ctx)
                          (catch IllegalStateException ise
                            (throw (Exception.
                                    "Error starting media driver. This may be due
                                     to a media driver data incompatibility between
                                     versions. Check that no other media driver has
                                     been started and then use -d to delete the directory
                                     on startup" ise))))]
    (println "Launched the Media Driver. Blocking forever...")
    (<!! (chan))))
