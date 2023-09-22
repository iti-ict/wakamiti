---
title: Email
date: 2023-08-03
slug: /plugins/email
---

Este plugin permite comprobar el estado de las carpetas de un servidor de correo,
verificar el número de mensajes sin leer, e interceptar nuevos mensajes entrantes.
También permite validar campos del último mensaje tales como asunto, remitente, cuerpo
y adjuntos.

Este plugin está diseñado para usarse conjuntamente con otros para formar escenarios completos.
Por ejemplo, para validar que una aplicación envía correos como consecuencia de alguna otra 
operación como una petición REST.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:email-wakamiti-plugin:1.1.3
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>email-wakamiti-plugin</artifactId>
  <version>1.1.3</version>
</dependency>
```

---
## Tabla de contenido

---

---
## Configuración


### `email.store.address`

Dirección de correo del usuario del servidor, para usarse como crendenciales de login.

Ejemplo:
```yaml
email:
  address: test@localhost
```

Desde: 1.0.0

<br /><br />

### `email.password`

Contraseña del usuario del servidor de correo, para usarse como credenciales de login.

Ejemplo:
```yaml
email:
  password: xjlk4324
```

Desde: 1.0.0

<br /><br />

### `email.store.host`

Nombre de red o dirección IP donde está ubicado el almacén del servidor de correo.

Ejemplo:
```yaml
email:
  store:
    host: imap.gmail.com
```

Desde: 1.0.0

<br /><br />

### `email.store.port`

Puerto para acceder al almacén del servidor de correo (suele variar en función del protocolo).

Ejemplo:
```yaml
email:
  port: 993
```

Desde: 1.0.0

<br /><br />

### `email.store.protocol`

Protocolo usado por el almacén del servidor de correo.

Ejemplo:
```yaml
email:
  store:
    protocol: imap
```

---

### `email.store.folder`

Nombre de la carpeta a comprobar dentro del almacén de correo
Ejemplo:
```yaml
email:
  store:
    folder: INBOX
```

Desde: 1.0.0


---
## Pasos


### Definir la ubicación del servidor de correo
```text copy=true
el servidor de correo ubicado en {host:text}:{port:int} usando el protocolo {protocol:word}
```

#### Parámetros:
| nombre      | tipo wakamiti | descripción                                    |
|-------------|---------------|------------------------------------------------|
| `host`      | `text`        | IP o nombre de red del servidor de correo      |
| `port`      | `int`         | Puerto del almacén de correo (según protocolo) |
| `protocol`  | `word`        | Protocolo del almacén de correo                |

#### Ejemplos:
```gherkin
  Dado el servidor de correo ubicado en 'imap.gmail.com':993 usando el protocolo imap
```

Desde: 1.0.0

<br /><br />

### Definir las credenciales de usuario
```text copy=true
el usuario de correo con dirección {address:text} y contraseña {password:text}
```

#### Parámetros:
| nombre     | tipo wakamiti | descripción               |
|------------|---------------|---------------------------|
| `address`  | `text`        | La dirección de correo    |
| `password` | `text`        | La contraseña de usuario  |

#### Ejemplos:
```gherkin
  Dado el usuario de correo con dirección 'john@mymail.com'  y contraseña 'daDjkl3434S'
```

Desde: 1.0.0

<br /><br />

### Definir la carpeta de correo usada para las pruebas
```text copy=true
la carpeta de correo {text}
```

#### Parámetros:
| tipo wakamiti  | descripción                       |
|----------------|-----------------------------------|
| `text`         | El nombre de la carpeta de correo |

#### Ejemplos:
```gherkin
  Dada la carpeta de correo 'INBOX'
```

Desde: 1.0.0

<br /><br />

### Comprobar el número de correos sin leer
```text copy=true
(que) el número de correos sin leer {integer-assertion}
```

#### Parámetros:
| tipo wakamiti       | descripción                                           |
|---------------------|-------------------------------------------------------|
| `integer-assertion` | Comprobación a aplicar al número de mensajes sin leer |

#### Ejemplos:
```gherkin
  Dado que el número de correos sin leer is mayor que 0
```

Desde: 1.0.0

<br /><br />

### Comprobar que se recibe un nuevo correo en un intervalo de tiempo determinado
```text copy=true
(que) se recibe un nuevo correo en los próximos {sec:integer} segundos
```

#### Parámetros:
| nombre     | tipo wakamiti | descripción                                        |
|------------|---------------|----------------------------------------------------|
| `sec`      | `integer`     | Segundos a la espera de que llegue un nuevo correo |

#### Ejemplos:
```gherkin
  Entonces se recibe un nuevo correo en los próximos 5 segundos
```

Desde: 1.0.0

<br /><br />

### Comprobar el asunto del último correo
```text copy=true
(que) el asunto del correo {text-assertion}
```

#### Parámetros:
| tipo wakamiti    | descripción                                  |
|------------------|----------------------------------------------|
| `text-assertion` | Comprobación a aplicar al asunto del correo  |

#### Ejemplos:
```gherkin
  Entonces el asunto del correo empieza por 'Nueva incidencia'
```

Desde: 1.0.0

<br /><br />

### Comprobar el remitente del último correo
```text copy=true
(que) el remitente del correo {text-assertion}
```

#### Parámetros:
| tipo wakamiti    | descripción                                    |
|------------------|------------------------------------------------|
| `text-assertion` | Comprobación a aplicar al remitente del correo |

#### Ejemplos:
```gherkin
  Entonces el remitente del correo es 'support@company.com'
```

Desde: 1.0.0

<br /><br />

### Comprobar el contenido del cuerpo del último correo
```text copy=true
(que) el cuerpo del correo es:
```

#### Parámetros:
| tipo wakamiti | descripción             |
|---------------|-------------------------|
| `document`    | El contenido a comparar |

#### Ejemplos:
```gherkin
  Entonces el cuerpo del correo es:
  """
     Hola,
     Su incidencia ha sido recibida.
     Saludos.
  """
```

Desde: 1.0.0

<br /><br />

### Comprobar parcialmente el contenido del cuerpo del último correo
```text copy=true
(que) el cuerpo del correo contiene lo siguiente:
```

#### Parámetros:
| tipo wakamiti | descripción             |
|---------------|-------------------------|
| `document`    | El contenido a comparar |

#### Ejemplos:
```gherkin
  Entonces el cuerpo del correo contiene lo siguiente:
  """
     Su incidencia ha sido recibida.
  """
```

Desde: 1.0.0

<br /><br />

### Comparar el contenido del cuerpo del último correo con un fichero
```text copy=true
(que) el cuerpo del correo es el contenido del fichero {file}
```

#### Parámetros:
| tipo wakamiti | descripción                 |
|---------------|-----------------------------|
| `file`        | Ruta del fichero a comparar |

#### Ejemplos:
```gherkin
  Entonces el cuerpo del correo es el contenido del fichero 'email.txt'
```

Desde: 1.0.0

<br /><br />

### Comparar parcialmente el contenido del cuerpo del último correo con un fichero
```text copy=true
(que) el cuerpo del correo contiene el contenido del fichero {file}
```

#### Parámetros:
| tipo wakamiti | descripción                 |
|---------------|-----------------------------|
| `file`        | Ruta del fichero a comparar |

#### Ejemplos:
```gherkin
  Entonces el cuerpo del correo contiene el contenido del fichero 'email.txt'
```

Desde: 1.0.0

<br /><br />

### Comprobar el número de adjuntos en el último correo
```text copy=true
(que) el número de adjuntos en el correo {integer-assertion}
```

#### Parámetros:
| tipo wakamiti       | descripción                                  |
|---------------------|----------------------------------------------|
| `integer-assertion` | Comprobación a aplicar al número de adjuntos |

#### Ejemplos:
```gherkin
  Entonces el número de adjuntos en el correo es menor que 2
```

Desde: 1.0.0

<br /><br />

### Comprobar que el último correo contiene un adjunto con determinado nombre de fichero
```text copy=true
(que) el correo tiene un fichero adjunto cuyo nombre {text-assertion}
```

#### Parámetros:
| tipo wakamiti    | descripción                                               |
|------------------|-----------------------------------------------------------|
| `text-assertion` | Comprobación a aplicar al nombre de los ficheros adjuntos |

#### Ejemplos:
```gherkin
  Entonces el correo tiene un fichero adjunto cuyo nombre es 'attach.txt'
```

Desde: 1.0.0

<br /><br />

### Comparar el contenido de un fichero adjunto en formato binario
```text copy=true
(que) el correo tiene un fichero adjunto con el contenido del fichero binario {file}
```

#### Parámetros:
| tipo wakamiti | descripción                 |
|---------------|-----------------------------|
| `file`        | Ruta del fichero a comparar |

#### Ejemplos:
```gherkin
  Entonces el correo tiene un fichero adjunto con el contenido del fichero binario 'attach.dat'
```

Desde: 1.0.0

<br /><br />

### Comparar el contenido de un fichero adjunto en formato de texto
```text copy=true
(que) el correo tiene un fichero adjunto con el contenido del fichero de texto {file}
```

#### Parámetros:
| tipo wakamiti | descripción                 |
|---------------|-----------------------------|
| `file`        | Ruta del fichero a comparar |

#### Ejemplos:
```gherkin
  Entonces el correo tiene un fichero adjunto con el contenido del fichero de texto 'attach.txt'
```

Desde: 1.0.0

<br /><br />

### Comparar el contenido de un adjunto con el texto indicado
```text copy=true
(que) el correo tiene un fichero adjunto con el siguiente contenido:
```

#### Parámetros:
| tipo wakamiti | descripción      |
|---------------|------------------|
| `document`    | Texto a comparar |

#### Ejemplos:
```gherkin
  Entonces el correo tiene un fichero adjunto con el siguiente contenido:
  """
  Esto es un contenido adjunto
  """
```

Desde: 1.0.0

<br /><br />

### Eliminar todos los correos con un remitente determinado (operación de limpieza)
```text copy=true
Al finalizar, se borran todos los correos cuyo remitente {text-assertion}
```

#### Parámetros:
| tipo wakamiti    | descripción                                        |
|------------------|----------------------------------------------------|
| `text-assertion` | Comprobación a aplicar al remitente de los correos |

#### Ejemplos:
```gherkin
  Antecedentes:
    * Al finalizar, se borran todos los correos cuyo remitente es 'test@localhost'
```

Desde: 1.0.0

<br /><br />

### Eliminar todos los correos con un asunto determinado (operación de limpieza)
```text copy=true
Al finalizar, se borran todos los correos cuyo asunto {text-assertion}
```

#### Parámetros:
| tipo wakamiti    | descripción                                     |
|------------------|-------------------------------------------------|
| `text-assertion` | Comprobación a aplicar al asunto de los correos |

#### Ejemplos:
```gherkin
  Antecedentes:
    * Al finalizar, se borran todos los correos cuyo asunto empieza por 'Testing'
```

Desde: 1.0.0
