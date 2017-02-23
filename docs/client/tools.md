The client library comes with a set of tools to make it fast and easy to interact with the Fineo 
API.

# Schema

The Schema tool allows you to specify a POJO (plain old Java object) for the schema of the table 
(e.g. metric). This lets you easily specify fields and their types with just a little Java code 
and a couple of lines on the command line.
  
The Schema tool comes bundled with a simple metric type named 'metric' with one VARCHAR column 
'field'.
  
# Stream

The Stream tool lets you easily upload events to the 'stream' API endpoint, either as a single 
event at a time (sequential mode) or in batches of events (default). You specify the type of the 
event that you used when creating the schema (above) and then sequentially specify the fields in 
the event.

# SqlToJson

The SqlToJson tool provides helps you make SQL queries and then outputs the results into JSON 
formatted text. If you are looking to investigate your data or dig into it deeply, you are likely 
better off using [SqlLine](/jdbc/sqlline). However, SqlToJson is useful if you are just getting 
started.

# Using the tools

You can get the tools [here]. 

## Schema

To create the bundled metric type into a table named 'metric', you run: 

```
$ java -cp tools-<version>.jar io.fineo.client.tools.Schema \
  --api-key <your api key> \
  --username <your email> --password <your password> \
  create --type metric
```

If you wanted to name the table something different, you could also specify `--metric-name`. 
Otherwise, we just use the type you specify. Note that if you have your own type, you could 
create a metric type like:

```
$ java -cp tools-<version>.jar;my-company-files.jar io.fineo.client.tools.Schema \
  --api-key <your api key> \
  --username <your email> --password <your password> \
  create \
  --type com.company.Event \
  --metric-name my-events
```

Alternatively, you can also create schema entirely based on the typing your specify on the 
command line. For example:

```
$ java -cp tools-<version>.jar;my-company-files.jar io.fineo.client.tools.Schema \
  --api-key <your api key> \
  --username <your email> --password <your password> \
  create \
  --metric-name my-events2 \
  -Ffield1=VARCHAR -Ffield2=INTEGER
```

Would create a metric named 'my-events2' with two fields: field1(varchar) and field2(integer). 
Note that because we are not specifying a type, we must specify a metric name because there is no
 default value we could create in its place. 

### Reading schema

You can also read the schema for a given table with the `read` command. For instance, to read the
 schema of the metric table that we create above, you would run:
  
```
$ java -cp tools-<version>.jar io.fineo.client.tools.Schema \
  --api-key <your api key> \
  --username <your email> --password <your password> \
  read --metric-name metric
```

which would output a JSON object like:

```
{
  "name":"metric",
  "aliases":[],
  "timestampPatterns":[],
  "fields":[
    {"name":"field","aliases":[],"type":"STRING"},
    {"name":"timestamp","aliases":[],"type":"LONG"},
  ]
}
```

### Deleting a metric

You can also delete a table (e.g. metric) with the `delete` command like so: 

```
$ java -cp tools-<version>.jar io.fineo.client.tools.Schema \
  --api-key <your api key> \
  --username <your email> --password <your password> \
  delete --metric-name metric
```

## Stream

The same event type that you created above can be reused with the Stream tool. If you are just 
sending a single event, you can easily specify each field:

```
$ java -cp tools-<version>.jar;my-company-files.jar io.fineo.client.tools.Stream \
  --api-key <your api key> \
  --username <your email> --password <your password> \
  --type metric \
  --field field.value
```

The tool sets the timestamp is set to the time the event is written, so you don't need to specify
 it explicitly (unless you want to).
 
 If you want to specify multiple events, you need to specify the index of the event to which each
  field belongs. Going back to the built in 'metric' type, you can specify two events like this:

```
$ java -cp tools-<version>.jar;my-company-files.jar io.fineo.client.tools.Stream \
  --api-key <your api key> \
  --username <your email> --password <your password> \
  --type metric \
  --field "1.field.a value"
  --field "2.field.another value"
```

Which will create two events and send them to the stream API (in json format):

```
[{
  "metrictype": "metric",
  "field" : "a value",
  "timestamp": <now>
},
{
  "metrictype": "metric",
  "field" : "another value",
  "timestamp": <now>
}]
```

## Timestamp Validation

Before sending events to the Fineo API, you should check that your timestamp parse as you expect.
 For this, we have the `TimestampValidation` tool that parses the timestamp _exactly as it would 
 on the server-side_ so you can be sure things are working as expected.

```
$  java -cp tools-<version>.jar;my-company-files.jar io.fineo.client.tools.TimestampValidation \
    --pattern <pattern> \
    --value <column value>
```

For example, you could check:

 * --pattern "uuuu-MM-dd HH:mm:ss.SSS@ UTC-8"
 * --value "2016-01-21 15:32:12.345"

Which would output something like (if you are in the PST timezone):

```
Timestamp parsed to Unix epoch: 1453415532345
Which is 
 ISO_INSTANT: 2016-01-21T22:32:12.345Z
 Local:       2016-01-21T14:32:12.345-08:00[America/Los_Angeles]
```

TimestampValidation is a local tool, so it does not need a URL or Api Key.

## SqlToJson

SqlToJson is stored in its [own jar] which includes all the JDBC connection management on top of 
the usual Fineo connection libraries.

Suppose you just want to read all the rows in the 'metric' table that you created and wrote to 
above:

```
$ java -jar tools-<version>-jdbc.jar \
    --api-key <your api key> \
    --username <your email> --password <your password> \
    SELECT field from metric
```

You can also specify the SQL query to make with the `--sql` option (helpful for using '*' 
matching and avoiding the shell expansion).

# Authentication

All requests must include an API KEY. Additionally, you must specify an AWS access key and 
secret key (to sign requests). There are multiple ways to specify credentials in the tools.

You can either specify the credentials in a YAML file, like:

```
access_key_id: <key>
secret_access_key: <secret>
```

Alternatively, you can also specify the credentials through command line properties statically or as
 an AWS profile. See `help` option for more information on how to specify the credentials
 
# Additional options

All tools have `-h` or `--help` options that fully describe all parameters that can used for each
 command.

[here]: http://maven.fineo.io/release/io/fineo/client/tools/1.1.1/tools-1.1.1.jar
[own jar]: http://maven.fineo.io/release/io/fineo/client/tools/1.1.1/tools-1.1.1-jdbc.jar
