### Replica Query Server Endpoints

The Replica Query Server has a number of endpoints for accessing the information about a running Onyx cluster. Below we display the HTTP method, the URI, the docstring for the route, and any associated parameters that it takes in its query string.

#### Summary

{{#endpoints}}
- `{{ uri }}`
{{/endpoints}}

{{#endpoints}}
---

##### Route

`[{{ request-method }}]` `{{ uri }}`


##### Query Params Schema

`{{{ query-params-schema }}}`

##### Docstring

{{{ doc }}}

{{/endpoints}}