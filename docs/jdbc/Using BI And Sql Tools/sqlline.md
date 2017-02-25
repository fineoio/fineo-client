You can connect to Fineo through SqlLine - a standard console based SQL access tool.

# How To

## 1. Download

The Fineo-specific build of sqlline is available [here] [[1](#1-sqlline)].

## 2. Prepare

Unpack the tarball and then copy in the desired fineo jdbc jar (see [jdbc](/jdbc/intro) for the jar) to
 `lib/`. If you want logging you can also copy in you desired logging framework (Fineo leverages 
 Slf4j everywhere); we recommend using [logback](http://logback.qos.ch/) [[2](#2-logback)].
 
## 3. Start
 
Now you are ready to start the sqlline shell:

```
$ ./sqlline
```

## 4. Connect!

Start by entering the connection credentials:
```
sqlline>  !connect jdbc:fineo:api_key=<your api key>
```

If you did not enter a authentication method (see [JDBC guide](/jdbc/intro)), you can enter your IAM
 credentials as a username (access key) and password (secret key) combination:

```
 Enter username for jdbc:fineo;api_key=<key>:
 Enter password for jdbc:fineo:api_key=<key>:
```

That's it. From there you can easily access all of your data through SQL!

## 5. Examples

Here are some simple things you can do in Sqlline.

### List tables

```
sqlline> !tables
```

### Get all rows in a table

```
sqlline> SELECT * FROM <table name>
```

## 6. Exiting

Crtl-C (the usual exit option) only cancels the previous command. Instead you need to use:

```
sqlline> !quit
```

### Notes

#### 1. Sqlline
We include a couple of fixes, like loading the properties file correctly and using lib/ vs. 
changing the java-ext dir. For more details, see [our fork](https://github.com/fineo-io/sqlline).

#### 2. Logback
For logback, the jars you need are: [logback-core] and [logback-classic]. Download these jars and add them to the `lib/` directory. 

[here]: http://maven.fineo.io/release/sqlline/sqlline/1.1.10-fineo-1.1/sqlline-1.1.10-fineo-1.1.tar.gz
[logback-core]: http://central.maven.org/maven2/ch/qos/logback/logback-core/1.1.7/logback-core-1.1.7.jar
[logback-classic]: http://central.maven.org/maven2/ch/qos/logback/logback-classic/1.1.7/logback-classic-1.1.7.jar
