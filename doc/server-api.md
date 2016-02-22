### Replica Query Server Endpoints

---

`[:get]` `/job/flow-conditions`

Given a job id, returns flow conditions for this job.

---

`[:get]` `/replica/killed-jobs`

Lists all the job ids that have been killed.

---

`[:get]` `/job/windows`

Given a job id, returns windows for this job.

---

`[:get]` `/job/workflow`

Given a job id, returns workflow for this job.

---

`[:get]` `/replica/task-allocation`

Given a job id, returns a map of task id -&gt; peer ids, denoting which peers are assigned to which tasks for this job only.

---

`[:get]` `/replica/task-scheduler`

Given a job id, returns the task scheduler for this job.

---

`[:get]` `/replica/jobs`

Lists all non-killed, non-completed job ids.

---

`[:get]` `/replica/peer-allocation`

Given a peer id, returns the job id and task id that this peer is currently assigned to, if any.

---

`[:get]` `/job/catalog`

Given a job id, returns catalog for this job.

---

`[:get]` `/replica/peers`

Lists all the peer idss.

---

`[:get]` `/replica/completed-jobs`

Lists all the job ids that have been completed.

---

`[:get]` `/job/triggers`

Given a job id, returns triggers for this job.

---

`[:get]` `/replica/tasks`

Given a job ids, returns all the task ids for this job.

---

`[:get]` `/job/lifecycles`

Given a job id, returns lifecycles for this job.

---

`[:get]` `/replica/job-allocation`

Returns a map of job id -&gt; task id -&gt; peer ids, denoting which peers are assigned to which tasks.

---

`[:get]` `/replica/peer-site`

Given a peer id, returns the Aeron hostname and port that this peer advertises to the rest of the cluster.

---

`[:get]` `/replica/peer-state`

Given a peer id, returns its current execution state (e.g. :idle, :active, etc).

---

`[:get]` `/job/task`

Given a job id and task id, returns catalog entry for this task.

---

`[:get]` `/replica/job-scheduler`

Returns the job scheduler for this tenancy of the cluster.

