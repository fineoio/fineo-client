Generally, the default properties work pretty well.

# Authentication

*If you have a custom access key/secret access*, rather than the standard username/password
combination *and* your login path is not working correctly, then you can set:

 * force_auth
    * Options:
      * username_password
        * Assume this credential is a username/password combination
      * key
        * Assume this credential is a access/secret combination

# Performance

However, if you want to maximize the throughput, there are a few parameters that you can tune
through the jdbc client:

  * client_max_connections
    * maximum number of allowed open HTTP connections
  * client_init_connection_timeout_millis
    * time initially establishing a connection before giving up and timing out.
  * client_request_timeout_millis
    * time for the request to complete before giving up and timing out.
  * client_request_max_retries
    * number of retries for a single HTTP request before giving up


If you find these are not sufficient, please [let us know](mailto:help@fineo.io).
