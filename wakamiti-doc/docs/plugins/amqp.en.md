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

<br />

---
## Tabla de contenido

---
<br />


## Install

---

Include the module in the corresponding section.


```text tabs=coord name=yaml copy=true
es.iti.wakamiti:amqp-wakamiti-plugin:2.4.0
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>amqp-wakamiti-plugin</artifactId>
  <version>2.4.0</version>
</dependency>
```

<br />


## Options

---


###  `amqp.connection.url`
- Type: `string` *required*

Set the URL to be used by the AMQP broker.

Example:

```yaml
amqp:
  connection:
    url: amqp://127.0.0.1:5671
```

<br />

---


###  `amqp.connection.username`
- Type: `string` *required*

Set the username to be used by the AMQP broker.

Example:

```yaml
amqp:
  connection:
    username: guest
```

<br />

---


###  `amqp.connection.password`
- Type: `string` *required*

Set the password to be used by the AMQP broker.

Example:

```yaml
amqp:
  connection:
    password: guest
```

<br />

---


###  `amqp.queue.durable`
- Type: `boolean` 
- Default `false`

Set whether the queue will be durable or not (the queue will survive a server reboot).

Example:

```yaml
amqp:
  queue:
    durable: "true"
```

<br />

---

###  `amqp.queue.exclusive`
- Type: `boolean`
- Default `false`

Set whether the queue will be exclusive (restricted to the current connection).

Example:

```yaml
amqp:
  queue:
    exclusive: "true"
```

<br />

---


###  `amqp.queue.autodelete`
- Type: `boolean`
- Default `false`

Set whether to auto delete queue (will be deleted by server when no longer in use).

Example:

```yaml
amqp:
  queue:
    autodelete: "true"
```

<br />


## Usage

---

This plugin provides the following steps:

### Define connection
```text copy=true
the AMQP connection URL {url} using the user {username} and the password {password}
```

Set the URL and credentials to be used by the AMQP broker. This is the descriptive way of setting the configuration 
properties [`amqp.connection.url`](#amqpconnectionurl), [`amqp.connection.username`](#amqpconnectionusername),
[`amqp.connection.password`](#amqpconnectionpassword).

#### Parameters
| Name       | Wakamiti type     | Description              |
|------------|-------------------|--------------------------|
| `url`      | `text` *required* | The broker URL           |
| `username` | `text` *required* | The credentials username |
| `password` | `text` *required* | The credentials password |

#### Examples:
```gherkin
Given the AMQP connection URL 'amqp://127.0.0.1:5671' using the user 'guest' and the password 'guest'
```

<br />

---


### Define destination queue
```text copy=true
the destination queue {word}
```

Set the name of the queue to watch.

#### Parameters
| Name   | Wakamiti type     | Description  |
|--------|-------------------|--------------|
| `word` | `word` *required* | A queue name |

#### Examples:
```gherkin
  Given the destination queue TEST
```

<br />

---


### Send message to queue
```text copy=true
the following JSON message is sent to the queue {word}:
    {data}
```

Sends a JSON message to the given queue.

#### Parameters
| Name   | Wakamiti type         | Description          |
|--------|-----------------------|----------------------|
| `word` | `word` *required*     | A queue name         |
| `data` | `document` *required* | A JSON message body  |

#### Examples:
```gherkin
When the following JSON message is sent to the queue TEST:
    """json
    {
        "data": {
            "message": "Test message sent"
        }
    }
    """
```

<br />

---


### Send message to queue (file)
```text copy=true
the message from the JSON file {file} is sent to the queue {queue}
```

Sends a JSON message extracted from a local file to the given queue.

#### Parameters
| Name    | Wakamiti type     | Description                         |
|---------|-------------------|-------------------------------------|
| `file`  | `file` *required* | A local file with the JSON message  |
| `queue` | `word` *required* | A queue name                        |

#### Examples:
```gherkin
  When the message from the JSON file 'data/message.json' is sent to the queue TEST
```

<br />

---


### Set pause
```text copy=true
wait for {integer} second(s)
```
Wait a fixed number of seconds (usually to ensure a message has been processed).

#### Parameters
| Name      | Wakamiti type        | Description                 |
|-----------|----------------------|-----------------------------|
| `integer` | `integer` *required* | Amount of time (in seconds) |

#### Examples:
```gherkin
* Wait for 2 seconds
```

<br />

---


### Validate message
```text copy=true
the following JSON message is received within {integer} seconds:
    {data}
```
Validate that a specific JSON message is received in the destination queue, failing after a certain timeout.

#### Parameters
| Name      | Wakamiti type         | Description                 |
|-----------|-----------------------|-----------------------------|
| `integer` | `integer` *required*  | Amount of time (in seconds) |
| `data`    | `document` *required* | A JSON message body         |

#### Examples:
```gherkin
Then the following JSON message is received within 5 seconds:
    """json
      {
        "data": {
          "message": "Test message sent"
        }
      }
    """
```

<br />

---


### Validate message (file)
```text copy=true
the message from the JSON file {file} is received within {seconds} seconds
```

Validate that a specific JSON message is received in the destination queue, failing after a certain timeout.

#### Parameters
| Name      | Wakamiti type        | Description                        |
|-----------|----------------------|------------------------------------|
| `file`    | `file` *required*    | A local file with the JSON message |
| `seconds` | `integer` *required* | Amount of time (in seconds)        |

#### Examples:
```gherkin
Then the message from the JSON file 'data/message.json' is received within 5 seconds
```


