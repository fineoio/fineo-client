Schema is not managed through SQL interfaces (yet!), but instead exposed through its own 
RESTful APIs.

The swagger specifications are available [here].

However, you still need to authenticate with an API Key and
[sign the request](http://docs.aws.amazon.com/general/latest/gr/signing_aws_api_requests.html) 
with your IAM Credentials. You can try the API using [Postman](https://www.getpostman.com/). 
However, the easiest access is through our [Java client](client/java-client).

# Schema Flexibility

Schema flexibilty is a key component of what makes Fineo so easy to use. With a single operation 
you can avoid hundreds of lines of 'traditional' ETL that occur when you have schema changes and 
also easily integrate your existing data sources into the Fineo platform.

Additionally, you can continue to support old formats in parallel with new data formats, all at 
the push of a button.

Note that schema is only necessary at read time; you can start writing to the API immediately, 
but only need to provide schema when you read. This also means that you can send new events (e.g.
 testing new formats) and have them ingested without breaking _anything_ - writes will happen 
 normally and existing reads will continue as if those fields did not exist.

## Metric Typing, aka Tables

Fineo SQL tables map to a logical 'metric type',e.g. theromstats. Each table has only one 
'display' name (the name you see when you do ```LIST TABLES``` in a SQL driver. However, you can 
multiple specifications of the logical type in your event. 
 
Lets take the example of working in the 'smart building' industry and tracking thermostats. You 
events might look something like this:
 
```
 {
   "timestamp": 1474045497000,
   "metrictype": "thermostat",
   "temp_f": 75.1
 }
```
 
The ```metrictype``` key is a special key in Fineo that identifies the table to which the event 
should be routed. In this case, to the thermostat table.
 
However, say you have a new thermostat that sends out the name "thermo". Well, you can then just 
add a metric alias for "thermo" to "thermostat" and that new event will get mapped in,

### Metric Type Keys
 
Once you move beyond prototyping with Fineo, its likely that your devices aren't going to be 
sending the field ```metrictype```. In that case, you can leverage _Metric Type Keys_ - alternate
names of fields that can define a metric.
 
Suppose that you have events like:
 
```
 {
   "timestamp": 1474045497000,
   "type": "thermostat",
   "temp_f": 75.1
 }
```
 
 You add a Metric Type Key of 'type', which yields a metric type of 'thermostat' and causes the 
 event to get mapped into the correct table.
 
 Metric Type Keys are globally exposed, so you need to be careful of the order if you have 
 multiple Metric Type Keys as we use the _first matching key_ to determine the metric type.
 
## Timestamps

The simplest event has a ```timestamp``` field with a 'Unix Epoch' value. We realize that legacy 
systems probably don't provide data in this format, so we have a couple of ways to handle this data.

#### Timestamp Patterns

A Timestamp Pattern defines an expected format of the ```timestamp``` field that can be parsed 
into a Unix Epoch time. There are a couple of 'standard' formats that we support out of the box:

  * ISO_DATE_TIME
    * Example: ```2011-12-03T10:15:30```
    * Note: Assumed to be at UTC
  * ISO_OFFSET_DATE_TIME
    * Example: ```2011-12-03T10:15:30+01:00```
    * Note: Same as ISO_DATE_TIME but with an offset from UTC
  * ISO_ZONED_DATE_TIME
    * Example: ```2011-12-03T10:15:30+01:00[Europe/Paris]```
  * ISO_INSTANT
    * Example: ```2011-12-03T10:15:30Z```
    * Notes:
        - Assumed to be at UTC
        - Time to at least the seconds field is required.
        - Fractional seconds from zero to nine are parsed.
  * RFC_1123_DATE_TIME
    * Example: ```Tue, 3 Jun 2008 11:05:30 GMT```

Any of these standard names can be provided as the Timestamp Pattern. Alternatively, you provide 
your own parsing format as defined in at
[Java DateTimeFormatter: Patterns For Formatting and Parsing].  

#### Timestamp Aliases

All fields and metric have the idea of aliases. Above, we saw how 'thermo' is an alias for 
'thermostat'. The only exception to how this works is for the `timestamp` key. Its aliases act 
the same way as the 'Metric Type Key' did for metric names.

Initially, all metrics types (tables) just start out with a single field:

```
  Display Name: timestamp,
  Aliases: [],
  Type: long
```

If you were then to provide an alias for the timestamp field, you could have alternative 
timestmap key names. For example, say you wanted to send the record:
 
```
  {
    "ts": 1474045497000,
    "metrictype": "thermostat",
    "temp_f": 75.1
  }
```

You would add the 'ts' aliases for the timestamp field and then write the record normally. If no 
timestamp is found, the record write will fail, so this is the only schema you have to ensure is 
set _before_ sending a write (as opposed to all other fields).


# Limitations

While we are in BETA, there are a few limitations on schema and its manipulation.

  * No field deletes
  * Disallowed field names:
    * companykey
    * Anything with the prefix:
      * _f
      * ```T<n>¦¦```
        * where ```<n>``` is a number
 
  
[here]: http://api.fineo.io/?url=http://api.fineo.io.s3-website-us-east-1.amazonaws.com/dist/schema-swagger.json
[Java DateTimeFormatter: Patterns For Formatting and Parsing]: https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
