(ns lib-onyx.replica-query-server
  (:require [com.stuartsierra.component :as component]
            [ring.component.jetty :refer [jetty-server]]
            [lib-onyx.log-subscriber :as s]
            [lib-onyx.replica-query :as rq]
            [cheshire.core :refer [generate-string]]))

(def endpoints
  {{:uri "/peers"
    :request-method :get}
   (fn [replica] (rq/peers replica))

   {:uri "/jobs"
    :request-method :get}
   (fn [replica] (rq/jobs replica))

   {:uri "/killed-jobs"
    :request-method :get}
   (fn [replica] (rq/killed-jobs replica))})

(def default-serializer "application/edn")

(def serializers
  {"application/edn" pr-str
   "application/json" generate-string})

(defn serializer-name [content-type]
  (if (serializers content-type)
    content-type
    default-serializer))

(defn get-serializer [content-type]
  (get serializers content-type
       (get serializers default-serializer)))

(defn handler [state {:keys [content-type] :as request}]
  (let [{:keys [replica as-of-entry as-of-timestamp]} @state
        f (get endpoints (select-keys request [:request-method :uri]))
        serializer (get-serializer content-type)]
    (if-not f
      {:status 404
       :headers {"Content-Type" (serializer-name content-type)}
       :body (serializer "Endpoint not found")}
      (let [result (f replica)]
        {:status 200
         :headers {"Content-Type" (serializer-name content-type)}
         :body (serializer {:result result
                            :as-of-entry as-of-entry
                            :as-of-timestamp as-of-timestamp})}))))

(defn app [state]
  {:handler (partial handler state)})

(defrecord SubscriberServer [port]
  component/Lifecycle

  (start [component]
    (let [state (:state (:subscriber component))
          c (jetty-server {:app (app state) :port port})]
      (assoc component :server (component/start c))))

  (stop [component]
    (component/stop (:server component))
    (assoc component :server nil)))

(defn replica-query-system [peer-config server-port]
  (component/system-map
   :subscriber (s/log-subscriber-component peer-config 1000)
   :server (component/using (->SubscriberServer server-port)
                            [:subscriber])))

(defn start-replica-query-server [peer-config server-port]
  (component/start (replica-query-system peer-config server-port)))

(defn stop-replica-query-server [rq-server]
  (component/stop rq-server))
