Kukumo :: AMQP
====================================================================================================
A set of steps to interact with an application via the 
[Advanced Message Queuing Protocol](https://amqp.org). The underlying implementation 
is based on [RabbitMQ](https://rabbitmq.com), although it might change in further versions.

Currently, this library provides very limited functionality and exists mostly as a proof 
of concept.


Configuration
----------------------------------------------------------------------------------------------------

###  `amqp.connection.url`
Set the URL to be used by the AMQP broker.

Example:

```yaml
amqp:
  connection:
    url: amqp://127.0.0.1:5671
```


###  `amqp.connection.username`
Set the username to be used by the AMQP broker.

Example:

```yaml
amqp:
  connection:
    username: guest
```


###  `amqp.connection.password`
Set the password to be used by the AMQP broker.

Example:

```yaml
amqp:
  connection:
    password: guest
```


###  `amqp.queue.durable`

Example:

```yaml
amqp:
  queue:
    durable:
```

###  `amqp.queue.exclusive`

Example:

```yaml
amqp:
  queue:
    exclusive:
```


###  `amqp.queue.autodelete`


Example:

```yaml
amqp:
  queue:
    autodelete:
```

Steps
----------------------------------------------------------------------------------------------------

## Setup steps

---
#### `the AMQP connection URL {url:text} using the user {username:text} and the password {password:text}`
Set the URL and credentials to be used by the AMQP broker. This is the descriptive way of 
setting the configuration properties `amqp.connection.url`, `amqp.connection.username`, 
`amqp.connection.password`.

##### Parameters:
| name       | Kukumo type | description              |
|------------|-------------|--------------------------|
| `url`      | `text`      | The broker URL           |
| `username` | `text`      | The credentials username |
| `password` | `text`      | The credentials password |

##### Examples:
```gherkin
Given the AMQP connection URL 'amqp://127.0.0.1:5671' using the user 'guest' and the password 'guest'
```
##### Localizations:
- :es: `la conexión AMQP con URL {url:text} usando el usuario {username:text} y la contraseña {password:text}`




---
#### `the destination queue {word}`
The name of the queue to watch.

##### Parameters:
| name | Kukumo type | description  |
|------|-------------|--------------|
|      | `word`      | A queue name |
##### Examples:
```gherkin
And the destination queue TEST
```
##### Localizations:
- :es: `la cola de destino {word}`



## Action steps


---
#### `the following JSON message is sent to the queue {word}:`
Send a JSON message to the given queue

##### Parameters:
| name | Kukumo type  | description         |
|------|--------------|---------------------|
|      | `word`       | A queue name        |
|      | `document`   | A JSON message body |
##### Examples:
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
##### Localizations:
- :es: `se envía a la cola {word} el siguiente mensaje JSON:`



---
#### `the message from the JSON file {file:file} is sent to the queue {queue:word}`
Send a JSON message extracted from a local file to the given queue

##### Parameters:
| name | Kukumo type | description                        |
|------|-------------|------------------------------------|
|      | `file`      | A local file with the JSON message |
|      | `word`      | A queue name                       |

##### Examples:
```gherkin
When the message from the JSON file 'data/message.json' is sent to the queue TEST
```
##### Localizations:
- :es: `se envía a la cola {queue:word} el mensaje del fichero JSON {file:file}`



---
#### `wait for {integer} second(s)`
Wait a fixed number of seconds (usually to ensure a message has been processed).

##### Parameters:
| name | Kukumo type | description                 |
|------|-------------|-----------------------------|
|      | `integer`   | Amount of time (in seconds) |
##### Examples:
```gherkin
* Wait for 2 seconds
```
##### Localizations:
- :es: `se espera durante {integer} segundo(s)`


## Validation steps

---
#### `the following JSON message is received within {integer} seconds:`
Validate that a specific JSON message is received in the destination queue,
failing after a certain timeout.

##### Parameters:
| name | Kukumo type | description                 |
|------|-------------|-----------------------------|
|      | `integer`   | Amount of time (in seconds) |
|      | `document`  | A JSON message body         |
##### Examples:
```gherkin
Then the following JSON message is received within 5 seconds:
    ```json
    {
        "data": {
            "message": "Test message sent"
        }
    }
    ```
```
##### Localizations:
- :es: `el siguiente mensaje JSON se recibe en {integer} segundos:`



---
#### `the message from the JSON file {file:file} is received within {seconds:integer} seconds`
Validate that a specific JSON message is received in the destination queue,
failing after a certain timeout.

##### Parameters:
| name | Kukumo type  | description                         |
|------|--------------|-------------------------------------|
|      | `file`       | A local file with the JSON message  |
|      | `integer`    | Amount of time (in seconds)         |
##### Examples:
```gherkin
Then the message from the JSON file 'data/message.json' is received within 5 seconds:
```
##### Localizations:
- :es: `el mensaje del fichero JSON {file:file} se recibe en {seconds:integer} segundos`






