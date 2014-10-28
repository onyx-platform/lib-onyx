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

## License

Copyright © 2014 Michael Drogalis

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
