
There are several limitations/restrictions on the platform. All other limitations are explicitly documented in the specific APIs

# Client

 * java >= 1.8u101

# Devices

 * Max 2 keys at any time, per device

# Events

 1. 200 events/sec, with bursts to 500 events/sec
 2. Individual event < 375KB

**NOTE**: A request can be a either a single stream event, a list of events to the `stream/events` 
endpoint, or a batch request.

## Streaming
 1. 1MB per message
 
## Batch
 1. 5MB per message
    - Does not apply to S3 uploaded batches

## Functionality

1. No repeat alias names within a parent.
	*  e.g. no repeat metric aliases, no repeat field names in a metric. Otherwise, there is no way to tell metrics/fields apart.

2. Disallowed field names:
    * companykey
    * metrictype
    * timestamp
    * Anything with the prefix:
      * _f
      * ```T<n>¦¦```
        * where ```<n>``` is a number

3. (Currently) No lookup of columns by their original ingest names, only their output names

## SQL
1. No multiple * selection.
  * For example, ```*, field, *```. You can do ``*, f1, f2,...```, but the second * is not supported.
