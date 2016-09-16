
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

## User IAM Credentials

Each user is also given a set of credentials (managed through 
[AWS IAM](https://aws.amazon.com/iam/)). They will look something like this:
 
```
aws_access_key: AKIBJHP57RXU4RO...
aws_secret_access_key: 5kZVWNNhf56h4iRC04DyYN3XI5elYO...
```

These credentials are used to authenticate/authorize a specific person to specific action - 
reading, writing, updating schema, etc.
