---
title: AMQP steps
date: 2022-09-20
slug: /en/plugins/amqp
---


A set of steps to interact with an application via the [Advanced Message Queuing Protocol](https://amqp.org). 
The underlying implementation is based on [RabbitMQ](https://rabbitmq.com), although it might change in further versions.

> **DISCLAIMER**
>
> Currently, this library provides very limited functionality and exists mostly as a proof of concept.

```text tabs=coord name=yaml
es.iti.wakamiti:amqp-wakamiti-plugin:2.3.3
```

```text tabs=coord name=maven
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>amqp-wakamiti-plugin</artifactId>
  <version>2.3.3</version>
</dependency>
```

---
## Table of content

---

---
## Configuration



###  `amqp.connection.url`
Sets the URL to be used by the AMQP broker.

Example:

```yaml
amqp:
  connection:
    url: amqp://127.0.0.1:5671
```

<br /><br />

###  `amqp.connection.username`
Sets the username to be used by the AMQP broker.

Example:

```yaml
amqp:
  connection:
    username: guest
```

<br /><br />

###  `amqp.connection.password`
Sets the password to be used by the AMQP broker.

Example:

```yaml
amqp:
  connection:
    password: guest
```

<br /><br />

###  `amqp.queue.durable`
Sets whether the queue will be durable or not (the queue will survive a server reboot).

Default value is `false`.

Example:

```yaml
amqp:
  queue:
    durable: "true"
```

<br /><br />

###  `amqp.queue.exclusive`
Establece si la cola será exclusiva (restringida a la conexión actual).

Default value is `false`.

Example:

```yaml
amqp:
  queue:
    exclusive: "true"
```

<br /><br />

###  `amqp.queue.autodelete`
Sets whether to auto delete queue (will be deleted by server when no longer in use).

Default value is `false`.

Example:

```yaml
amqp:
  queue:
    autodelete: "true"
```


---
## Steps



### Define connection

```
the AMQP connection URL {url} using the user {username} and the password {password}
```
Sets the URL and credentials to be used by the AMQP broker. This is the descriptive way of setting the configuration 
properties [`amqp.connection.url`](#amqpconnectionurl), [`amqp.connection.username`](#amqpconnectionusername),
[`amqp.connection.password`](#amqpconnectionpassword).

#### Parameters
| Name       | Wakamiti type | Description              |
|------------|---------------|--------------------------|
| `url`      | `text`        | The broker URL           |
| `username` | `text`        | The credentials username |
| `password` | `text`        | The credentials password |

#### Examples:
```gherkin
  Given the AMQP connection URL 'amqp://127.0.0.1:5671' using the user 'guest' and the password 'guest'
```

<br /><br />

### Define destination queue

```
the destination queue {word}
```
Sets the name of the queue to watch.

#### Parameters
| Name   | Wakamiti type | Description  |
|--------|---------------|--------------|
| `word` | `word`        | A queue name |

#### Examples:
```gherkin
  Given the destination queue TEST
```

<br /><br />

### Send message to queue

```
the following JSON message is sent to the queue {word}:
```
Sends a JSON message to the given queue.

#### Parameters
| Name   | Wakamiti type | Description          |
|--------|---------------|----------------------|
| `word` | `word`        | A queue name         |
|        | `document`    | A JSON message body  |

#### Examples:
```gherkin
  When the following JSON message is sent to the queue TEST:
    ```json
    {
        "data": {
            "message": "Test message sent"
        }
    }
    ```
```

<br /><br />

### Send message to queue (file)
```
the message from the JSON file {file} is sent to the queue {queue}
```
Sends a JSON message extracted from a local file to the given queue.

#### Parameters
| Name    | Wakamiti type | Description                         |
|---------|---------------|-------------------------------------|
| `file`  | `file`        | A local file with the JSON message  |
| `queue` | `word`        | A queue name                        |

#### Examples:
```gherkin
  When the message from the JSON file 'data/message.json' is sent to the queue TEST
```

<br /><br />

### Set pause
```
wait for {integer} second(s)
```
Waits a fixed number of seconds (usually to ensure a message has been processed).

#### Parameters
| Name      | Wakamiti type | Description                 |
|-----------|---------------|-----------------------------|
| `integer` | `integer`     | Amount of time (in seconds) |

#### Examples:
```gherkin
  * Wait for 2 seconds
```

<br /><br />

### Validate message

```
the following JSON message is received within {integer} seconds:
```
Validates that a specific JSON message is received in the destination queue, failing after a certain timeout.

#### Parameters
| Name      | Wakamiti type | Description                 |
|-----------|---------------|-----------------------------|
| `integer` | `integer`     | Amount of time (in seconds) |
|           | `document`    | A JSON message body         |

#### Examples:
```gherkin
  Then the following JSON message is received within 5 seconds:
    ```json
      {
        "data": {
          "message": "Test message sent"
        }
      }
```

<br /><br />

### Validate message (file)
```
the message from the JSON file {file} is received within {seconds} seconds
```
Validates that a specific JSON message is received in the destination queue, failing after a certain timeout.

#### Parameters
| Name   | Wakamiti type | Description                        |
|--------|---------------|------------------------------------|
| `file` | `file`        | A local file with the JSON message |
|        | `integer`     | Amount of time (in seconds)        |

#### Examples:
```gherkin
  Then the message from the JSON file 'data/message.json' is received within 5 seconds
```


