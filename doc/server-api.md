### Replica Query Server Endpoints

The Replica Query Server has a number of endpoints for accessing the information about a running Onyx cluster. Below we display the HTTP method, the URI, the docstring for the route, and any associated parameters that it takes in its query string.

---

`[:get]` `/job/flow-conditions`


##### Query Params Schema

`{"job-id" java.lang.String}`

Given a job id, returns flow conditions for this job.

---

`[:get]` `/replica/killed-jobs`


##### Query Params Schema

``

Lists all the job ids that have been killed.

---

`[:get]` `/replica/job-allocations`


##### Query Params Schema

``

Returns a map of job id -&gt; task id -&gt; peer ids, denoting which peers are assigned to which tasks.

---

`[:get]` `/job/windows`


##### Query Params Schema

`{"job-id" java.lang.String}`

Given a job id, returns windows for this job.

---

`[:get]` `/job/workflow`


##### Query Params Schema

`{"job-id" java.lang.String}`

Given a job id, returns workflow for this job.

---

`[:get]` `/replica/task-scheduler`


##### Query Params Schema

`{"job-id" java.lang.String}`

Given a job id, returns the task scheduler for this job.

---

`[:get]` `/replica/jobs`


##### Query Params Schema

``

Lists all non-killed, non-completed job ids.

---

`[:get]` `/replica/peer-allocation`


##### Query Params Schema

`{"peer-id" java.lang.String}`

Given a peer id, returns the job id and task id that this peer is currently assigned to, if any.

---

`[:get]` `/replica/task-allocations`


##### Query Params Schema

``

Given a job id, returns a map of task id -&gt; peer ids, denoting which peers are assigned to which tasks for this job only.

---

`[:get]` `/job/catalog`


##### Query Params Schema

`{"job-id" java.lang.String}`

Given a job id, returns catalog for this job.

---

`[:get]` `/replica/peers`


##### Query Params Schema

``

Lists all the peer ids.

---

`[:get]` `/replica/completed-jobs`


##### Query Params Schema

``

Lists all the job ids that have been completed.

---

`[:get]` `/job/triggers`


##### Query Params Schema

`{"job-id" java.lang.String}`

Given a job id, returns triggers for this job.

---

`[:get]` `/replica/tasks`


##### Query Params Schema

`{"job-id" java.lang.String}`

Given a job id, returns all the task ids for this job.

---

`[:get]` `/job/lifecycles`


##### Query Params Schema

`{"job-id" java.lang.String}`

Given a job id, returns lifecycles for this job.

---

`[:get]` `/replica/peer-site`


##### Query Params Schema

`{"peer-id" java.lang.String}`

Given a peer id, returns the Aeron hostname and port that this peer advertises to the rest of the cluster.

---

`[:get]` `/replica/peer-state`


##### Query Params Schema

`{"peer-id" java.lang.String}`

Given a peer id, returns its current execution state (e.g. :idle, :active, etc).

---

`[:get]` `/job/task`


##### Query Params Schema

`{"job-id" java.lang.String, "task-id" java.lang.String}`

Given a job id and task id, returns catalog entry for this task.

---

`[:get]` `/replica/job-scheduler`


##### Query Params Schema

``

Returns the job scheduler for this tenancy of the cluster.

