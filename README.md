# lib-onyx

lib-onyx is a utilities library to make working with Onyx's extensibility features easier out of the box.

### Installation

On Clojars:

```
[org.onyxplatform/lib-onyx "0.13.3.0-SNAPSHOT"]
```

### API docs

- [Clojure API](http://www.onyxplatform.org/lib-onyx)
- [HTTP API](doc/server-api.md)

### Log Subscription

View every update to the Onyx Log as it happens. Just supply your peer configuration:

```clojure
(require '[lib-onyx.log-subscriber :as s])

(def subscriber (s/start-log-subscriber your-peer-config))

(println (:replica @(:state subscriber))
(println (:as-of-entry @(:state subscriber)))
(println (:as-of-timestamp @(:state subscriber)))

(s/stop-log-subscriber subscriber)
```

See the API docs listed above for more information.

### Replica Queries

Given a log subscriber, use convenience functions to query the replica state.
All functions take a dereferenced replica so that they can operate on
a stable, immutable value:

```clojure
(require '[lib-onyx.replica-query :as rq])
(require '[lib-onyx.log-subscriber :as s])

(def subscriber (s/start-log-subscriber your-peer-config))

(def replica (rq/deref-replica subscriber)

(println (rq/jobs replica))

(println (rq/peers replica))

(s/stop-log-subscriber subscriber)
```

See the API docs listed above for more information.

### Replica HTTP Server

lib-onyx exposes an HTTP server to service replica and cluster queries across languages.

```clojure
(require '[lib-onyx.lib-onyx.replica-query-server :as rqs])

(def server-port 3000)

(def server (rqs/start-replica-query-server peer-config server-port)
```

Then query it:

```
$ http --json http://localhost:3000/replica/peers
```

```json
HTTP/1.1 200 OK
Content-Length: 197
Content-Type: application/json
Date: Tue, 23 Feb 2016 03:35:08 GMT
Server: Jetty(9.2.10.v20150310)

{
    "as-of-entry": 12,
    "as-of-timestamp": 1456108757818,
    "result": [
        "e52df81d-38c9-44e6-9e3d-177d3e83292b",
        "fd4725f9-3429-49eb-840d-6c3e29cecc41",
        "fc933dda-7260-4547-93fc-241a02ca599a"
    ],
    "status": "success"
}
```

See the HTTP docs listed at the top of this page for all the endpoints.

And bring it back it down with:

```clojure
(rqs/stop-replica-query-server server)
```

### Joplin Migrators

lib-onyx add's support for using [Joplin](https://github.com/juxt/joplin) db
migrations as a prerequisite for job start. This will ensure that your target
database is in the correct state before Onyx starts reading/writing to it. You
must include your specific database dependency and the relevant config. These
are listed on the https://github.com/juxt/joplin README.md.

From there, provide a joplin config. This is an example if you're using
[Aero](https://github.com/juxt/aero). If not, the `#path` tags are just `get-in`
`ks` syntax for config file traversal.

``` clojure
 :joplin-config
 {:databases {:sql {:type :sql
                    :url "jdbc:mysql://192.168.99.100:3306/onyx?user=admin&password=mypass"
                    :migrations-table "ragtime_migrations"}}
  :migrators {:sql-migrator "resources/migrators/sql"}
  :seeds {:sql-seed "my.seed.namespace/run"
  :environments {:dev [{:db #path       [:joplin-config :databases :sql]
                        :migrator #path [:joplin-config :migrators :sql-migrator]
                        :seed #path     [:joplin-config :seeds :sql-seed}]}}
```

Then, either use the `lib-onyx.migrations.sql/joplin` lifecycle with the
`:joplin/config` and `:joplin/environment` keys set in either your catalog or
lifecycle map, or use the provided task-bundle middleware `with-joplin-migrations`
to handle the lifecycle and config instrumentation for you.

`:joplin/config` corresponds to the above config map. Really all you need is the
`:environments` key  specified and `:joplin/environment` will simply ensure up
to date migrations for all the environment maps inside the specified environment's
vector. In the above case, the `{:joplin/environment :dev}` would be your only
choice.

## License

Copyright © 2016 Distributed Masonry

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
