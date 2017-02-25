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
      <version>1.1.2</version>
      <classifier>all</classifier>
    </dependency>
    ...
  </dependencies>
</project>
```

This acts as a 'thin' client to the Fineo servers, which do the heavy-lifting of the JDBC 
requests as a 'fat jar' containing all the necessary dependencies.

# Simple Connection

The Fineo JDBC Driver has two main components.

The Driver class:

```
io.fineo.read.Driver
```

and the JDBC URL. You must also provide your API Key, either as an URL parameter, like:

```
jdbc:fineo:api_key=<your_key>
```

Or in the `Properties` object, when connecting with Java.

The connection string is parsed according to 
[OLE DB Connection String Syntax](https://msdn.microsoft.com/en-us/library/windows/desktop/ms722656\(v=vs.85\).aspx).

# Authentication

All requests are authenticated with [AWS-style SigV4](http://docs.aws.amazon.com/general/latest/gr/signature-version-4.html) signatures and credentials.

There are several ways that the SDK can obtain the credentials. These can be specified either in 
the connection URL properties or in the java Properties object. 

The possible authentication methods are:
 
  - Simple authentication: username/password
      - property: ```username```, ```password```
      - Example:
        * ```jdbc:fineo:api_key=1234;username=jane@example.com;password=secret```
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
              - Example:
                  * ```jdbc:fineo:api_key=1234;authentication=static;aws_key=access;aws_secret=secret```
          * profile
              - sub property: profile_name=```<name>```
              - Loads the specified profile from the `~/.aws/credentials` file

You can also provision multiple authentication types with the ```_OR_``` separator to set a 
hierarchy of types. For example:

 * Load from just system properties:
 
```authentication=system```

 * Load from system properties or profile properties, specifying a profile name
 
 ```authentication=system_OR_profile;profile_name=my-profile```


All requests must have **both an API Key and IAM User credentials**.

# SQL Spec

Currently, we support the same semantics as [Drill SQL](http://drill.apache.org/docs/sql-reference)

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
    String apikey = "yourapikey1234";
    Properties props = new Properties();
    props.put("user", "me@example.com");
    props.put("password", "password");
    try (Connection conn = getConnection(format("jdbc:fineo:api_key=%s", url, apikey), props)) {
      conn.getMetaData().getCatalogs();
    }
  }
}

```

# Tuning

You find information on tuning the client connection at our [tuning guide](/jdbc/tuning)

[JDBC Driver Jar]: http://maven.fineo.io/release/io/fineo/client/jdbc/1.1.2/jdbc-1.1.2-all.jar
[default AWS credential chain]: http://docs.aws.amazon.com/java-sdk/latest/developer-guide/credentials.html
