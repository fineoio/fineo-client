
There are several limitations/restrictions on the platform. All other limitations are explicitly documented in the specific APIs

# Client

 * java >= 1.8u101

# Events

 1. 500 events/sec
 2. Individual event < 375KB

## Streaming
 1. 1MB per message

## Functionality

1. No repeat alias names within a parent.
	*  e.g. no repeat metric aliases, no repeat field names in a metric. Otherwise, there is no way to tell metrics/fields apart.

2. No new field names: _f*, companykey, metrictype, timestamp, T<n>¦¦
3. (Currently) No lookup of columns by their original ingest names, only their output names

## SQL
1. No multiple * selection, e.g. *, field, *. You can do *, f1, f2,..., but the second * is not supported.

