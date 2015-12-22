(defproject org.onyx-platform/lib-onyx "0.8.3.0"
  :description "A library to support additional functionality in Onyx"
  :url "https://github.com/MichaelDrogalis/lib-onyx"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.onyxplatform/onyx "0.8.3"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]]
                   :plugins [[lein-midje "3.1.1"]]}})
