# Fineo Client

The formal Fineo documentation can be found [here](https://client.fineo.io). This documentation 
is for 'internal' development.
 
## Making a Release

Releases are managed with jgitflow-maven-plugin.

### Start a release

```
$ mvn -f java/pom.xml jgitflow:release-start
```

### Updating Version

In between making a formal release you also need to update the version in the `docs/`. This can 
be done with:

```
$ ./update-doc-version.sh <version>
```

And then committed back.

### Finish release

The internal build handles pushing the jars and updating the latest versions. Push, in this order,

 * master
 * release-<version>

After the release completes, update the master branch onto stable and force the master changes on top of stable (e.g. the pom versions should be [new version]-SNAPSHOT)

```
$ git checkout master
$ git pull origin
$ git rebase stable -s recursive -X theirs
$ git push origin -f
```