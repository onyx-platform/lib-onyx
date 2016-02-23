(ns ^{:no-doc true} lib-onyx.render-server-api
  (:require [clostache.parser :refer [render-resource]]
            [lib-onyx.replica-query-server :as s]))

(def annotated-routes
  (sort-by :uri (map (fn [[k v]] (merge k v)) s/endpoints)))

(defn generate-docs []
  (spit
   "doc/server-api.md"
   (render-resource "replica_query_server_template.md"
                    {:endpoints annotated-routes})))

(defn -main [& args]
  (generate-docs))
