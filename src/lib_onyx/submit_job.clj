(ns lib-onyx.submit-job
  (:gen-class)
  (:require [aero.core :refer [read-config]]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.java.io :as io]
            [clojure.edn]
            onyx.api
            [onyx.job :refer [register-job]]))

(defn job-exists? [job-name]
  (contains? (methods register-job) job-name ))

(defn file-exists? [file]
  (let [f (clojure.string/trim file)
        classf (io/resource file)
        relf (when (.exists (io/as-file f)) (io/as-file f))]
    (or classf relf)))

(def cli-options
  ;; An option with a required argument
  [["-c" "--config FILE" "Aero/EDN config file"
    :parse-fn file-exists?
    :validate [identity "File does not exist relative to the workdir or on the classpath"
               read-config "Not a valid Aero or EDN file"]]
   ["-p" "--profile PROFILE" "Aero profile"
    :parse-fn (fn [profile] (clojure.edn/read-string (clojure.string/trim profile)))]
   ["-h" "--help"]])

(defn -main
  "Submit's a registered job to the Onyx cluster"
  [& args]
  (let [{:keys [options arguments summary errors]} (parse-opts args cli-options)
        [job-name] arguments
        {:keys [profile config]} options
        parsed-config (read-config config {:profile profile})]
    (assert (job-exists? job-name) (str "There is no job registered under the name " job-name))
    (-> (onyx.api/submit-job (:peer-config parsed-config)
                             (register-job job-name parsed-config))
        (clojure.pprint/pprint))))
