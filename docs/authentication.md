
There are two main components to authentication & authorization with Fineo:

 * API Key
 * SigV4 Credentials (aka. AWS Credentials)


# API Key

When you sign up, you will be given an API Key. **Keep this safe** - it ties all requests back to your tables/data. If anyone else gets access to this key, they will be able to see you information and send as you.

If you lose it, you can look it up again on the [Web App - Profile](https://app.fineo.io/#/pages/profile) page.

## Credentials

When you sign up for Fineo through the [web application](https://app.fineo.io/), you specified an email and password. These will be user
 username and password, respectively, and used for signing all requests to the API. This ensures that no one else can masquerade as you. It will also be used as your username/password combination when reading data through [JDBC](/jdbc/intro).

As a user, you have access to all available API endpoints.

### Device Credentials

By default, devices only have access to the 'stream' API ([read more about writing to the stream](/write)).

Device credentials are specified in key/secret style keys that are compliant with the
[AWS IAM](https://aws.amazon.com/iam/) style credentials. They will look something like this:

```
aws_access_key: AKIBJHP57RXU4RO...
aws_secret_access_key: 5kZVWNNhf56h4iRC04DyYN3XI5elYO...
```

and can be easily used with the Fineo SDK.

Please [reach out](mailto:help@fineo.io) if you need to increase API access for your devices.

