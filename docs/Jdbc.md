

To connect with JDBC, first download the [JDBC driver Jar] or include it via maven in your 
project with:

```
<project>
...
  <repositories>
    <repository>
      <id>Fineo</id>
      <url>http://maven.fineo.io/release</url>
    </repository>
  </repositories>
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

# Simple connection

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

# Authentication

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
 
 ```authentication=system_OR_profile;profile_name=my-profile```


All requests must have **both an API Key and IAM User credentials**.

# Tuning

You find information on tuning the client connection at our [tuning guide](client/jdbc-tuning)

[JDBC Driver Jar]: http://maven.fineo.io/release/io/fineo/client/jdbc/1.0.0/jdbc-1.0.0.jar
