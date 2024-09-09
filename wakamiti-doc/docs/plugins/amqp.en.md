---
title: AMQP steps
date: 2022-09-20
slug: /en/plugins/amqp
---


This plugin provides a set of steps to interact with an application via the 
[Advanced Message Queuing Protocol](https://amqp.org). The underlying implementation is based on 
[RabbitMQ](https://rabbitmq.com), although it might change in further versions.

> **DISCLAIMER**
>
> Currently, this library provides very limited functionality and exists mostly as a proof of concept.


---
## Tabla de contenido

---


## Install


Include the module in the corresponding section.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:amqp-wakamiti-plugin:2.6.0
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>amqp-wakamiti-plugin</artifactId>
  <version>2.6.0</version>
</dependency>
```


## Options


###  `amqp.connection.url`
- Type: `string` *required*

Sets the URL to be used by the AMQP broker.

Example:

```yaml
amqp:
  connection:
    url: amqp://127.0.0.1:5671
```


###  `amqp.connection.username`
- Type: `string` *required*

Sets the username to be used by the AMQP broker.

Example:

```yaml
amqp:
  connection:
    username: guest
```


###  `amqp.connection.password`
- Type: `string` *required*

Sets the password to be used by the AMQP broker.

Example:

```yaml
amqp:
  connection:
    password: guest
```


###  `amqp.queue.durable`
- Type: `boolean` 
- Default `false`

Sets whether the queue will be durable or not (the queue will survive a server reboot).

Example:

```yaml
amqp:
  queue:
    durable: "true"
```


###  `amqp.queue.exclusive`
- Type: `boolean`
- Default `false`

Sets whether the queue will be exclusive (restricted to the current connection).

Example:

```yaml
amqp:
  queue:
    exclusive: "true"
```


###  `amqp.queue.autodelete`
- Type: `boolean`
- Default `false`

Sets whether to auto delete queue (will be deleted by server when no longer in use).

Example:

```yaml
amqp:
  queue:
    autodelete: "true"
```


## Steps

### Define connection
```text copy=true
the AMQP connection URL {url} using the user {username} and the password {password}
```

Sets the URL and credentials to be used by the AMQP broker. This is the descriptive way of setting the configuration 
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


### Define destination queue
```text copy=true
the destination queue {word}
```

Sets the name of the queue to watch.

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


### Set pause
```text copy=true
wait for {duration}
```
Waits a fixed duration (usually to ensure a message has been processed).

#### Parameters
| Name       | Wakamiti type            | Description    |
|------------|--------------------------|----------------|
| `duration` | [duration][1] *required* | Amount of time |

#### Examples:
```gherkin
* Wait for 2 seconds
```


### Validate message
```text copy=true
the following JSON message is received within {duration}:
    {data}
```
Validates that a specific JSON message is received in the destination queue, failing after a certain timeout.

#### Parameters
| Name       | Wakamiti type            | Description         |
|------------|--------------------------|---------------------|
| `duration` | [duration][1] *required* | Amount of time      |
| `data`     | `document` *required*    | A JSON message body |

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


### Validate message (file)
```text copy=true
the message from the JSON file {file} is received within {duration}
```

Validates that a specific JSON message is received in the destination queue, failing after a certain timeout.

#### Parameters
| Name       | Wakamiti type            | Description                        |
|------------|--------------------------|------------------------------------|
| `file`     | `file` *required*        | A local file with the JSON message |
| `duration` | [duration][1] *required* | Amount of time                     |

#### Examples:
```gherkin
Then the message from the JSON file 'data/message.json' is received within 5 seconds
```


[1]: en/wakamiti/architecture#duration