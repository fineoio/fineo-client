[Apache Zeppelin] is a web-based notebook that enables interactive data analytics.Fineo is a standard SQL adapter for Zeppelin and can be used to make beautiful data-driven, interactive and collaborative documents.

# Step 1: Install the Fineo JDBC driver in Zeppelin

Fineo provides standard JDBC connectivity that easily integrates into Zeppelin. This guide has been
tested with Zeppelin version 0.6.2.

For general instructions on using the Fineo JDBC driver, see the [JDBC guide].

1. Download the latest Fineo JDBC jar from the general [JDBC guide].

2. Open the Interpreter panel

3. Update the `jdbc` section with the configurations:
 ![add artifact](/img/zeppelin/artifact.png)
    1. Add the jar as a local artifact with the full path on the local filesystem to the Jar.

    2. Add you username and password as `default.username` and `default.password, respectively

    3. Add you API Key as `default.api_key`

    4. Set the default driver to Fineo's driver
        * `default.driver` = io.fineo.read.Driver

    5. Disable concurrent use of JDBC connections
        * `zeppelin.jdbc.concurrent.use` = false
        * Fineo connections are very light weight and already use their own underlying pool.

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


[Apache Zeppelin]: https://zeppelin.apache.org/ 
[JDBC guide]: /jdbc/intro
