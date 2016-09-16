# Fineo Client

[![Documentation Status](https://readthedocs.org/projects/fineo-client/badge/?version=latest)](http://fineo-client.readthedocs.io/en/latest/?badge=latest)

## Authentication

### API Key

When you sign up, you will be given an API Key. Keep this save - it ties all requests back to 
your tables/data. If anyone else gets access to this key, they will be able to see you information.

### User IAM Credentials

Each user is also given a set of credentials (managed through 
[AWS IAM](https://aws.amazon.com/iam/)). They will look something like this:
 
```
aws_access_key: AKIBJHP57RXU4RO...
aws_secret_access_key: 5kZVWNNhf56h4iRC04DyYN3XI5elYO...
```

These credentials are used to authenticate/authorize a specific person to specific action - 
reading, writing, updating schema, etc.

## Read

Reads are exclusively supported through a JDBC/ODBC[1] Driver. 

### JDBC

To connect with JDBC, first download the [JDBC driver Jar] or include it via maven in your 
project with:

```
<project>
...
  <dependencies>
    <dependency>
      <groupId>io.fineo.client</groupId>
      <artifactId>jdbc</artifactId>
      <version>1.0.0</version>
    </dependency>
    ...
  </dependencies>
</project>
```

This acts as a 'thin' client to the Fineo servers, which do the heavy-lifting of the JDBC 
requests as a 'fat jar' containing all the necessary dependencies.

#### Simple connection

Fineo has a custom JDBC driver that you specify with the prefix:

```
jdbc:fineo:
```

In Java code, you may need to load the driver with:
```
Class.forName(io.fineo.read.Driver)
```

The full connection string can then be specified as:

```
jdbc:fineo:url:<URL you were given>;[other properties]
```

### Authentication

Properties are either specified on the JDBC URL separated with commas, in the BI user interface 
or through the Java Properties passed in when creating the connection.

The properties you need to set are:

 1. API Key
   - property: ```api_key```
   * example: ```api_key=1234242352```
 2. User IAM credentials
  * property: ```authentication```
  * Options:
    * DEFAULT
      * Use the [default AWS credential chain](http://docs.aws.amazon.com/java-sdk/latest/developer-guide/credentials.html)
      *  property necessary
    * system
      * Load from the system properties:
        * aws.accessKeyId
        * aws.secretKey
    * env
      * Load from the environment variables:
        * AWS_ACCESS_KEY_ID / AWS_ACCESS_KEY
        * AWS_SECRET_KEY / AWS_SECRET_ACCESS_KEY
    * static
      * sub-property: aws_key
      * sub-property: aws_secret
    * profile
      * sub property: profile_name=<name>
      * Loads the specified profile from ~/.aws/credentials

You can also provision multiple authentcation types with the ```_OR_``` separator to set a 
hierarchy of types. For example:

 * Load from just system properties:
 
```authentication=system```

 * Load from system properties or profile properties, specifing a profile name
 
 ```authentication=system_OR_profile,profile_name=my-profile```


All requests must have **both an API Key and IAM User credentials**.

### Tuning

You find information on tuning the client connection at our [tuning guide](client/jdbc-tuning)

## Writes & Schema

Writes and Schema are managed through their own RESTful APIs.

The swagger specifications are available here:
 * [write](http://api.fineo.io/?spec=write.json)
 * [schema](http://api.fineo.io/?spec=schema)

However, you still need to authenticate with an API Key and [sign the request]
(http://docs.aws.amazon.com/general/latest/gr/signing_aws_api_requests.html) 
with your IAM Credentials. As such, you can try the API using [Postman](https://www.getpostman.com/)
 or, more simply, directly using our [java client](client/java-client.

### Batch Uploads

Batch data uploads can take two forms:

 1. Sending a batch of events
 2. Sending a reference to an S3 file

The latter has some slight requirements around it. Unless your S3 bucket is publicly accessible 
(not recommended), you need to add the [fineo bucket policy](batch-upload-bucket-policy.json) to 
the bucket so we can read the data you send.
 
Currently, S3 file uploads support the following types:

  * csv
  * json
    * each record must be on a single line and not prefixed with ```[``` or followed by ```]```. 
    This is _not_ a well-formed JSON file.

With the following compression formats:

  * none
  * gzip (.gz)

## Notes

[1] ODBC is planned on the roadmap, but not yet implemented.

[JDBC Driver Jar] : http://maven.fineo.io.s3-website-us-east-1.amazonaws.com
