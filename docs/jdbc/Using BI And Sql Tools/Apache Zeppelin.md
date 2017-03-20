[Apache Zeppelin] is a web-based notebook that enables interactive data analytics.Fineo is a standard SQL adapter for Zeppelin and can be used to make beautiful data-driven, interactive and collaborative documents.

Fineo provides standard JDBC connectivity that easily integrates into Zeppelin. This guide has been tested with Zeppelin version 0.6.2.

# Step 0: Install Apache Zeppelin

Go to [Apache Zeppelin] and download the version you want to run. From there follow the [Zeppelin Quickstart Instructions](https://zeppelin.apache.org/docs/0.6.2/install/install.html).

However, we provide an abridged version here. Start by extracting the package:

```
$ tar -xf zeppelin-0.6.2-bin-all.tgz
```

and run the Zeppelin server:

```
$ cd zeppelin-0.6.2-bin-all
$ bin/zeppelin-daemon.sh start
```

If successful, you should be able to view zepplin locally: [http://localhost:8080/#/](http://localhost:8080/#/)

# Step 1: Install the Fineo JDBC driver in Zeppelin

1. Download the latest Fineo JDBC jar from the general [JDBC guide].

2. Open the Interpreter panel.
    * In the top right (user name) -> Interpreter

3. Update the `jdbc` section with the configurations:
    1. Add the jar as a local artifact with the full path on the local filesystem to the Jar.
 ![add artifact](/img/zeppelin/artifact.png)

    2. Add you username and password as `default.username` and `default.password`, respectively. (Hint: Your username is you email address you used to sign up.)

    3. Add your API Key as `default.api_key`

    4. Set the default driver & connection information
        * `default.driver` = io.fineo.read.Driver
        * `default.url` = jdbc:fineo

    5. Disable concurrent use of JDBC connections
        * `zeppelin.jdbc.concurrent.use` = false
        * Fineo connections are very light weight and already use their own underlying pool.

    6. Hit Save.
        * This will restart the interpreter and allow access to the driver in jdbc statements

![jdbc properties](/img/zeppelin/jdbc_properties.png)

# Step 2: Create a new note

1. Select Notebook -> Create New Note, and enter a meaningful title

2. Add a `%jdbc` section

3. Make a simple query for all the data in a table, like:

```
SELECT * FROM server_stats
```

4. Run the note.

 ![simple table](/img/zeppelin/table.png)

Or view some of the data as a chart

![simple chart](/img/zeppelin/chart.png)

NOTE: the first query will take a moment to run as Zeppelin will be launching the interpreter for
 the first time and getting the Fineo credentials token.


[Apache Zeppelin]: https://zeppelin.apache.org/ 
[JDBC guide]: /jdbc/intro
