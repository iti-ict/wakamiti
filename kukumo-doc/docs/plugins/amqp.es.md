---
title: Pasos AMQP
date: 2022-09-20
slug: /plugins/amqp
---


Este plugin proporciona una serie de pasos para interactuar con una aplicación vía 
[Advanced Message Queuing Protocol](https://amqp.org). La implementación subyacente se basa en 
[RabbitMQ](https://rabbitmq.com), aunque podría cambiar en futuras versiones.

Actualmente, esta librería proporciona una funcionalidad muy limitada y existe como prueba de concepto.


**Configuración**:
- [`amqp.connection.url`](#amqpconnectionurl)
- [`amqp.connection.username`](#amqpconnectionusername)
- [`amqp.connection.password`](#amqpconnectionpassword)
- [`amqp.queue.durable`](#amqpqueuedurable)
- [`amqp.queue.exclusive`](#amqpqueueexclusive)
- [`amqp.queue.autodelete`](#amqpqueueautodelete)

**Pasos**:
- [Definir conexión](#definir-conexi%C3%B3n)
- [Definir cola destino](#definir-cola-destino)
- [Enviar mensaje a cola](#enviar-mensaje-a-cola)
- [Enviar mensaje a cola (fichero)](#enviar-mensaje-a-cola-fichero)
- [Establecer pausa](#establecer-pausa)
- [Validar mensaje](#validar-mensaje)
- [Validar mensaje (fichero)](#validar-mensaje-fichero)


## Configuración

---
###  `amqp.connection.url`
Establece la URL que utilizará el agente AMQP.

Ejemplo:

```yaml
amqp:
  connection:
    url: amqp://127.0.0.1:5671
```

---
###  `amqp.connection.username`
Establece el nombre de usuario que utilizará el agente AMQP.

Ejemplo:

```yaml
amqp:
  connection:
    username: guest
```

---
###  `amqp.connection.password`
Establece la contraseña de usuario que utilizará el agente AMQP.

Ejemplo:

```yaml
amqp:
  connection:
    password: guest
```

---
###  `amqp.queue.durable`
Establece si la cola será duradera o no (la cola sobrevivirá a un reinicio del servidor).

El valor por defecto es `false`.

Ejemplo:

```yaml
amqp:
  queue:
    durable: "true"
```

---
###  `amqp.queue.exclusive`
Establece si la cola será exclusiva (restringida a la conexión actual).

El valor por defecto es `false`.

Ejemplo:

```yaml
amqp:
  queue:
    exclusive: "true"
```

---
###  `amqp.queue.autodelete`
Establece si la cola de eliminación automática (el servidor la eliminará cuando ya no esté en uso).

El valor por defecto es `false`.

Ejemplo:

```yaml
amqp:
  queue:
    autodelete: "true"
```


## Pasos

---
### Definir conexión

```
la conexión AMQP con URL {url} usando el usuario {username} y la contraseña {password}
```
Establece la URL y las credenciales que utilizará el agente AMQP. Esta es la forma descriptiva de establecer las 
propiedades [`amqp.connection.url`](#amqpconnectionurl), [`amqp.connection.username`](#amqpconnectionusername), 
[`amqp.connection.password`](#amqpconnectionpassword).


#### Parámetros:
| Nombre     | Kukumo type | Descripción           |
|------------|-------------|-----------------------|
| `url`      | `text`      | La URL del agente     |
| `username` | `text`      | Nombre de usuario     |
| `password` | `text`      | Contraseña de usuario |

#### Ejemplos:
```gherkin
  Dada la conexión AMQP con URL 'amqp://127.0.0.1:5671' usando el usuario 'guest' y la contraseña 'guest'
```


---
### Definir cola destino

```
la cola de destino {word}
```
Establece el nombre de la cola que se observará.

#### Parámetros:
| Nombre | Kukumo type | Descripción       |
|--------|-------------|-------------------|
| `word` | `word`      | Nombre de la cola |

#### Ejemplos:
```gherkin
  Dada la cola de destino TEST
```


---
### Enviar mensaje a cola

```
se envía a la cola {word} el siguiente mensaje JSON:
```
Envía un mensaje JSON a la cola indicada.

#### Parámetros:
| Nombre | Kukumo type  | Descripción       |
|--------|--------------|-------------------|
| `word` | `word`       | Nombre de la cola |
|        | `document`   | Mensaje JSON      |

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


---
### Enviar mensaje a cola (fichero)
```
se envía a la cola {queue} el mensaje del fichero JSON {file}
```
Envía el contenido de un fichero JSON a la cola indicada.

#### Parámetros:
| Nombre  | Kukumo type | Descripción       |
|---------|-------------|-------------------|
| `file`  | `file`      | Fichero JSON      |
| `queue` | `word`      | Nombre de la cola |

#### Ejemplos:
```gherkin
  Cuando se envía a la cola TEST el mensaje del fichero JSON 'data/message.json'
```


---
### Establecer pausa

```
se espera durante {integer} segundo(s)
```
Se produce una espera de un número fijo de segundos (generalmente para asegurarse de que se haya procesado el mensaje).

#### Parámetros:
| Nombre    | Kukumo type | Descripción                      |
|-----------|-------------|----------------------------------|
| `integer` | `integer`   | Cantidad de tiempo (en segundos) |

#### Ejemplos:
```gherkin
  * se espera durante 2 segundos
```


---
### Validar mensaje

```
el siguiente mensaje JSON se recibe en {integer} segundos:
```
Valida que se reciba un mensaje JSON específico en la [cola observada](#definir-cola-destino), produciéndose un fallo 
después del tiempo de espera indicado.

#### Parámetros:
| Nombre    | Kukumo type | Descripción                      |
|-----------|-------------|----------------------------------|
| `integer` | `integer`   | Cantidad de tiempo (en segundos) |
|           | `document`  | Mensaje JSON                     |

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


---
### Validar mensaje (fichero)

```
el mensaje del fichero JSON {file} se recibe en {seconds} segundos
```
Valida que se reciba el contenido de un fichero JSON específico en la [cola observada](#definir-cola-destino), 
produciéndose un fallo después del tiempo de espera indicado.

#### Parámetros:
| Nombre | Kukumo type  | Descripción                      |
|--------|--------------|----------------------------------|
| `file` | `file`       | Fichero JSON                     |
|        | `integer`    | Cantidad de tiempo (en segundos) |

#### Ejemplos:
```gherkin
  Cuando el mensaje del fichero JSON 'data/message.json' se recibe en 5 segundos
```


