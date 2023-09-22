---
title: Pasos AMQP
date: 2022-09-20
slug: /plugins/amqp
---


Este plugin proporciona una serie de pasos para interactuar con una aplicación vía 
[Advanced Message Queuing Protocol](https://amqp.org). La implementación subyacente se basa en 
[RabbitMQ](https://rabbitmq.com), aunque podría cambiar en futuras versiones.

> **AVISO**
>
> Actualmente, esta librería proporciona una funcionalidad muy limitada y existe como prueba de concepto.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:amqp-wakamiti-plugin:2.3.3
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>amqp-wakamiti-plugin</artifactId>
  <version>2.3.3</version>
</dependency>
```

---
## Tabla de contenido

---

---
## Configuración



###  `amqp.connection.url`
Establece la URL que utilizará el agente AMQP.

Ejemplo:

```yaml
amqp:
  connection:
    url: amqp://127.0.0.1:5671
```

<br /><br />

###  `amqp.connection.username`
Establece el nombre de usuario que utilizará el agente AMQP.

Ejemplo:

```yaml
amqp:
  connection:
    username: guest
```

<br /><br />

###  `amqp.connection.password`
Establece la contraseña de usuario que utilizará el agente AMQP.

Ejemplo:

```yaml
amqp:
  connection:
    password: guest
```

<br /><br />

###  `amqp.queue.durable`
Establece si la cola será duradera o no (la cola sobrevivirá a un reinicio del servidor).

El valor por defecto es `false`.

Ejemplo:

```yaml
amqp:
  queue:
    durable: "true"
```

<br /><br />

###  `amqp.queue.exclusive`
Establece si la cola será exclusiva (restringida a la conexión actual).

El valor por defecto es `false`.

Ejemplo:

```yaml
amqp:
  queue:
    exclusive: "true"
```

<br /><br />

###  `amqp.queue.autodelete`
Establece si la cola de eliminación automática (el servidor la eliminará cuando ya no esté en uso).

El valor por defecto es `false`.

Ejemplo:

```yaml
amqp:
  queue:
    autodelete: "true"
```


---
## Pasos



### Definir conexión

```text copy=true
la conexión AMQP con URL {url} usando el usuario {username} y la contraseña {password}
```
Establece la URL y las credenciales que utilizará el agente AMQP. Esta es la forma descriptiva de establecer las 
propiedades [`amqp.connection.url`](#amqpconnectionurl), [`amqp.connection.username`](#amqpconnectionusername), 
[`amqp.connection.password`](#amqpconnectionpassword).

#### Parámetros:
| Nombre     | Wakamiti type | Descripción           |
|------------|---------------|-----------------------|
| `url`      | `text`        | La URL del agente     |
| `username` | `text`        | Nombre de usuario     |
| `password` | `text`        | Contraseña de usuario |

#### Ejemplos:
```gherkin
  Dada la conexión AMQP con URL 'amqp://127.0.0.1:5671' usando el usuario 'guest' y la contraseña 'guest'
```

<br /><br />

### Definir cola destino

```text copy=true
la cola de destino {word}
```
Establece el nombre de la cola que se observará.

#### Parámetros:
| Nombre | Wakamiti type | Descripción       |
|--------|---------------|-------------------|
| `word` | `word`        | Nombre de la cola |

#### Ejemplos:
```gherkin
  Dada la cola de destino TEST
```

<br /><br />

### Enviar mensaje a cola

```text copy=true
se envía a la cola {word} el siguiente mensaje JSON:
```
Envía un mensaje JSON a la cola indicada.

#### Parámetros:
| Nombre | Wakamiti type | Descripción       |
|--------|---------------|-------------------|
| `word` | `word`        | Nombre de la cola |
|        | `document`    | Mensaje JSON      |

#### Ejemplos:
```gherkin
  Cuando se envía a la cola TEST el siguiente mensaje JSON:
    ```json
    {
        "data": {
            "message": "Test message sent"
        }
    }
    ```
```

<br /><br />

### Enviar mensaje a cola (fichero)
```text copy=true
se envía a la cola {queue} el mensaje del fichero JSON {file}
```
Envía el contenido de un fichero JSON a la cola indicada.

#### Parámetros:
| Nombre  | Wakamiti type | Descripción       |
|---------|---------------|-------------------|
| `file`  | `file`        | Fichero JSON      |
| `queue` | `word`        | Nombre de la cola |

#### Ejemplos:
```gherkin
  Cuando se envía a la cola TEST el mensaje del fichero JSON 'data/message.json'
```

<br /><br />

### Establecer pausa

```text copy=true
se espera durante {integer} segundo(s)
```
Se produce una espera de un número fijo de segundos (generalmente para asegurarse de que se haya procesado el mensaje).

#### Parámetros:
| Nombre    | Wakamiti type | Descripción                      |
|-----------|---------------|----------------------------------|
| `integer` | `integer`     | Cantidad de tiempo (en segundos) |

#### Ejemplos:
```gherkin
  * se espera durante 2 segundos
```

<br /><br />

### Validar mensaje

```text copy=true
el siguiente mensaje JSON se recibe en {integer} segundos:
```
Valida que se reciba un mensaje JSON específico en la [cola observada](#definir-cola-destino), produciéndose un fallo 
después del tiempo de espera indicado.

#### Parámetros:
| Nombre    | Wakamiti type | Descripción                      |
|-----------|---------------|----------------------------------|
| `integer` | `integer`     | Cantidad de tiempo (en segundos) |
|           | `document`    | Mensaje JSON                     |

#### Ejemplos:
```gherkin
  Cuando el siguiente mensaje JSON se recibe en 5 segundos:
    ```json
      {
        "data": {
          "message": "Test message sent"
        }
      }
```

<br /><br />

### Validar mensaje (fichero)

```text copy=true
el mensaje del fichero JSON {file} se recibe en {seconds} segundos
```
Valida que se reciba el contenido de un fichero JSON específico en la [cola observada](#definir-cola-destino), 
produciéndose un fallo después del tiempo de espera indicado.

#### Parámetros:
| Nombre | Wakamiti type | Descripción                      |
|--------|---------------|----------------------------------|
| `file` | `file`        | Fichero JSON                     |
|        | `integer`     | Cantidad de tiempo (en segundos) |

#### Ejemplos:
```gherkin
  Cuando el mensaje del fichero JSON 'data/message.json' se recibe en 5 segundos
```


