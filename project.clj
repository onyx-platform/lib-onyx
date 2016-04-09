(defproject org.onyxplatform/lib-onyx "0.8.12.1-SNAPSHOT"
  :description "A library to support additional functionality in Onyx"
  :url "https://github.com/onyx-platform/lib-onyx"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories {"snapshots" {:url "https://clojars.org/repo"
                              :username :env
                              :password :env
                              :sign-releases false}
                 "releases" {:url "https://clojars.org/repo"
                             :username :env
                             :password :env
                             :sign-releases false}}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 ^{:voom {:repo "git@github.com:onyx-platform/onyx.git" :branch "master"}}
                 [org.onyxplatform/onyx "0.8.11"]
                 [com.stuartsierra/component "0.3.1"]
                 [joplin.core "0.3.6"]
                 [cheshire "5.5.0"]
                 [ring-jetty-component "0.3.0"]
                 [ring "1.4.0"]]
  :profiles {:dev {:dependencies [[de.ubercode.clostache/clostache "1.4.0"]]
                   :plugins [[codox "0.8.8"]
                             [lein-set-version "0.4.1"]
                             [lein-update-dependency "0.1.2"]
                             [lein-pprint "1.1.1"]]
                   :source-paths ["dev-src"]}
             :circle-ci {:jvm-opts ["-Xmx4g"]}}
  :codox {:output-dir "doc/api"})
