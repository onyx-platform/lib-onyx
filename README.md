# lib-onyx

An Onyx library with a collection of functions and idioms that are useful for batch and streaming workflows.

## Usage

### In Memory Joins

Performs a streaming join on a sequence of a segments. Uses an atom that maintains a hashmap to retain unmatched values, and emits segments with `:lib-onyx.join/by` keyword in common.

#### Catalog Entry

```clojure
{:onyx/name :join-segments
 :onyx/ident :lib-onyx.join/join-segments
 :onyx/fn :lib-onyx.join/join-segments
 :onyx/type :function
 :onyx/consumption :concurrent
 :lib-onyx.join/by :id
 :onyx/batch-size batch-size
 :onyx/doc "Performs an in-memory streaming join on segments with a common key"}
```

#### Workflow Example

```clojure
(def workflow
  [[:input-1 :join-segments]
   [:input-2 :join-segments]
   [:join-segments :output]])
```

### Requeue and Bounded Retry

Requeues and retries segments when their corresponding functions that exceptions. This function is parameterizes to retry at most a bounded number of times. Retry will requeue the segment *at the back* of the ingress queue for that task.

#### Catalog Entry

```clojure
{:onyx/name :exciting-name
 :onyx/ident :lib-onyx.join/requeue-and-retry
 :onyx/fn :lib-onyx.retry-test/exciting-name
 :onyx/type :function
 :onyx/consumption :concurrent
 :lib-onyx.retry/n 3
 :onyx/batch-size batch-size
 :onyx/doc "Requeues segments that throw exceptions, at most :lib-onyx.retry/n times"}
```

#### Workflow Example

```clojure
(def workflow {:in {:exciting-name :out}})
```

#### Notes

Function implementations surround themselves with a call to `retry/retry-on-failure`. This function takes 3 parameters: the function to invoke with a segment, a producing function, and a segment. The latter two are provided by lib-onyx.

```clojure
(defn exciting-name-impl [{:keys [name] :as segment}]
  (when (.startsWith name "X")
    (throw (ex-info "Name started with X" {:reason :x-name :segment segment})))
  {:name (str name "!")})

(defn exciting-name [produce-f segment]
  (retry/retry-on-failure exciting-name-impl produce-f segment))
```

## License

Copyright © 2014 Michael Drogalis

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
