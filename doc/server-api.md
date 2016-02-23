### Replica Query Server Endpoints

The Replica Query Server has a number of endpoints for accessing the information about a running Onyx cluster. Below we display the HTTP method, the URI, the docstring for the route, and any associated parameters that it takes in its query string.

#### Summary

- `/job/catalog`
- `/job/flow-conditions`
- `/job/lifecycles`
- `/job/task`
- `/job/triggers`
- `/job/windows`
- `/job/workflow`
- `/replica/completed-jobs`
- `/replica/job-allocations`
- `/replica/job-scheduler`
- `/replica/jobs`
- `/replica/killed-jobs`
- `/replica/peer-allocation`
- `/replica/peer-site`
- `/replica/peer-state`
- `/replica/peers`
- `/replica/task-allocations`
- `/replica/task-scheduler`
- `/replica/tasks`

---

##### Route

`[:get]` `/job/catalog`


##### Query Params Schema

`{"job-id" java.lang.String}`

##### Docstring

Given a job id, returns catalog for this job.

---

##### Route

`[:get]` `/job/flow-conditions`


##### Query Params Schema

`{"job-id" java.lang.String}`

##### Docstring

Given a job id, returns flow conditions for this job.

---

##### Route

`[:get]` `/job/lifecycles`


##### Query Params Schema

`{"job-id" java.lang.String}`

##### Docstring

Given a job id, returns lifecycles for this job.

---

##### Route

`[:get]` `/job/task`


##### Query Params Schema

`{"job-id" java.lang.String, "task-id" java.lang.String}`

##### Docstring

Given a job id and task id, returns catalog entry for this task.

---

##### Route

`[:get]` `/job/triggers`


##### Query Params Schema

`{"job-id" java.lang.String}`

##### Docstring

Given a job id, returns triggers for this job.

---

##### Route

`[:get]` `/job/windows`


##### Query Params Schema

`{"job-id" java.lang.String}`

##### Docstring

Given a job id, returns windows for this job.

---

##### Route

`[:get]` `/job/workflow`


##### Query Params Schema

`{"job-id" java.lang.String}`

##### Docstring

Given a job id, returns workflow for this job.

---

##### Route

`[:get]` `/replica/completed-jobs`


##### Query Params Schema

``

##### Docstring

Lists all the job ids that have been completed.

---

##### Route

`[:get]` `/replica/job-allocations`


##### Query Params Schema

``

##### Docstring

Returns a map of job id -> task id -> peer ids, denoting which peers are assigned to which tasks.

---

##### Route

`[:get]` `/replica/job-scheduler`


##### Query Params Schema

``

##### Docstring

Returns the job scheduler for this tenancy of the cluster.

---

##### Route

`[:get]` `/replica/jobs`


##### Query Params Schema

``

##### Docstring

Lists all non-killed, non-completed job ids.

---

##### Route

`[:get]` `/replica/killed-jobs`


##### Query Params Schema

``

##### Docstring

Lists all the job ids that have been killed.

---

##### Route

`[:get]` `/replica/peer-allocation`


##### Query Params Schema

`{"peer-id" java.lang.String}`

##### Docstring

Given a peer id, returns the job id and task id that this peer is currently assigned to, if any.

---

##### Route

`[:get]` `/replica/peer-site`


##### Query Params Schema

`{"peer-id" java.lang.String}`

##### Docstring

Given a peer id, returns the Aeron hostname and port that this peer advertises to the rest of the cluster.

---

##### Route

`[:get]` `/replica/peer-state`


##### Query Params Schema

`{"peer-id" java.lang.String}`

##### Docstring

Given a peer id, returns its current execution state (e.g. :idle, :active, etc).

---

##### Route

`[:get]` `/replica/peers`


##### Query Params Schema

``

##### Docstring

Lists all the peer ids.

---

##### Route

`[:get]` `/replica/task-allocations`


##### Query Params Schema

``

##### Docstring

Given a job id, returns a map of task id -> peer ids, denoting which peers are assigned to which tasks for this job only.

---

##### Route

`[:get]` `/replica/task-scheduler`


##### Query Params Schema

`{"job-id" java.lang.String}`

##### Docstring

Given a job id, returns the task scheduler for this job.

---

##### Route

`[:get]` `/replica/tasks`


##### Query Params Schema

`{"job-id" java.lang.String}`

##### Docstring

Given a job id, returns all the task ids for this job.

