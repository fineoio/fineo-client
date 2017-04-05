The java client is the easiest way to manipulate schema and write to the API. 

# Requirements

 * maven 3
 * java >= 8u101

# Usage

The basic java client is available through Maven here:

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
      <artifactId>common</artifactId>
      <version>|version|</version>
    </dependency>
    ...
  </dependencies>
</project>
```

From there, you can choose which module you want to use:

  * io.fineo.client.models:schema
  * io.fineo.client.models:write
  
Each module provides a basic interface of the API which you combine with a builder to create the 
instance to call. For instance, to use the 'StreamWrite' API:

```
  FineoClientBuilder builder = new FineoClientBuilder()
                                .withApiKey(apiKey)
                                .withCredentials(awsCredentials);
  StreamWrite stream = builder.build(StreamWrite.class)
```

All the Java APIs support a synchronous and an asynchronous version of each method.

## Credentials

When setting up credentials with the API, create an form of AWS Credentials. The easiest way is with the ```CredentialsHelper```:

```
 AWSCredentialsProvider provider = CredentialsHelper.getHelper(key, secret);
```

The ```CredentialsHelper``` will look at the 'shape' of the credentials and attempt to provide the best ```AWSCredentialsProvider```. The key and secret provided to the `CredentialsHelper` can either be a Device key/secret combination (obtained from the 'Devices' section of the web application) or it can be the email/password combination used to sign uo for Fineo.  

However, to make it even easier to get credentials, Fineo supports any form of `AWSCredentialsProvider`, a few are:

  * [StaticCredentialProvider]
  * [EnvironmentCredentialProvider]
  * [ProfileCredentialProvider]
  * [DefaultCredentialProvider]

[Read more about AWS Credential Providers](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/AWSCredentialsProvider.html)


[StaticCredentialProvider]: http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/AWSStaticCredentialsProvider.html
[EnvironmentCredentialProvider]: http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/EnvironmentVariableCredentialsProvider.html 
[ProfileCredentialProvider]: http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/profile/ProfileCredentialsProvider.html
[DefaultCredentialProvider]: http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/DefaultAWSCredentialsProviderChain.html
