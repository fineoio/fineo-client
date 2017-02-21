
# Simple Connection

The Fineo JDBC Driver has two main components.

The Driver class:

```
io.fineo.read.Driver
```

and the JDBC URL, which is at least:

```
jdbc:fineo:url=https://api.fineo.io/read
```

You must also specify your API Key, either as a URl parameter, like:

```
jdbc:fineo:url=https://api.fineo.io/read;api_key=<your_key>
```

Or in the `Properties` object, when connecting with Java.

The connection string is parsed according to 
[OLE DB Connection String Syntax](https://msdn.microsoft.com/en-us/library/windows/desktop/ms722656\(v=vs.85\).aspx).

Additionally, you must also specify some way to load the request-signing credentials. Similarly, 
this can be done as a connection string property or in the `Properties` object in Java.

# Authentication

Properties are either specified on the JDBC URL separated with commas, in the BI user interface 
or through the Java Properties passed in when creating the connection.

The properties you need to set are:

 1. API Key
    - property: ```api_key```
    - example: ```api_key=1234242352```
 2. User IAM credentials
    - Simple authentication: username/password
      - property: ```username```, ```password```
      - Example:
        * ```jdbc:fineo:url=https://api.fineo.io/read;api_key=1234;username=jane@example.com;password=secret```
        * Note: it is not recommended to pass the password as plain-text in the connection string. 
        Instead, you should store it in the ```Properties``` or enter it in a 'hidden' style field 
    - AWS Credentials 
        -property: ```authentication```
        - Options:
            * default
                - Use the [default AWS credential chain]
            * system
                - Load from the system properties:
                    - aws.accessKeyId
                    - aws.secretKey
            * env
                - Load from the environment variables:
                    - AWS_ACCESS_KEY_ID / AWS_ACCESS_KEY
                    - AWS_SECRET_KEY / AWS_SECRET_ACCESS_KEY
            * static
                - sub-property: aws_key
                - sub-property: aws_secret
            * profile
                - sub property: profile_name=```<name>```
                - Loads the specified profile from ~/.aws/credentials

You can also provision multiple authentication types with the ```_OR_``` separator to set a 
hierarchy of types. For example:

 * Load from just system properties:
 
```authentication=system```

 * Load from system properties or profile properties, specifying a profile name
 
 ```authentication=system_OR_profile;profile_name=my-profile```


All requests must have **both an API Key and IAM User credentials**.

# SQL Spec

Currently, we support the same semantics as [Drill SQL](http://drill.apache.org/docs/sql-reference)

# Java

To connect with JDBC, first download the [JDBC driver Jar] or include it via from the Fineo maven
 repository:

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
      <version>1.1.1</version>
      <classifier>all</classifier>
    </dependency>
    ...
  </dependencies>
</project>
```

This acts as a 'thin' client to the Fineo servers, which do the heavy-lifting of the JDBC 
requests as a 'fat jar' containing all the necessary dependencies.

# Simple connection

You might need to load the class in Java before loading the connection.
```
Class.forName(io.fineo.read.Driver)
```

The full connection string can then be specified as described above. A full example might look like:

```
package io.fineo.read.jdbc;

import io.fineo.read.Driver;
import java.sql.Connection;
import static java.lang.String.format;
import static java.sql.DriverManager.getConnection;

public class Example{

  public void example() throws Exception {
    io.fineo.read.Driver.load();

    String url = "https://api.fineo.io/read";
    String apikey = "yourapikey1234";
    String jdbc = format("jdbc:fineo:url=%s;api_key=%s", url, apikey);
    System.out.println("Connecting with JDBC URL: " + jdbc);
    try (Connection conn = getConnection(jdbc)) {
      conn.getMetaData().getCatalogs();
    }
  }
}

```

# Tuning

You find information on tuning the client connection at our [tuning guide](jdbc/tuning)

[JDBC Driver Jar]: http://maven.fineo.io/release/io/fineo/client/jdbc/1.1.1/jdbc-1.1.1-all.jar
[default AWS credential chain]: http://docs.aws.amazon.com/java-sdk/latest/developer-guide/credentials.html