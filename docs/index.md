
[![Documentation Status](https://readthedocs.org/projects/fineo-client/badge/?version=latest)](http://fineo-client.readthedocs.io/en/latest/?badge=latest)


This guide provides information about how to access the Fineo API

# Features

 - Fully integrated java client API with synchronous and asynchronous endpoints
 - JDBC compliant access to data
 - In-depth security

# Authentication

## API Key

When you sign up, you will be given an API Key. **Keep this safe** - it ties all requests back to 
your tables/data. If anyone else gets access to this key, they will be able to see you information.

## Credentials

When you sign up through the application, you specified an email and password. These will be user
 username and password, respectively, and used for signing all requests to the API. This ensures 
 that no one else can masquerade as you. 
 
Devices are also given 'access' and 'secret' keys that are compliant with the
[AWS IAM](https://aws.amazon.com/iam/) style credentials. They will look something like this:
 
```
aws_access_key: AKIBJHP57RXU4RO...
aws_secret_access_key: 5kZVWNNhf56h4iRC04DyYN3XI5elYO...
```

These credentials are used to authenticate/authorize a specific device to when making actions - 
reading, writing, updating schema, etc.

**All these credentials should be kept in a safe place.**

# Getting started

Looking to jump right in? Head over to our [getting started] guide.

[getting started]: /getting-started
