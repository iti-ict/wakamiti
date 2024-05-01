---
title: Email
date: 2023-08-04
slug: /en/plugins/email
---


This plugin allows you to check the status of folders on a mail server, check the number of unread messages and 
intercept new incoming messages. It also allows you to validate fields of the last message such as subject, sender, body 
and attachments.

This plugin is designed to be used in conjunction with other plugins to create complete scenarios. For example, to 
validate that an application is sending mail as a result of another operation such as a REST request.


---
## Tabla de contenido

---


## Install


Include the module in the corresponding section.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:email-wakamiti-plugin:1.4.0
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>email-wakamiti-plugin</artifactId>
  <version>1.4.0</version>
</dependency>
```


## Options


### `email.address`
- Type: `string` *required*

User's email address, to be used as login credentials.

Example:
```yaml
email:
  address: test@localhost
```


### `email.password`
- Type: `string` *required*

User password, to be used as login credentials.

Example:
```yaml
email:
  password: xjlk4324
```


### `email.store.host`
- Type: `string` *required*

Host name or IP address where the mail server store is located.

Example:
```yaml
email:
  store:
    host: imap.gmail.com
```


### `email.store.port`
- Type: `integer` *required*

Port for accessing the mail server store (usually varies depending on the protocol).

Example:
```yaml
email:
  store:
    port: 993
```


### `email.store.protocol`
- Type: `string` *required*

Protocol used by the mail store.

Example:
```yaml
email:
  store:
    protocol: imap
```


### `email.store.folder`
- Type: `string` *required*

Name of the folder to be scanned in the mail store.

Example:
```yaml
email:
  store:
    folder: INBOX
```


## Steps


### Define the mail store server to use
```text copy=true
the email server at {host}:{port} using the protocol {protocol}
```

#### Parameters:
| name       | wakamiti type        | description                                 |
|------------|----------------------|---------------------------------------------|
| `host`     | `text` *required*    | IP or name of the mail store                |
| `port`     | `integer` *required* | Port of the mail store (according protocol) |
| `protocol` | `word` *required*    | Protocol of the mail store                  |

#### Examples:
```gherkin
Given the email server at 'imap.gmail.com':993 using the protocol imap
```


### Define the mail user credentials
```text copy=true
the email user with address {address} and password {password}
```

#### Parameters:
| name       | wakamiti type     | description       |
|------------|-------------------|-------------------|
| `address`  | `text` *required* | The email address |
| `password` | `text` *required* | The user password |

#### Examples:
```gherkin
Given the email user with address 'john@mymail.com' and password 'daDjkl3434S'
```


### Define the mail store folder to use
```text copy=true
the email folder {text}
```

#### Parameters:
| name   | wakamiti type     | description                 |
|--------|-------------------|-----------------------------|
| `text` | `text` *required* | The name of the mail folder |

#### Examples:
```gherkin
Given the email folder 'INBOX'
```


### Verify the number of unread emails
```text copy=true
(that) the number of unread emails {matcher}
```
#### Parameters:
| name       | wakamiti type                     | description                                              |
|------------|-----------------------------------|----------------------------------------------------------|
| `matcher`  | [integer-assertion][1] *required* | The validation to apply to the number of unread messages |

#### Examples:
```gherkin
Given that the number of unread emails is greater than 0
```


### Verify that a new mail is received in a given interval
```text copy=true
(that) a new email is received within {duration}
```

#### Parameters:
| name       | wakamiti type            | description                          |
|------------|--------------------------|--------------------------------------|
| `duration` | [duration][2] *required* | Time to wait for an incoming message |

#### Examples:
```gherkin
Then a new email is received within 5 seconds
```


### Verify the subject of the last email
```text copy=true
(that) the subject of the email {matcher}
```

#### Parameters:
| name      | wakamiti type                  | description                                         |
|-----------|--------------------------------|-----------------------------------------------------|
| `matcher` | [text-assertion][1] *required* | The validation to apply to the subject of the email |

#### Examples:
```gherkin
Then the subject of the email starts with 'Issue opened'
```


### Verify the sender of the last email
```text copy=true
(that) the sender of the email {matcher}
```

#### Parameters:
| name      | wakamiti type                  | description                                        |
|-----------|--------------------------------|----------------------------------------------------|
| `matcher` | [text-assertion][1] *required* | The validation to apply to the sender of the email |

#### Examples:
```gherkin
Then the sender of the email is 'support@company.com'
```


### Verify the body contents of the last email
```text copy=true
(that) the body of the email is:
    {data}
```

#### Parameters:
| name   | wakamiti type         | description            |
|--------|-----------------------|------------------------|
| `data` | `document` *required* | The content to compare |

#### Examples:
```gherkin
Then the body of the email is:
  """
  Hello,
  Your issue has been received.
  Regards
  """
```


### Verify partially the body contents of the last email
```text copy=true
(that) the body of the email contains:
    {data}
```

#### Parameters:
| name   | wakamiti type         | description            |
|--------|-----------------------|------------------------|
| `data` | `document` *required* | The content to compare |

#### Examples:
```gherkin
Then the body of the email contains:
  """
  Your issue has been received.
  """
```


### Verify the body contents of the last email against an external file
```text copy=true
(that) the body of the email is the content of the file {file}
```

#### Parameters:
| name   | wakamiti type     | description                 |
|--------|-------------------|-----------------------------|
| `file` | `file` *required* | Path of the file to compare |

#### Examples:
```gherkin
Then the body of the email is the content of the file 'email.txt'
```


### Verify the body contents of the last email contains the content of an external file
```text copy=true
(that) the body of the email contains the content of the file {file}
```

#### Parameters:
| name   | wakamiti type     | description                 |
|--------|-------------------|-----------------------------|
| `file` | `file` *required* | Path of the file to compare |

#### Examples:
```gherkin
Then the body of the email contains the content of the file 'email.txt'
```


### Verify the number of attachments of the last email
```text copy=true
(that) the number of attachments in the email {matcher}
```

#### Parameters:
| name       | wakamiti type                     | description                                          |
|------------|-----------------------------------|------------------------------------------------------|
| `matcher`  | [integer-assertion][1] *required* | The validation to apply to the number of attachments |

#### Examples:
```gherkin
Then the number of attachments in the email is less than 2
```


### Verify that the email contains an attached file with certain name
```text copy=true
(that) the email has an attached file whose name {matcher}
```

#### Parameters:
| name      | wakamiti type                  | description                                               |
|-----------|--------------------------------|-----------------------------------------------------------|
| `matcher` | [text-assertion][1] *required* | The validation to apply to the name of the attached files |

#### Examples:
```gherkin
Then the email has an attached file whose name is 'attach.txt'
```


### Verify the binary content of an attached file
```text copy=true
(that) the email has an attached file with the content of the binary file {file}
```

#### Parameters:
| name   | wakamiti type     | description                 |
|--------|-------------------|-----------------------------|
| `file` | `file` *required* | Path of the file to compare |

#### Examples:
```gherkin
Then the email has an attached file with the content of the binary file 'attach.dat'
```


### Verify the text content of an attached file
```text copy=true
(that) the email has an attached file with the content of the text file {file}
```

#### Parameters:
| name   | wakamiti type     | description                 |
|--------|-------------------|-----------------------------|
| `file` | `file` *required* | Path of the file to compare |

#### Examples:
```gherkin
Then the email has an attached file with the content of the text file 'attach.txt'
```


### Verify the text content of an attach file against the following text
```text copy=true
(that) the email has an attached file with the following content:
    {data}
```

#### Parameters:
| name   | wakamiti type         | description            |
|--------|-----------------------|------------------------|
| `data` | `document` *required* | The content to compare |

#### Examples:
```gherkin
Then the email has an attached file with the following content:
  """
  This is an attached content
  """
```


### Delete all emails from a given sender (clean-up operation)
```text copy=true
At finish, delete all emails whose sender {matcher}
```

#### Parameters:
| name      | wakamiti type                  | description                                 |
|-----------|--------------------------------|---------------------------------------------|
| `matcher` | [text-assertion][1] *required* | The validation to apply to the email sender |

#### Examples:
```gherkin
* At finish, delete all emails whose sender is 'test@localhost'
```


### Delete all emails with a given subject (clean-up operation)
```text copy=true
At finish, delete all emails whose subject {matcher}
```

#### Parameters:
| name      | wakamiti type                  | description                                  |
|-----------|--------------------------------|----------------------------------------------|
| `matcher` | [text-assertion][1] *required* | The validation to apply to the email subject |

#### Examples:
```gherkin
* At finish, delete all emails whose subject starts with 'Testing'
```


[1]: en/wakamiti/architecture#comparador
[2]: en/wakamiti/architecture#duration 
