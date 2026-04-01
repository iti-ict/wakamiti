Este plugin proporciona una serie de pasos para interactuar con brokers AMQP vía
[Advanced Message Queuing Protocol](https://amqp.org). Admite conexiones AMQP 1.0 y AMQP 0.9.1,
configuración de colas y persistencia de mensajes, además de pasos para enviar y validar mensajes
JSON, purgar colas y comprobar que no se reciben mensajes.


## Instalación


Incluye el módulo en la sección correspondiente.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:amqp-wakamiti-plugin:2.9.0
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>amqp-wakamiti-plugin</artifactId>
  <version>2.9.0</version>
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


###  `amqp.connection.protocol`
- Tipo: `string`
- Por defecto: `AMQP_1_0`

Selecciona el protocolo AMQP que se utilizará. Se aceptan `AMQP_1_0` y `AMQP_0_9_1` (también alias como
`amqp-1.0`, `amqp-0.9.1`, `1_0`, `0_9_1`).

Ejemplo:

```yaml
amqp:
  connection:
    protocol: AMQP_0_9_1
```


###  `amqp.message.persistent`
- Tipo: `boolean`
- Por defecto: `true`

Establece si los mensajes se envían como persistentes.

Ejemplo:

```yaml
amqp:
  message:
    persistent: "false"
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


### Definir protocolo
```text copy=true
el protocolo AMQP {protocol}
```

Define el protocolo AMQP que se utilizará. Esta es la forma descriptiva de establecer la propiedad
[`amqp.connection.protocol`](#amqpconnectionprotocol).

#### Parámetros:
| Nombre     | Wakamiti type        | Descripción        |
|------------|----------------------|--------------------|
| `protocol` | `word` *obligatorio* | Nombre del protocolo |

#### Ejemplos:
```gherkin
Dado el protocolo AMQP AMQP_0_9_1
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


### Purgar cola
```text copy=true
(que) se vacía la cola {word}
```
- [Modo post-ejecución][2]

Purga todos los mensajes pendientes de la cola indicada.

#### Parámetros:
| Nombre | Wakamiti type        | Descripción       |
|--------|----------------------|-------------------|
| `word` | `word` *obligatorio* | Nombre de la cola |

#### Ejemplos:
```gherkin
Cuando se vacía la cola TEST
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

### Validar mensaje (en cualquier orden)
```text copy=true
el siguiente mensaje JSON se recibe en {duration} (en cualquier orden):
    {data}
```

Valida que se reciba un mensaje JSON en la cola observada, permitiendo que el orden de los campos
y de los elementos de arrays sea diferente.

#### Parámetros:
| Nombre     | Wakamiti type               | Descripción        |
|------------|-----------------------------|--------------------|
| `duration` | [duration][1] *obligatorio* | Cantidad de tiempo |
| `data`     | `document` *obligatorio*    | Mensaje JSON       |

#### Ejemplos:
```gherkin
Cuando el siguiente mensaje JSON se recibe en 5 segundos (en cualquier orden):
    """json
    {
      "items": [
        { "id": 2 },
        { "id": 1 }
      ]
    }
    """
```


### Validar fragmento de mensaje
```text copy=true
el siguiente fragmento JSON se recibe en {duration}:
    {data}
```

Valida que al menos uno de los mensajes JSON recibidos contenga el fragmento esperado como subconjunto.

#### Parámetros:
| Nombre     | Wakamiti type               | Descripción            |
|------------|-----------------------------|------------------------|
| `duration` | [duration][1] *obligatorio* | Cantidad de tiempo     |
| `data`     | `document` *obligatorio*    | Fragmento JSON esperado|

#### Ejemplos:
```gherkin
Cuando el siguiente fragmento JSON se recibe en 5 segundos:
    """json
    {
      "data": {
        "message": "Test message sent"
      }
    }
    """
```


### Validar fragmento de mensaje (fichero)
```text copy=true
el fragmento JSON del fichero {file} se recibe en {duration}
```

Valida que al menos uno de los mensajes JSON recibidos contenga el fragmento cargado desde fichero.

#### Parámetros:
| Nombre     | Wakamiti type               | Descripción               |
|------------|-----------------------------|---------------------------|
| `file`     | `file` *obligatorio*        | Fichero con fragmento JSON|
| `duration` | [duration][1] *obligatorio* | Cantidad de tiempo        |

#### Ejemplos:
```gherkin
Cuando el fragmento JSON del fichero 'data/message-fragment.json' se recibe en 5 segundos
```


### Validar ausencia de mensajes
```text copy=true
no se recibe ningún mensaje durante {duration}
```

Valida que no se reciba ningún mensaje en la cola de destino durante el tiempo de espera.

#### Parámetros:
| Nombre     | Wakamiti type               | Descripción        |
|------------|-----------------------------|--------------------|
| `duration` | [duration][1] *obligatorio* | Cantidad de tiempo |

#### Ejemplos:
```gherkin
Entonces no se recibe ningún mensaje durante 10 segundos
```


## Modos especiales


Algunos pasos pueden ejecutarse con un comportamiento diferente si se definen de las siguientes maneras:

### Modo post-ejecución
```text copy=true
Al finalizar, * 
```

El paso se ejecutará una vez finalice el escenario, independientemente del resultado de la ejecución.

#### Ejemplos:
```gherkin
* Al finalizar, se vacía la cola TEST
```


[1]: wakamiti/architecture#duration
[2]: #modo-post-ejecución
