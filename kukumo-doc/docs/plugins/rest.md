---
title: Rest
date: 2022-09-20
slug: plugins/rest
---

Este plugin proporciona un conjunto de pasos para interactúan con una API RESTful.

**Configuración**:
- [`rest.baseURL`](#restbaseurl)
- [`rest.contentType`](#restcontenttype)
- [`rest.httpCodeThreshold`](#resthttpcodethreshold)
- [`rest.oauth2.url`](#rest-oauth2url)
- [`rest.oauth2.clientId`](#restoauth2clientid)
- [`rest.oauth2.clientSecret`](#restoauth2clientsecret)

**Pasos**:
- [Definir tipo de contenido](#definir-tipo-de-contenido)
- [Definir URL base](#definir-url-base)
- [Definir servicio](#definir-servicio)
- [Definir identificador](#definir-identificador)
- [Definir parámetros de solicitud (request)](#definir-par%C3%A1metros-de-solicitud-request)
- [Definir parámetros de búsqueda (query)](#definir-par%C3%A1metros-de-solicitud-query)
- [Definir parámetros de ruta (path)](#definir-par%C3%A1metros-de-solicitud-path)
- [Definir cabeceras](#definir-cabeceras)
- [Definir timeout](#definir-timeout)
- [Definir umbral de códigos HTTP](#definir-umbral-de-cdigos-http)
- [Definir autenticación básica](#definir-autenticaci%C3%B3n-b%C3%A1sica)
- [Definir token de autenticación](#definir-token-de-autenticaci%C3%B3n)
- [Definir token de autenticación (fichero)](#definir-token-de-autenticaci%C3%B3n-fichero)
- [Definir autenticación oauth2](#definir-autenticaci%C3%B3n-oauth2)
- [Definir archivo adjunto](#definir-archivo-adjunto)
- [Definir archivo adjunto (fichero)](#definir-archivo-adjunto-fichero)
- [Realizar llamada GET](#realizar-llamada-get)
- [Realizar llamada DELETE](#realizar-llamada-delete)
- [Realizar llamada PUT con mensaje](#realizar-llamada-put-con-mensaje)
- [Realizar llamada PUT con mensaje (fichero)](#realizar-llamada-put-con-mensaje-fichero)
- [Realizar llamada PATCH](#realizar-llamada-patch)
- [Realizar llamada PATCH con mensaje](#realizar-llamada-patch-con-mensaje)
- [Realizar llamada PATCH con mensaje (fichero)](#realizar-llamada-patch-con-mensaje-fichero)
- [Realizar llamada POST](#realizar-llamada-post)
- [Realizar llamada POST con mensaje](#realizar-llamada-post-con-mensaje)
- [Realizar llamada POST con mensaje (fichero)](#realizar-llamada-post-con-mensaje-fichero)
- [Comprobar código de respuesta](#comprobar-c%C3%B3digo-de-respuesta)
- [Comprobar mensaje de respuesta](#comprobar-mensaje-de-respuesta)
- [Comprobar mensaje de respuesta (fichero)](#comprobar-mensaje-de-respuesta-fichero)
- [Comprobar fragmento de la respuesta](#comprobar-fragmento-de-la-respuesta)
- [Comprobar tipo de contenido de la respuesta](#comprobar-tipo-de-contenido-de-la-respuesta)
- [Comprobar tamaño de la respuesta](#comprobar-tama%C3%B1o-de-la-respuesta)
- [Comprobar cabecera](#comprobar-cabecera)




## Configuración

---
###  `rest.baseURL`
Establece la URL base para las llamadas REST.
Esto es equivalente al paso `{word} como el tipo de contenido REST` si se prefiere una declaración más descriptiva.

Ejemplo:

```yaml
rest:
  baseURL: http://example.org/api/v2
```

---
### `rest.contentType`
Establece el tipo de contenido que se enviará en la cabecera de las llamadas REST.
Los valores aceptados son:

| literal    | valor de la cabecera `content-type`                                    |
|------------|------------------------------------------------------------------------|
| `ANY`      | `*/*`                                                                  |
| `TEXT`     | `text/plain`                                                           |
| `JSON`     | `application/json, application/javascript, text/javascript, text/json` |
| `XML`      | `application/xml, text/xml, application/xhtml+xml`                     |
| `HTML`     | `text/html`                                                            |
| `URLENC`   | `application/x-www-form-urlencoded`                                    |
| `BINARY`   | `application/octet-stream`                                             |


El valor por defecto es `JSON`.

Ejemplo:

```yaml
rest:
  contentType: XML
```

---
### `rest.httpCodeThreshold`

Establece un límite a los códigos de respuesta HTTP. Cada vez que una llamada REST retorne un
código HTTP igual o superior a este valor, el paso se marcará como fallido automáticamente, sin
comprobar ninguna otra condición.

El valor por defecto es `500`.

Ejemplo:

```yaml
rest:
  httpCodeThreshold: 999
```

---
### `rest.oauth2.url`

Establece el servicio de autenticación [OAuth 2.0][oauth2] que se usará para generar el token que se enviará
en la cabecera HTTP `Authorization` de las llamadas REST.

Ejemplo:

```yaml
rest:
  oauth2: 
    url: https://accounts.google.com/o/oauth2/auth
```


---
### `rest.oauth2.clientId`
Establece el parámetro `clientId` para el servicio de autenticación [OAuth 2.0][oauth2] definido
por el valor de la propiedad de configuración `rest.oauth2.url`.

Ejemplo:

```yaml
rest:
  oauth2: 
    clientId: WEB_APP
```

---
### `rest.oauth2.clientSecret`
Establece el parámetro `clientSecret` para el servicio de autenticación [OAuth 2.0][oauth2] definido
por el valor de la propiedad de configuración `rest.oauth2.url`.

Ejemplo:

```yaml
rest:
  oauth2: 
    clientSecret: ABRACADABRAus1ZMGHvq9R
```



## Pasos

---
### Definir tipo de contenido
```
{type} como el tipo de contenido REST
```
Establece el tipo de contenido de la API en la cabecera `content-type`. Este paso es equivalente a configurar la 
propiedad [`rest.contentType`](#restcontenttype). 

#### Parámetros:
| Nombre | Kukumo type | Descripción        |
|--------|-------------|--------------------|
| `type` | `word`      | La URL de conexión |

#### Ejemplos:
```gherkin
  Dado XML como el tipo de contenido REST
```

---
### Definir URL base
```
la URL base {url}
```
Establece la ruta base de la API. Este paso es equivalente a configurar la propiedad [`rest.baseURL`](#restbaseurl). 

#### Parámetros:
| Nombre | Kukumo type | Descripción |
|--------|-------------|-------------|
| `url`  | `url`       | URL base    |

#### Ejemplos:
```gherkin
  Dada la URL base http//example.org/api
```

---
### Definir servicio
```
el servicio REST {service}
```
Establece la ruta del servicio a probar. Se concatenará al valor de la [url base](#definir-url-base).

#### Parámetros:
| Nombre    | Kukumo type | Descripción  |
|-----------|-------------|--------------|
| `service` | `text`      | Segmento URL |

#### Ejemplos:
```gherkin
  Dado el servicio REST '/users`
```

---
### Definir identificador
```
* identificad(o|a|os|as) por {text}
```
Establece un identificador de recurso REST para ser usado por el servicio. Se concatenará al valor de la 
[url base](#definir-url-base) y del [servicio](#definir-servicio) en concreto.

#### Parámetros:
| Nombre | Kukumo type | Descripción                 |
|--------|-------------|-----------------------------|
| `text` | `text`      | Un identificador de recurso |

#### Ejemplos:
```gherkin
  Dado un usuario identificado por 'john'
```
```gherkin
  Dado un libro identificado por '978-3-16-148410-0'
```

---
### Definir parámetros de solicitud (request)
```
los siguiente parámetros de solicitud:
```
Establece los parámetros de la petición REST. Estos parámetros se enviaran como datos de formulario.

##### Parámetros:
| Nombre | Kukumo type | Descripción                                   |
|--------|-------------|-----------------------------------------------|
|        | `table`     | Una tabla con las columnas `nombre` y `valor` |

##### Ejemplos:
```gherkin
  Dados los siguiente parámetros de solicitud:
    | nombre | valor    |
    | age    | 13       |
    | city   | Valencia |
```

---
### Definir parámetros de solicitud (query)
```
los siguiente parámetros de búsqueda:
```
Establece los parámetros de la petición REST. Estos parámetros se concatenerán a la URL de la petición tras la ruta, 
por ejemplo `/user?param1=abc&param2=123`.

##### Parámetros:
| Nombre | Kukumo type | Descripción                                   |
|--------|-------------|-----------------------------------------------|
|        | `table`     | Una tabla con las columnas `nombre` y `valor` |

##### Ejemplos:
```gherkin
  Dados los siguiente parámetros de solicitud:
    | nombre | valor    |
    | age    | 13       |
    | city   | Valencia |
```

---
### Definir parámetros de solicitud (path)
```
los siguiente parámetros de ruta:
```
Establece los parámetros de la petición REST. Estos parámetros formarán parte de la ruta de la URL, sustituyendo a los 
fragmentos indicados con `{}`.

##### Parámetros:
| Nombre | Kukumo type | Descripción                                   |
|--------|-------------|-----------------------------------------------|
|        | `table`     | Una tabla con las columnas `nombre` y `valor` |

##### Ejemplos:
```gherkin
  Dado el servicio 'user/{usuario}/items/{item}'
  Y los siguientes parámetros de ruta:
    | nombre  | valor    |
    | usuario | 25       |
    | item    | 7        |
```

---
### Definir cabeceras
```
las siguientes cabeceras:
```
Establece las cabeceras de la petición REST.

##### Parámetros:
| Nombre | Kukumo type | Descripción                                   |
|--------|-------------|-----------------------------------------------|
|        | `table`     | Una tabla con las columnas `nombre` y `valor` |

##### Ejemplos:
```gherkin
  Dadas las siguientes cabeceras:
    | nombre       | valor |
    | Age          | 3600  |
    | Keep-Alive   | 1200  |
```

---
### Definir timeout
```
un timeout de {int} (mili)segundos
```
Establece un tiempo máximo de respuesta (en segundos o milisegundos) para las siguientes peticiones HTTP.

##### Parámetros:
| Nombre | Kukumo type | Descripción      |
|--------|-------------|------------------|
| `int`  | `int`       | El tiempo máximo |

##### Ejemplos:
```gherkin
  Dado un timeout de 12000 milisegundos
```
```gherkin
  Dado un timeout de 10 segundos
```

---
### Definir umbral de códigos HTTP
```
(que) toda petición se considera fallida si su código HTTP {matcher}
```
Establece una validación general para el código HTTP de todas las respuestas siguientes. Es similar a la propiedad de 
configuración [`rest.httpCodeTreshold`](#resthttpcodethreshold) pero con una validación de enteros personalizada.

##### Parámetros:
| Nombre    | Kukumo type         | Descripción               |
|-----------|---------------------|---------------------------|
| `matcher` | `integer-assertion` | Una validación de enteros |

##### Ejemplo:
```gherkin
  * toda petición se considera fallida si su código HTTP es igual o mayor que 500
```

---
### Definir autenticación básica
```
(que) el servicio usa autenticación básica con las credenciales {username}:{password}
```
Establece las credenciales de autenticación básica que se enviarán en la cabecera HTTP `Authorization`.

##### Parámetros:
| Nombre     | Kukumo type | Descripción       |
|------------|-------------|-------------------|
| `username` | `text`      | Nombre de usuario |
| `password` | `text`      | Contraseña        |

##### Ejemplos:
```gherkin
  Dado que el servicio usa autenticación básica con las credenciales 'us1532':'xxxxx'
```

---
### Definir token de autenticación
```
(que) el servicio usa el token de autenticación {text}
```
Establece el token de autenticación que se enviará en la cabecera `Authorization` para las siguientes peticiones. 

##### Parámetros:
| Nombre | Kukumo type | Descripción            |
|--------|-------------|------------------------|
| `text` | `text`      | Token de autenticación |

##### Ejemplos:
```gherkin
  Dado que el servicio usa el token de autenticación 'hudytw9834y9cqy32t94'
```

---
### Definir token de autenticación (fichero)
```
(que) el servicio usa el token de autenticación del fichero {file}
```
Establece el token de autenticación que se enviará en la cabecera `Authorization` para las siguientes llamadas, obtenido 
desde un fichero.

##### Parámetros:
| Nombre | Kukumo type  | Descripción                           |
|--------|--------------|---------------------------------------|
| `file` | `file`       | Fichero con el token de autenticación |

##### Ejemplo:
```gherkin
  Dado que el servicio usa el token de autenticación del fichero 'token.txt'
```

---
### Definir autenticación oauth2
```
(que) el servicio usa el proveedor de autenticación con los siguientes datos:
```
Establece el cuerpo que se enviará al servicio de autenticación.

##### Parámetros:
| Nombre | Kukumo type | Descripción                                |
|--------|-------------|--------------------------------------------|
|        | `document`  | Cadena con los parámetros de autenticación |

##### Ejemplos:
```gherkin
  Dado que el servicio usa el proveedor de autenticación con los siguientes datos:
    """
    grant_type=password&username=OficinaTest4&password=xxxxx
    """
```

---
### Definir archivo adjunto
```
(que) se incluye el fichero adjunto con los siguientes datos:
```
Indica el texto que se incluirá como fichero adjunto en datos de formulario.

##### Parámetros:
| Nombre | Kukumo type | Descripción          |
|--------|-------------|----------------------|
|        | `document`  | Contenido a adjuntar |

##### Ejemplos:
```gherkin
  Dado que se incluye el fichero adjunto con los siguientes datos:
    """
    Contenido del fichero
    """
```

---
### Definir archivo adjunto (fichero)
```
(que) se incluye el fichero adjunto {file}
```
Indica el fichero cuyo contenido se incluirá como fichero adjunto en datos de formulario.

##### Parámetros:
| Nombre | Kukumo type | Descripción                         |
|--------|-------------|-------------------------------------|
| `file` | `file`      | Fichero con el contenido a adjuntar |

##### Ejemplos:
```gherkin
  Dado que se incluye el fichero adjunto 'data.txt'
```

---
### Realizar llamada GET
```
se realiza la búsqueda *
```
```
se consulta(n) *
```
Envía una petición `GET` al servicio con los parámetros definidos previamente.

##### Ejemplos:
```gherkin
  Cuando se realiza la búsqueda de usuarios
```
```gherkin
  Cuando se consulta el usuario
```

---
### Realizar llamada DELETE
```
se elimina(n) *
```
Envía una petición `DELETE` al servicio y recurso REST definido previamente.

##### Ejemplos:
```gherkin
  Cuando se elimina el usuario
```

---
### Realizar llamada PUT con mensaje
```
se reemplaza(n) * con los siguientes datos:
```
Envía una petición `PUT` al servicio y recurso REST definido previamente. El cuerpo de la petición será el contenido 
indicado a continuación.

##### Parámetros:
| Nombre | Kukumo type | Descripción              |
|--------|-------------|--------------------------|
|        | `document`  | El cuerpo de la petición |

##### Ejemplos:
```gherkin
  Cuando se reemplaza el usuario con los siguientes datos:
    """json
    {
        "firstName": "John",
        "lastName": "Doe",
        "birthDate": "1980-02-20",
        "address": "221B, Baker Street"
    }
    """
```

---
### Realizar llamada PUT con mensaje (fichero)
```
se reemplaza(n) * con los datos del fichero {file}
```
Envía una petición `PUT` al servicio y recurso REST definido previamente. El cuerpo de la petición será el contenido del 
fichero indicado.

##### Parámetros:
| Nombre | Kukumo type | Descripción                          |
|--------|-------------|--------------------------------------|
| `file` | `file`      | Fichero con el cuerpo de la petición |

##### Ejemplos:
```gherkin
  Cuando se reemplaza el usuario con los datos del fichero 'data/user123.json'
```

---
### Realizar llamada PATCH
```
se modifica(n) * 
```
Envía una petición `PATCH` al servicio y recurso REST definido previamente. 

##### Ejemplos:
```gherkin
  Cuando se modifica el usuario
```

---
### Realizar llamada PATCH con mensaje
```
se modifica(n) * con los siguientes datos:
```
Envía una petición `PATCH` al servicio y recurso REST definido previamente. El cuerpo de la petición será el contenido 
indicado a continuación.

##### Parámetros:
| Nombre | Kukumo type | Descripción              |
|--------|-------------|--------------------------|
|        | `document`  | El cuerpo de la petición |

##### Ejemplos:
```gherkin
  Cuando se modifica el usuario con los siguientes datos:
    """json
    {
        "firstName": "Jim"
    }
    """
```

---
### Realizar llamada PATCH con mensaje (fichero)
```
se modifica(n) * con los datos del fichero {file}
```
Envía una petición `PATCH` al servicio y recurso REST definido previamente. El cuerpo de la petición será el contenido 
del fichero indicado.

##### Parámetros:
| Nombre | Kukumo type | Descripción                          |
|--------|-------------|--------------------------------------|
| `file` | `file`      | Fichero con el cuerpo de la petición |

##### Ejemplos:
```gherkin
  Cuando se modifica el usuario con los datos del fichero 'data/user123.json'
```

---
### Realizar llamada POST
```
se crea(n) *
```
Envía una petición `POST` al servicio definido previamente.

##### Ejemplo:
```gherkin
  Cuando se crea el usuario
```

---
### Realizar llamada POST con mensaje
```
se crea(n) * con los siguientes datos:
```
```
se envía al servicio los siguientes datos:
```
Envía una petición `POST` al servicio definido previamente. El cuerpo de la petición se rellenará con el contenido 
indicado a continuación.

##### Parámetros:
| Nombre | Kukumo type | Descripción              |
|--------|-------------|--------------------------|
|        | `document`  | El cuerpo de la petición |

##### Ejemplos:
```gherkin
  Cuando se crea un usuario con los siguientes datos:
    """json
    {
        "firstName": "John",
        "lastName": "Doe",
        "birthDate": "1980-02-20",
        "address": "221B, Baker Street"
    }
    """
```
```gherkin
  Cuando se envía al servicio los siguientes datos:
    """json
    {
        "date": "2021-10-30"
    }
    """
```

---
### Realizar llamada POST con mensaje (fichero)
```
se crea(n) * con los datos del fichero {file}
```
```
se envía al servicio los datos del fichero {file}
```
Envía una petición `POST` al servicio definido previamente. El cuerpo de la petición se rellenará con el contenido del 
fichero indicado.

##### Parámetros:
| Nombre | Kukumo type | Descripción                          |
|--------|-------------|--------------------------------------|
| `file` | `file`      | Fichero con el cuerpo de la petición |

##### Ejemplos:
```gherkin
  Cuando se crea un usuario con los datos del fichero 'data/user123.json'
```
```gherkin
  Cuando se envía al servicio los datos del fichero 'booking.json'
```

---
### Comprobar código de respuesta
```
el código de respuesta HTTP {matcher}
```
Comprueba que el código HTTP de la última respuesta satisface una validación de enteros.

##### Parámetros:
| Nombre    | Kukumo type         | Descripción               |
|-----------|---------------------|---------------------------|
| `matcher` | `integer-assertion` | Una validación de enteros |

##### Ejemplos:
```gherkin
  Entonces el código de respuesta HTTP es 201
```


---
### Comprobar mensaje de respuesta
```
la respuesta es exactamente:
```
Valida que el cuerpo de la respuesta sea exacto al indicado, incluyendo el orden de los campos.
```
la respuesta es exactamente (en cualquier orden):
```
Valida que el cuerpo de la respuesta sea exacto al indicado, pero pueden llegar los campos en diferente orden.
```
la respuesta es parcialmente:
```
Valida que el cuerpo de la respuesta incluya, al menos, los campos indicados.

##### Parámetros:
| Nombre | Kukumo type | Descripción           |
|--------|-------------|-----------------------|
|        | `document`  | El contenido esperado |

##### Ejemplos:
```gherkin
  Entonces la respuesta es exactamente:
    """json
    {
        "age": 23,
        "name": "John"
    }
    """
```

---
### Comprobar mensaje de respuesta (fichero)
```
la respuesta es exactamente el contenido del fichero {file}
```
Valida que el cuerpo de la respuesta sea exacto al indicado en el fichero, incluyendo el orden de los campos.
```
la respuesta es exactamente el contenido del fichero {file} \(en cualquier orden\)
```
Valida que el cuerpo de la respuesta sea exacto al indicado en el fichero, pero pueden llegar los campos en diferente orden.
```
la respuesta es parcialmente el contenido del fichero {file}
```
Valida que el cuerpo de la respuesta incluya, al menos, los campos indicados en el fichero.

##### Parámetros:
| Nombre | Kukumo type | Descripción          |
|--------|-------------|----------------------|
| `file` | `file`      | Un fichero existente |

##### Ejemplos:
```gherkin
  Entonces la respuesta es parcialmente el contenido del fichero 'data/response1.json'
```

---
### Comprobar fragmento de la respuesta
```
el (texto|entero|decimal) del fragmento de la respuesta {fragment} {matcher}
```
Valida el valor (*texto*, *entero* o *decimal*) de un fragmento del cuerpo de respuesta, localizado mediante una ruta dada
(usando [JSONPath][jsonpath] o [XPath][xpath] dependiendo del tipo de contenido).

##### Parámetros:
| Nombre     | Kukumo type   | Descripción                 |
|------------|---------------|-----------------------------|
| `fragment` | `text`        | Una ruta JSONPath or XPath  |
| `matcher`  | `*-assertion` | El comparador del fragmento |
`*`: `text`, `integer` o `decimal`, dependiendo del tipo indicado en el paso.

##### Ejemplos:
```gherkin
  Entonces el decimal del fragmento de la respuesta 'users[1].account.availableMoney` es mayor que 23.57
```
```gherkin
  Entonces el texto del fragmento de la respuesta 'users[1].name` no es 'John'
```


---
### Comprobar tipo de contenido de la respuesta
```
el tipo de contenido de la respuesta es {word}
```
Valida que el tipo de contenido de la última respuesta es el esperado.
Este paso equivale a validar que el valor de la cabecera `Content-Type` de la respuesta es el tipo MIME correspondiente.

##### Parámetros:
| Nombre  | Kukumo type | Descripción                                        |
|---------|-------------|----------------------------------------------------|
| `word`  | `word`      | `ANY`,`TEXT`,`JSON`,`XML`,`HTML`,`URLENC`,`BINARY` |

##### Ejemplos:
```gherkin
  Entonces el tipo de contenido de la respuesta es JSON
```

---
### Comprobar tamaño de la respuesta
```
el tamaño de la respuesta {matcher}
```
Comprueba que la longitud en bytes de la última respuesta satisface una validación.

##### Parámetros:
| Nombre    | Kukumo type         | Descripción               |
|-----------|---------------------|---------------------------|
| `matcher` | `integer-assertion` | Una validación de enteros |

##### Ejemplos:
```gherkin
  Entonces el tamaño de la respuesta es menor que 500
```

---
### Comprobar cabecera
```
el (texto|entero|decimal) de la cabecera de la respuesta {name} {matcher}
```
Comprueba que una determinada cabecera HTTP en la última respuesta satisface una validación de *texto*, *entero* o 
*decimal*.

##### Parámetros:
| Nombre    | Kukumo type   | Descripción                 |
|-----------|---------------|-----------------------------|
| `name`    | `text`        | Nombre de la cabecera       |
| `matcher` | `*-assertion` | El comparador del fragmento |
`*`: `text`, `integer` o `decimal`, dependiendo del tipo indicado en el paso.

##### Ejemplos:
```gherkin
  Entonces el texto de la cabecera de la respuesta Content-Encoding contiene 'gzip'
```
```gherkin
  Entonces el entero de la cabecera de la respuesta Age es mayor que 10
```



[oauth2]: https://datatracker.ietf.org/doc/html/rfc6749 (OAuth 2.0)
[jsonschema]: https://json-schema.org/ (JSON Schema)
[jsonpath]: https://goessner.net/articles/JsonPath/
[xmlschema]: https://www.w3.org/2001/XMLSchema (XML Schema)
[xpath]: https://en.wikipedia.org/wiki/XPath (XPath)
