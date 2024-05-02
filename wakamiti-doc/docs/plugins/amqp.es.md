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


---
## Tabla de contenido

---


## Instalación


Incluye el módulo en la sección correspondiente.

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


## Configuración


###  `amqp.connection.url`
- Tipo: `string` *obligatorio*

Establece la URL que utilizará el agente AMQP.

Ejemplo:

```yaml
amqp:
  connection:
    url: amqp://127.0.0.1:5671
```


###  `amqp.connection.username`
- Tipo: `string` *obligatorio*

Establece el nombre de usuario que utilizará el agente AMQP.

Ejemplo:

```yaml
amqp:
  connection:
    username: guest
```


###  `amqp.connection.password`
- Tipo: `string` *obligatorio*

Establece la contraseña de usuario que utilizará el agente AMQP.

Ejemplo:

```yaml
amqp:
  connection:
    password: guest
```


###  `amqp.queue.durable`
- Tipo: `boolean`
- Por defecto: `false`

Establece si la cola será duradera o no (la cola sobrevivirá a un reinicio del servidor).

Ejemplo:

```yaml
amqp:
  queue:
    durable: "true"
```


###  `amqp.queue.exclusive`
- Tipo: `boolean`
- Por defecto: `false`

Establece si la cola será exclusiva (restringida a la conexión actual).

Ejemplo:

```yaml
amqp:
  queue:
    exclusive: "true"
```


###  `amqp.queue.autodelete`
- Tipo: `boolean`
- Por defecto: `false`

Establece si la cola de eliminación automática (el servidor la eliminará cuando ya no esté en uso).

Ejemplo:

```yaml
amqp:
  queue:
    autodelete: "true"
```


## Pasos


### Definir conexión
```text copy=true
la conexión AMQP con URL {url} usando el usuario {username} y la contraseña {password}
```

Establece la URL y las credenciales que utilizará el agente AMQP. Esta es la forma descriptiva de establecer las 
propiedades [`amqp.connection.url`](#amqpconnectionurl), [`amqp.connection.username`](#amqpconnectionusername), 
[`amqp.connection.password`](#amqpconnectionpassword).

#### Parámetros:
| Nombre     | Wakamiti type        | Descripción           |
|------------|----------------------|-----------------------|
| `url`      | `text` *obligatorio* | La URL del agente     |
| `username` | `text` *obligatorio* | Nombre de usuario     |
| `password` | `text` *obligatorio* | Contraseña de usuario |

#### Ejemplos:
```gherkin
Dada la conexión AMQP con URL 'amqp://127.0.0.1:5671' usando el usuario 'guest' y la contraseña 'guest'
```


### Definir cola destino
```text copy=true
la cola de destino {word}
```

Establece el nombre de la cola que se observará.

#### Parámetros:
| Nombre | Wakamiti type        | Descripción       |
|--------|----------------------|-------------------|
| `word` | `word` *obligatorio* | Nombre de la cola |

#### Ejemplos:
```gherkin
Dada la cola de destino TEST
```


### Enviar mensaje a cola
```text copy=true
se envía a la cola {word} el siguiente mensaje JSON:
    {data}
```

Envía un mensaje JSON a la cola indicada.

#### Parámetros:
| Nombre | Wakamiti type            | Descripción       |
|--------|--------------------------|-------------------|
| `word` | `word` *obligatorio*     | Nombre de la cola |
| `data` | `document` *obligatorio* | Mensaje JSON      |

#### Ejemplos:
```gherkin
Cuando se envía a la cola TEST el siguiente mensaje JSON:
    """json
    {
        "data": {
            "message": "Test message sent"
        }
    }
    """
```


### Enviar mensaje a cola (fichero)
```text copy=true
se envía a la cola {queue} el mensaje del fichero JSON {file}
```

Envía el contenido de un fichero JSON a la cola indicada.

#### Parámetros:
| Nombre  | Wakamiti type        | Descripción       |
|---------|----------------------|-------------------|
| `file`  | `file` *obligatorio* | Fichero JSON      |
| `queue` | `word` *obligatorio* | Nombre de la cola |

#### Ejemplos:
```gherkin
Cuando se envía a la cola TEST el mensaje del fichero JSON 'data/message.json'
```


### Establecer pausa
```text copy=true
se espera durante {duration}
```

Se produce una espera de una duración fija (generalmente para asegurarse de que se haya procesado el mensaje).

#### Parámetros:
| Nombre     | Wakamiti type               | Descripción        |
|------------|-----------------------------|--------------------|
| `duration` | [duration][1] *obligatorio* | Cantidad de tiempo |

#### Ejemplos:
```gherkin
* se espera durante 2 segundos
```


### Validar mensaje
```text copy=true
el siguiente mensaje JSON se recibe en {duration}:
    {data}
```

Valida que se reciba un mensaje JSON específico en la [cola observada](#definir-cola-destino), produciéndose un fallo 
después del tiempo de espera indicado.

#### Parámetros:
| Nombre     | Wakamiti type               | Descripción        |
|------------|-----------------------------|--------------------|
| `duration` | [duration][1] *obligatorio* | Cantidad de tiempo |
| `data`     | `document` *obligatorio*    | Mensaje JSON       |

#### Ejemplos:
```gherkin
Cuando el siguiente mensaje JSON se recibe en 5 segundos:
    """json
      {
        "data": {
          "message": "Test message sent"
        }
      }
    """
```


### Validar mensaje (fichero)
```text copy=true
el mensaje del fichero JSON {file} se recibe en {duration}
```

Valida que se reciba el contenido de un fichero JSON específico en la [cola observada](#definir-cola-destino), 
produciéndose un fallo después del tiempo de espera indicado.

#### Parámetros:
| Nombre     | Wakamiti type               | Descripción        |
|------------|-----------------------------|--------------------|
| `file`     | `file` *obligatorio*        | Fichero JSON       |
| `duration` | [duration][1] *obligatorio* | Cantidad de tiempo |

#### Ejemplos:
```gherkin
Cuando el mensaje del fichero JSON 'data/message.json' se recibe en 5 segundos
```


[1]: wakamiti/architecture#duration