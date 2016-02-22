(defproject org.onyx-platform/lib-onyx "0.8.12.0-SNAPSHOT"
  :description "A library to support additional functionality in Onyx"
  :url "https://github.com/onyx-platform/lib-onyx"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.onyxplatform/onyx "0.8.11"]
                 [com.stuartsierra/component "0.3.1"]
                 [cheshire "5.5.0"]
                 [ring-jetty-component "0.3.0"]
                 [ring "1.4.0"]]
  :profiles {:dev {:dependencies [[de.ubercode.clostache/clostache "1.4.0"]]
                   :source-paths ["dev-src"]}})
