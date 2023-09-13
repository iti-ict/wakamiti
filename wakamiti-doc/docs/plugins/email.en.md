---
title: Email
date: 2023-08-04
slug: /en/plugins/email
---

This plugin allows to check the status of a e-mail store server, verify the 
number of unread messages and intercept new incomming messages. It also allows to 
check fields of the last incomming message such as subjet, sender, body, and 
attachments.

This plugin should be used along others to form complete scenarios, e.g., for checking
that the application sends emails as a consequence of some other operation such a REST request.


Coordinates
----------------------------------------------------------------------------------------------------

### Wakamiti configuration file

```yaml
wakamiti:
  launcher:
    modules:
        - es.iti.wakamiti:email-wakamiti-plugin:1.0.0
```

### Maven

```
  <dependency>
    <groupId>es.iti.wakamiti</groupId>
    <artifactId>email-wakamiti-plugin</artifactId>
    <version>1.0.0</version>
  </dependency>
```




Configuration
----------------------------------------------------------------------------------------------------


### `email.store.address`

Address of the user of the mail server, used as login credentials

Example:
```yaml
email:
  address: test@localhost
```

Since: 1.0.0

---

### `email.password`

Password of the user of the mail server, used as login credentials
Example:
```yaml
email:
  password: xjlk4324
```

Since: 1.0.0


---

### `email.store.host`

Host name or IP address where the mail store server is hosted

Example:
```yaml
email:
  store:
    host: imap.gmail.com
```

Since: 1.0.0


---

### `email.store.port`

Port to access to the mail store (it varies according the protocol)

Example:
```yaml
email:
  port: 993
```

Since: 1.0.0


---


### `email.store.protocol`

Protocol used by the mail store

Example:
```yaml
email:
  store:
    protocol: imap
```

---

### `email.store.folder`

Name of the folder to test in the mail store
Example:
```yaml
email:
  store:
    folder: INBOX
```

Since: 1.0.0




Steps
----------------------------------------------------------------------------------------------------


---
### Define the mail store server to use
```
the email server at {host:text}:{port:int} using the protocol {protocol:word}
```
#### Parameters:
| name       | wakamiti type | description                                 |
|------------|---------------|---------------------------------------------|
| `host`     | `text`        | IP or name of the mail store                |
| `port`     | `int`         | Port of the mail store (according protocol) |
| `protocol` | `word`        | Protocol of the mail store                  |

#### Examples:
```gherkin
  Given the email server at 'imap.gmail.com':993 using the protocol imap
```

Since: 1.0.0


---
### Define the mail user credentials
```
the email user with address {address:text} and password {password:text}
```
#### Parameters:
| name       | wakamiti type | description       |
|------------|---------------|-------------------|
| `address`  | `text`        | The email address |
| `password` | `text`        | The user password |

#### Examples:
```gherkin
  Given the email user with address 'john@mymail.com' and password 'daDjkl3434S'
```

Since: 1.0.0


---
### Define the mail store folder to use
```
the email folder {text}
```
#### Parameters:
| wakamiti type  | description                 |
|----------------|-----------------------------|
| `text`         | The name of the mail folder |

#### Examples:
```gherkin
  Given the email folder 'INBOX'
```

Since: 1.0.0


---
### Verify the number of unread emails
```
(that) the number of unread emails {integer-assertion}
```
#### Parameters:
| wakamiti type       | description                                              |
|---------------------|----------------------------------------------------------|
| `integer-assertion` | The validation to apply to the number of unread messages |
#### Examples:
```gherkin
  Given that the number of unread emails is greater than 0
```

Since: 1.0.0


---
### Verify that a new mail is received in a given interval
```
(that) a new email is received within {sec:integer} seconds
```
#### Parameters:
| name       | wakamiti type | description                                       |
|------------|---------------|---------------------------------------------------|
| `sec`      | `integer`     | Number of seconds to wait for an incoming message |

#### Examples:
```gherkin
  Then a new email is received within 5 seconds
```

Since: 1.0.0


---
### Verify the subject of the last email
```
(that) the subject of the email {text-assertion}
```
#### Parameters:
| wakamiti type    | description                                         |
|------------------|-----------------------------------------------------|
| `text-assertion` | The validation to apply to the subject of the email |
#### Examples:
```gherkin
  Then the subject of the email starts with 'Issue opened'
```

Since: 1.0.0


---
### Verify the sender of the last email
```
(that) the sender of the email {text-assertion}
```
#### Parameters:
| wakamiti type    | description                                        |
|------------------|----------------------------------------------------|
| `text-assertion` | The validation to apply to the sender of the email |
#### Examples:
```gherkin
  Then the sender of the email is 'support@company.com'
```

Since: 1.0.0


---
### Verify the body contents of the last email
```
(that) the body of the email is:
```
#### Parameters:
| wakamiti type | description            |
|---------------|------------------------|
| `document`    | The content to compare |
#### Examples:
```gherkin
  Then the body of the email is:
  """
     Hello,
     Your issue has been received.
     Regards
  """
```

Since: 1.0.0


---
### Verify partially the body contents of the last email
```
(that) the body of the email contains:
```
#### Parameters:
| wakamiti type | description            |
|---------------|------------------------|
| `document`    | The content to compare |
#### Examples:
```gherkin
  Then the body of the email contains:
  """
     Your issue has been received.
  """
```

Since: 1.0.0


---
### Verify the body contents of the last email against an external file
```
(that) the body of the email is the content of the file {file}
```
#### Parameters:
| wakamiti type | description                 |
|---------------|-----------------------------|
| `file`        | Path of the file to compare |
#### Examples:
```gherkin
  Then the body of the email is the content of the file 'email.txt'
```


Since: 1.0.0


---
### Verify the body contents of the last email contains the content of an external file
```
(that) the body of the email contains the content of the file {file}
```
#### Parameters:
| wakamiti type | description                 |
|---------------|-----------------------------|
| `file`        | Path of the file to compare |
#### Examples:
```gherkin
  Then the body of the email contains the content of the file 'email.txt'
```


Since: 1.0.0


---
### Verify the number of attachments of the last email
```
(that) the number of attachments in the email {integer-assertion}
```
#### Parameters:
| wakamiti type       | description                                          |
|---------------------|------------------------------------------------------|
| `integer-assertion` | The validation to apply to the number of attachments |
#### Examples:
```gherkin
  Then the number of attachments in the email is less than 2
```


Since: 1.0.0


---
### Verify that the email contains an attached file with certain name
```
(that) the email has an attached file whose name {text-assertion}
```
#### Parameters:
| wakamiti type    | description                                               |
|------------------|-----------------------------------------------------------|
| `text-assertion` | The validation to apply to the name of the attached files |
#### Examples:
```gherkin
  Then the email has an attached file whose name is 'attach.txt'
```


Since: 1.0.0


---
### Verify the binary content of an attached file
```
(that) the email has an attached file with the content of the binary file {file}
```
#### Parameters:
| wakamiti type | description                 |
|---------------|-----------------------------|
| `file`        | Path of the file to compare |
#### Examples:
```gherkin
  Then the email has an attached file with the content of the binary file 'attach.dat'
```


Since: 1.0.0



---
### Verify the text content of an attached file
```
(that) the email has an attached file with the content of the text file {file}
```
#### Parameters:
| wakamiti type | description                 |
|---------------|-----------------------------|
| `file`        | Path of the file to compare |
#### Examples:
```gherkin
  Then the email has an attached file with the content of the text file 'attach.txt'
```


Since: 1.0.0


---
### Verify the text content of an attach file against the following text
```
(that) the email has an attached file with the following content:
```
#### Parameters:
| wakamiti type | description             |
|---------------|-------------------------|
| `document`    | Text to compare against |
#### Examples:
```gherkin
  Then the email has an attached file with the following content:
  """
  This is an attached content
  """
```


Since: 1.0.0


---
### Delete all emails from a given sender (clean-up operation)
```
At finish, delete all emails whose sender {text-assertion}
```
#### Parameters:
| wakamiti type    | description                                 |
|------------------|---------------------------------------------|
| `text-assertion` | The validation to apply to the email sender |
#### Examples:
```gherkin
  Background:
    * At finish, delete all emails whose sender is 'test@localhost'
```


Since: 1.0.0


---
### Delete all emails with a given subject (clean-up operation)
```
At finish, delete all emails whose subject {text-assertion}
```
#### Parameters:
| wakamiti type    | description                                  |
|------------------|----------------------------------------------|
| `text-assertion` | The validation to apply to the email subject |
#### Examples:
```gherkin
  Background:
    * At finish, delete all emails whose subject starts with 'Testing'
```


Since: 1.0.0
