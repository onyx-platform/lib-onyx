(ns lib-onyx.replica-query-server
  (:require [com.stuartsierra.component :as component]
            [ring.component.jetty :refer [jetty-server]]
            [ring.middleware.params :refer [wrap-params]]
            [lib-onyx.log-subscriber :as s]
            [lib-onyx.replica-query :as rq]
            [lib-onyx.job-query :as jq]
            [cheshire.core :refer [generate-string]])
  (:import [java.util UUID]))

(defn parse-uuid [x]
  (UUID/fromString x))

(def endpoints
  {^{:doc "Lists all the peer idss."}
   {:uri "/replica/peers"
    :request-method :get}
   (fn [request log-subscriber replica]
     (rq/peers replica))

   ^{:doc "Lists all non-killed, non-completed job ids."}
   {:uri "/replica/jobs"
    :request-method :get}
   (fn [request log-subscriber replica]
     (rq/jobs replica))

   ^{:doc "Lists all the job ids that have been killed."}
   {:uri "/replica/killed-jobs"
    :request-method :get}
   (fn [request log-subscriber replica]
     (rq/killed-jobs replica))

   ^{:doc "Lists all the job ids that have been completed."}
   {:uri "/replica/completed-jobs"
    :request-method :get}
   (fn [request log-subscriber replica]
     (rq/completed-jobs replica))

   ^{:doc "Given a job ids, returns all the task ids for this job."}
   {:uri "/replica/tasks"
    :request-method :get}
   (fn [request log-subscriber replica]
     (let [job-id (get-in request [:query-params "job-id"])]
       (rq/tasks replica (parse-uuid job-id))))

   ^{:doc "Returns a map of job id -> task id -> peer ids, denoting which peers are assigned to which tasks."}
   {:uri "/replica/job-allocation"
    :request-method :get}
   (fn [request log-subscriber replica]
     (rq/job-allocations replica))

   ^{:doc "Given a job id, returns a map of task id -> peer ids, denoting which peers are assigned to which tasks for this job only."}
   {:uri "/replica/task-allocation"
    :request-method :get}
   (fn [request log-subscriber replica]
     (rq/task-allocations replica))

   ^{:doc "Given a peer id, returns the Aeron hostname and port that this peer advertises to the rest of the cluster."}
   {:uri "/replica/peer-site"
    :request-method :get}
   (fn [request log-subscriber replica]
     (let [peer-id (get-in request [:query-params "peer-id"])]
       (rq/peer-site replica (parse-uuid peer-id))))

   ^{:doc "Given a peer id, returns its current execution state (e.g. :idle, :active, etc)."}
   {:uri "/replica/peer-state"
    :request-method :get}
   (fn [request log-subscriber replica]
     (let [peer-id (get-in request [:query-params "peer-id"])]
       (rq/peer-state replica (parse-uuid peer-id))))

   ^{:doc "Given a peer id, returns the job id and task id that this peer is currently assigned to, if any."}
   {:uri "/replica/peer-allocation"
    :request-method :get}
   (fn [request log-subscriber replica]
     (let [peer-id (get-in request [:query-params "peer-id"])]
       (rq/peer-allocation replica (parse-uuid peer-id))))

   ^{:doc "Returns the job scheduler for this tenancy of the cluster."}
   {:uri "/replica/job-scheduler"
    :request-method :get}
   (fn [request log-subscriber replica]
     (rq/job-scheduler replica))

   ^{:doc "Given a job id, returns the task scheduler for this job."}
   {:uri "/replica/task-scheduler"
    :request-method :get}
   (fn [request log-subscriber replica]
     (let [job-id (get-in request [:query-params "job-id"])]
       (rq/task-scheduler replica (parse-uuid job-id))))

   ^{:doc "Given a job id, returns workflow for this job."}
   {:uri "/job/workflow"
    :request-method :get}
   (fn [request log-subscriber replica]
     (let [job-id (->> (get-in request [:query-params "job-id"])
                       (parse-uuid))]
       (jq/workflow log-subscriber job-id)))

   ^{:doc "Given a job id, returns catalog for this job."}
   {:uri "/job/catalog"
    :request-method :get}
   (fn [request log-subscriber replica]
     (let [job-id (->> (get-in request [:query-params "job-id"])
                       (parse-uuid))]
       (jq/catalog log-subscriber job-id)))

   ^{:doc "Given a job id, returns flow conditions for this job."}
   {:uri "/job/flow-conditions"
    :request-method :get}
   (fn [request log-subscriber replica]
     (let [job-id (->> (get-in request [:query-params "job-id"])
                       (parse-uuid))]
       (jq/flow-conditions log-subscriber job-id)))

   ^{:doc "Given a job id, returns lifecycles for this job."}
   {:uri "/job/lifecycles"
    :request-method :get}
   (fn [request log-subscriber replica]
     (let [job-id (->> (get-in request [:query-params "job-id"])
                       (parse-uuid))]
       (jq/lifecycles log-subscriber job-id)))

   ^{:doc "Given a job id, returns windows for this job."}
   {:uri "/job/windows"
    :request-method :get}
   (fn [request log-subscriber replica]
     (let [job-id (->> (get-in request [:query-params "job-id"])
                       (parse-uuid))]
       (jq/windows log-subscriber job-id)))

   ^{:doc "Given a job id, returns triggers for this job."}
   {:uri "/job/triggers"
    :request-method :get}
   (fn [request log-subscriber replica]
     (let [job-id (->> (get-in request [:query-params "job-id"])
                       (parse-uuid))]
       (jq/triggers log-subscriber job-id)))

   ^{:doc "Given a job id and task id, returns catalog entry for this task."}
   {:uri "/job/task"
    :request-method :get}
   (fn [request log-subscriber replica]
     (let [job-id (->> (get-in request [:query-params "job-id"])
                       (parse-uuid))
           task-id (->> (get-in request [:query-params "task-id"])
                        (parse-uuid))]
       (jq/task-information log-subscriber job-id task-id)))})

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

(defn handler [log-subscriber state {:keys [content-type] :as request}]
  (let [{:keys [replica as-of-entry as-of-timestamp]} @state
        f (get endpoints (select-keys request [:request-method :uri]))
        serializer (get-serializer content-type)]
    (if-not f
      {:status 404
       :headers {"Content-Type" (serializer-name content-type)}
       :body (serializer {:status :failed :message "Endpoint not found."})}
      (let [result (f request log-subscriber replica)]
        {:status 200
         :headers {"Content-Type" (serializer-name content-type)}
         :body (serializer {:result result
                            :as-of-entry as-of-entry
                            :as-of-timestamp as-of-timestamp})}))))

(defn app [log-subscriber state]
  {:handler (wrap-params (partial handler log-subscriber state))})

(defrecord SubscriberServer [port]
  component/Lifecycle

  (start [component]
    (let [subscriber (:subscriber component)
          state (:state subscriber)
          c (jetty-server {:app (app subscriber state) :port port})]
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
