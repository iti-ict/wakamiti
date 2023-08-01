---
title: Appium
date: 2023-07-31
slug: /en/plugins/email
---



Configuration
----------------------------------------------------------------------------------------------------


### `email.store.address`

Address of the user of the mail server, used as login credentials

Example:
```yaml
email:
  address: test@localhost
```

---

### `email.password`

Password of the user of the mail server, used as login credentials
Example:
```yaml
email:
  password: xjlk4324
```

---

### `email.store.host`

Host name or IP address where the mail store server is hosted

Example:
```yaml
email:
  store:
    host: imap.gmail.com
```

---

### `email.store.port`

Port to access to the mail store (it varies according the protocol)

Example:
```yaml
email:
  port: 993
```

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




Steps
----------------------------------------------------------------------------------------------------



---
### X
```
x
```
x
#### Examples:
```gherkin
  Given x
```


