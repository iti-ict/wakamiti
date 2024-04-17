---
title: Email
date: 2023-08-03
slug: /plugins/email
---


Este plugin permite comprobar el estado de las carpetas de un servidor de correo, verificar el número de mensajes sin 
leer, e interceptar nuevos mensajes entrantes. También permite validar campos del último mensaje tales como asunto, 
remitente, cuerpo y adjuntos.

Este plugin está diseñado para usarse conjuntamente con otros para formar escenarios completos. Por ejemplo, para 
validar que una aplicación envía correos como consecuencia de alguna otra operación como una petición REST.


---
## Tabla de contenido

---


## Instalación


Incluye el módulo en la sección correspondiente.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:email-wakamiti-plugin:1.2.0
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>email-wakamiti-plugin</artifactId>
  <version>1.2.0</version>
</dependency>
```


## Configuración


### `email.address`
- Tipo: `string` *obligatorio*

Dirección de correo del usuario, para usarse como crendenciales de login.

Ejemplo:
```yaml
email:
  address: test@localhost
```


### `email.password`
- Tipo: `string` *obligatorio*

Contraseña del usuario, para usarse como credenciales de login.

Ejemplo:
```yaml
email:
  password: xjlk4324
```


### `email.store.host`
- Tipo: `string` *obligatorio*

Nombre de host o dirección IP donde está ubicado el almacén del servidor de correo.

Ejemplo:
```yaml
email:
  store:
    host: imap.gmail.com
```


### `email.store.port`
- Tipo: `integer` *obligatorio*

Puerto para acceder al almacén del servidor de correo (suele variar en función del protocolo).

Ejemplo:
```yaml
email:
  store:
    port: 993
```


### `email.store.protocol`
- Tipo: `string` *obligatorio*

Protocolo usado por el almacén del servidor de correo.

Ejemplo:
```yaml
email:
  store:
    protocol: imap
```


### `email.store.folder`
- Tipo: `string` *obligatorio*

Nombre de la carpeta a comprobar dentro del almacén de correo.

Ejemplo:
```yaml
email:
  store:
    folder: INBOX
```


## Pasos


### Definir la ubicación del servidor de correo
```text copy=true
el servidor de correo ubicado en {host}:{port} usando el protocolo {protocol}
```

#### Parámetros:
| nombre      | tipo wakamiti           | descripción                                    |
|-------------|-------------------------|------------------------------------------------|
| `host`      | `text` *obligatorio*    | IP o nombre de red del servidor de correo      |
| `port`      | `integer` *obligatorio* | Puerto del almacén de correo (según protocolo) |
| `protocol`  | `word` *obligatorio*    | Protocolo del almacén de correo                |

#### Ejemplos:
```gherkin
Dado el servidor de correo ubicado en 'imap.gmail.com':993 usando el protocolo imap
```


### Definir las credenciales de usuario
```text copy=true
el usuario de correo con dirección {address} y contraseña {password}
```

#### Parámetros:
| nombre     | tipo wakamiti        | descripción               |
|------------|----------------------|---------------------------|
| `address`  | `text` *obligatorio* | La dirección de correo    |
| `password` | `text` *obligatorio* | La contraseña de usuario  |

#### Ejemplos:
```gherkin
Dado el usuario de correo con dirección 'john@mymail.com'  y contraseña 'daDjkl3434S'
```


### Definir la carpeta de correo usada para las pruebas
```text copy=true
la carpeta de correo {text}
```

#### Parámetros:
| nombre | tipo wakamiti        | descripción                       |
|--------|----------------------|-----------------------------------|
| `text` | `text` *obligatorio* | El nombre de la carpeta de correo |

#### Ejemplos:
```gherkin
Dada la carpeta de correo 'INBOX'
```


### Comprobar el número de correos sin leer
```text copy=true
(que) el número de correos sin leer {matcher}
```

#### Parámetros:
| nombre    | tipo wakamiti                      | descripción                                           |
|-----------|------------------------------------|-------------------------------------------------------|
| `matcher` | `integer-assertion` *obligatorio*  | Comprobación a aplicar al número de mensajes sin leer |

#### Ejemplos:
```gherkin
Dado que el número de correos sin leer es mayor que 0
```


### Comprobar que se recibe un nuevo correo en un intervalo de tiempo determinado
```text copy=true
(que) se recibe un nuevo correo en los próximos {sec} segundos
```

#### Parámetros:
| nombre | tipo wakamiti           | descripción                                        |
|--------|-------------------------|----------------------------------------------------|
| `sec`  | `integer` *obligatorio* | Segundos a la espera de que llegue un nuevo correo |

#### Ejemplos:
```gherkin
Entonces se recibe un nuevo correo en los próximos 5 segundos
```


### Comprobar el asunto del último correo
```text copy=true
(que) el asunto del correo {matcher}
```

#### Parámetros:
| nombre    | tipo wakamiti                  | descripción                                  |
|-----------|--------------------------------|----------------------------------------------|
| `matcher` | `text-assertion` *obligatorio* | Comprobación a aplicar al asunto del correo  |

#### Ejemplos:
```gherkin
Entonces el asunto del correo empieza por 'Nueva incidencia'
```


### Comprobar el remitente del último correo
```text copy=true
(que) el remitente del correo {matcher}
```

#### Parámetros:
| nombre    | tipo wakamiti                  | descripción                                    |
|-----------|--------------------------------|------------------------------------------------|
| `matcher` | `text-assertion` *obligatorio* | Comprobación a aplicar al remitente del correo |

#### Ejemplos:
```gherkin
Entonces el remitente del correo es 'support@company.com'
```


### Comprobar el contenido del cuerpo del último correo
```text copy=true
(que) el cuerpo del correo es:
    {data}
```

#### Parámetros:
| nombre | tipo wakamiti            | descripción             |
|--------|--------------------------|-------------------------|
| `data` | `document` *obligatorio* | El contenido a comparar |

#### Ejemplos:
```gherkin
Entonces el cuerpo del correo es:
  """
  Hola,
  Su incidencia ha sido recibida.
  Saludos.
  """
```


### Comprobar parcialmente el contenido del cuerpo del último correo
```text copy=true
(que) el cuerpo del correo contiene lo siguiente:
    {data}
```

#### Parámetros:
| nombre | tipo wakamiti            | descripción             |
|--------|--------------------------|-------------------------|
| `data` | `document` *obligatorio* | El contenido a comparar |

#### Ejemplos:
```gherkin
Entonces el cuerpo del correo contiene lo siguiente:
  """
  Su incidencia ha sido recibida.
  """
```


### Comparar el contenido del cuerpo del último correo con un fichero
```text copy=true
(que) el cuerpo del correo es el contenido del fichero {file}
```

#### Parámetros:
| nombre | tipo wakamiti        | descripción                 |
|--------|----------------------|-----------------------------|
| `file` | `file` *obligatorio* | Ruta del fichero a comparar |

#### Ejemplos:
```gherkin
Entonces el cuerpo del correo es el contenido del fichero 'email.txt'
```


### Comparar parcialmente el contenido del cuerpo del último correo con un fichero
```text copy=true
(que) el cuerpo del correo contiene el contenido del fichero {file}
```

#### Parámetros:
| nombre | tipo wakamiti        | descripción                 |
|--------|----------------------|-----------------------------|
| `file` | `file` *obligatorio* | Ruta del fichero a comparar |

#### Ejemplos:
```gherkin
Entonces el cuerpo del correo contiene el contenido del fichero 'email.txt'
```


### Comprobar el número de adjuntos en el último correo
```text copy=true
(que) el número de adjuntos en el correo {matcher}
```

#### Parámetros:
| nombre    | tipo wakamiti                  | descripción                                  |
|-----------|--------------------------------|----------------------------------------------|
| `matcher` | `text-assertion` *obligatorio* | Comprobación a aplicar al número de adjuntos |

#### Ejemplos:
```gherkin
Entonces el número de adjuntos en el correo es menor que 2
```


### Comprobar que el último correo contiene un adjunto con determinado nombre de fichero
```text copy=true
(que) el correo tiene un fichero adjunto cuyo nombre {matcher}
```

#### Parámetros:
| nombre    | tipo wakamiti                  | descripción                                               |
|-----------|--------------------------------|-----------------------------------------------------------|
| `matcher` | `text-assertion` *obligatorio* | Comprobación a aplicar al nombre de los ficheros adjuntos |

#### Ejemplos:
```gherkin
Entonces el correo tiene un fichero adjunto cuyo nombre es 'attach.txt'
```


### Comparar el contenido de un fichero adjunto en formato binario
```text copy=true
(que) el correo tiene un fichero adjunto con el contenido del fichero binario {file}
```

#### Parámetros:
| nombre | tipo wakamiti        | descripción                 |
|--------|----------------------|-----------------------------|
| `file` | `file` *obligatorio* | Ruta del fichero a comparar |

#### Ejemplos:
```gherkin
Entonces el correo tiene un fichero adjunto con el contenido del fichero binario 'attach.dat'
```


### Comparar el contenido de un fichero adjunto en formato de texto
```text copy=true
(que) el correo tiene un fichero adjunto con el contenido del fichero de texto {file}
```

#### Parámetros:
| nombre | tipo wakamiti        | descripción                 |
|--------|----------------------|-----------------------------|
| `file` | `file` *obligatorio* | Ruta del fichero a comparar |

#### Ejemplos:
```gherkin
Entonces el correo tiene un fichero adjunto con el contenido del fichero de texto 'attach.txt'
```


### Comparar el contenido de un adjunto con el texto indicado
```text copy=true
(que) el correo tiene un fichero adjunto con el siguiente contenido:
    {data}
```

#### Parámetros:
| nombre | tipo wakamiti            | descripción      |
|--------|--------------------------|------------------|
| `data` | `document` *obligatorio* | Texto a comparar |

#### Ejemplos:
```gherkin
Entonces el correo tiene un fichero adjunto con el siguiente contenido:
  """
  Esto es un contenido adjunto
  """
```


### Eliminar todos los correos con un remitente determinado (operación de limpieza)
```text copy=true
Al finalizar, se borran todos los correos cuyo remitente {matcher}
```

#### Parámetros:
| nombre    | tipo wakamiti                  | descripción                                        |
|-----------|--------------------------------|----------------------------------------------------|
| `matcher` | `text-assertion` *obligatorio* | Comprobación a aplicar al remitente de los correos |

#### Ejemplos:
```gherkin
* Al finalizar, se borran todos los correos cuyo remitente es 'test@localhost'
```


### Eliminar todos los correos con un asunto determinado (operación de limpieza)
```text copy=true
Al finalizar, se borran todos los correos cuyo asunto {matcher}
```

#### Parámetros:
| nombre    | tipo wakamiti                  | descripción                                     |
|-----------|--------------------------------|-------------------------------------------------|
| `matcher` | `text-assertion` *obligatorio* | Comprobación a aplicar al asunto de los correos |

#### Ejemplos:
```gherkin
* Al finalizar, se borran todos los correos cuyo asunto empieza por 'Testing'
```

