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
$ java -cp tools-1.1.0-SNAPSHOT.jar io.fineo.client.tools.Schema \
  --api-key <your api key> \
  --url <api> \
  --credentials-file <yaml credentials>
  --type metric
```

If you wanted to name the table something different, you could also specify `--metric-name`. 
Otherwise, we just use the type you specify. Note that if you have your own type, you could 
create a metric type like:

```
$ java -cp tools-1.1.0-SNAPSHOT.jar;my-company-files.jar io.fineo.client.tools.Schema \
  --api-key <your api key> \
  --url <api> \
  --credentials-file <yaml credentials> \
  --type com.company.Event \
  --metric-name my-events
```

## Stream

The same event type that you created above can be reused with the Stream tool. If you are just 
sending a single event, you can easily specify each field:

```
$ java -cp tools-1.1.0-SNAPSHOT.jar;my-company-files.jar io.fineo.client.tools.Schema \
  --api-key <your api key> \
  --url <api> \
  --credentials-file <yaml credentials> \
  --type metric \
  --field field.value
```

The tool sets the timestamp is set to the time the event is written, so you don't need to specify
 it explicitly (unless you want to).
 
 If you want to specify multiple events, you need to specify the index of the event to which each
  field belongs. Going back to built in 'metric' type, you can specify two events like this:

```
$ java -cp tools-1.1.0-SNAPSHOT.jar;my-company-files.jar io.fineo.client.tools.Schema \
  --api-key <your api key> \
  --url <api> \
  --credentials-file <yaml credentials> \
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

## SqlToJson

SqlToJson is stored in its [own jar] which includes all the JDBC connection management on top of 
the usual Fineo connection libraries.

Suppose you just want to read all the rows in the 'metric' table that you created and wrote to 
above:

```
$ java -jar tools-1.1.0-jdbc.jar \
    --api-key <your api key> \
    --url <api> \
    --credentials-file <yaml credentials> \
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
 an AWS profile.
 
# Additional options

All tools have `-h` or `--help` options that fully describe all parameters that can used for each
 command.

[here]: http://maven.fineo.io/release/io/fineo/client/tools/1.1.0/tools-1.1.0.jar
[own jar]: http://maven.fineo.io/release/io/fineo/client/tools/1.1.0/tools-1.1.0-jdbc.jar