You can connect to Fineo through SqlLine - a standard console based SQL access tool.

# How To

## 1. Download

The Fineo-specific build is available [here](http://maven.fineo.io/release/sqlline/sqlline/1.1.10-fineo-1.0/sqlline-1.1.10-fineo-1.0.tar.gz)

## 2. Start

Once you unpack the tarball, you can just run sqlline directly from the unpacked directory:

```
$ ./sqlline
```

## 4. Connect!

Start by entering the connection credentials:
```
sqlline>  !connect jdbc:fineo:url=<URL you were given>;api_key=<your api key>
```

If you did not enter a authentication method (see [JDBC guide](/Jdbc)), you can enter your IAM
 credentials as a username (access key) and password (secret key) combination:

```
 Enter username for jdbc:fineo:url=<url>;api_key=<key>:
 Enter password for jdbc:fineo:url=<url>;api_key=<key>:
```

That's it. From there you can easily access all of your data through SQL!
