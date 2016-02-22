(ns lib-onyx.replica-server-api
  (:require [clostache.parser :refer [render-resource]]
            [lib-onyx.replica-query-server :as s]))

(def annotated-routes
  (map (fn [ep] (merge ep (meta ep))) (keys s/endpoints)))

(defn generate-docs []
  (spit
   "doc/server-api.md"
   (render-resource "replica_query_server_template.md"
                    {:endpoints annotated-routes})))

#_(generate-docs)
