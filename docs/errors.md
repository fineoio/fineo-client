When events are written to the Stream API ([stream]) they are put into a scalable queue for processing. This generates a little bit of lag (e.g. eventual consistency), but enables incredibly scalable storage and flexible schema evolution.

The only error returned from the Strema API occurs if the event could not be added to the queue. This is very rare and can be solved with a retry.

However, if the event itself is malformed (e.g. missing a metric name or timestamp) an error will not immediately be returned. Instead, within **5 minutes** an "error record" will be written to the `errors.stream` table.

# Errors Panel

The [Fineo web application] makes it easy to view all the errors that have occured. You can filter on timerange as well as the content of the messages.

Under the hood, the Error Panel is just a light wrapper around the `errors.stream` table that handles all the complexity of paging results and filtering time ranges. See below for accessing the `errors.stream` table natively through JDBC.

# Error.Stream Table

The `error.stream` table is a special JDBC table that stores the events that could not be processed, when each was handled, the processing stage where the error occured and the reason for the error (among other elements).

Because this is just another Fineo JDBC table you can query it like another Fineo table (see [jdbc] documentation for more information).

There are a couple of helpful fields that can be used to minimize the number of results returned: `year`, `month`, `day`, and `hour`. Each failed record also contains a field, `handled_timestamp`, the number of milliseconds since the epoch (00:00:00 UTC on 1 January 1970).

## SqlLine Example

Query for all the errors:

```
0: jdbc:fineo:api_key=1234> SELECT * from errors.stream;
```

Events that were handled as of specific timestamp:

```
0: jdbc:fineo:api_key=1234> SELECT * from errors.stream WHERE handled_timestamp > 1490638685520;
```

The above query will search all the possible error records for the specified timestamp, making it very slow. If you experience timeouts, it is recommended to apply filters for `year`, `month`, `day` and `hour`, if possible (at the very least `>=`). The Error Panel discussed above takes care of this for you, if you are using the web app.

```
0: jdbc:fineo:api_key=1234> SELECT * from errors.stream WHERE handled_timestamp > 1490638685520 AND year >=2017 AND month >= 3 AND day >= 25 AND hour >= 5;
```


[stream]: /write
[jdbc]: /jdbc/intro
[Fineo web application]: https://app.fineo.io
