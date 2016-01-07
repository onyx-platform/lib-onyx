(defproject org.onyx-platform/lib-onyx "0.8.3.0"
  :description "A library to support additional functionality in Onyx"
  :url "https://github.com/MichaelDrogalis/lib-onyx"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/core.async "0.2.374"]
                 [com.taoensso/timbre "4.1.4"]
                 [cheshire "5.5.0"]
                 [ragtime "0.5.2"]
                 [mysql/mysql-connector-java "5.1.6"]
                 [com.rpl/specter "0.9.1"]]
  :profiles {:dev {:dependencies []
                   :plugins []}})
