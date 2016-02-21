# lib-onyx

lib-onyx is a utilities library to make working with Onyx's extensibility features easier out of the box.

### Installation

On Clojars:

```
[org.onyxplatform/lib-onyx "0.8.12.0-SNAPSHOT"]
```

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

See the docstrings for more information.

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

See the `replica_query.clj` for the full API.

## License

Copyright © 2016 Distributed Masonry

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
