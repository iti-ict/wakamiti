---
title: Pasos REST
date: 2022-09-20
slug: /plugins/rest
---


Este plugin proporciona un conjunto de pasos para interactuar con una API RESTful.


---
## Tabla de contenido

---


## Instalación


Incluye el módulo en la sección correspondiente.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:rest-wakamiti-plugin:2.4.0
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>rest-wakamiti-plugin</artifactId>
  <version>2.4.0</version>
</dependency>
```


## Configuración

### `rest.baseURL`
- Tipo: `URL` 

Establece la URL base para las llamadas REST. Esta configuración es equivalente al paso 
[Definir URL base](#definir-url-base) si se prefiere una declaración más descriptiva.

Ejemplo:
```yaml
rest:
  baseURL: https://example.org/api/v2
```


### `rest.contentType`
- Tipo: `string`
- Por defecto: `JSON`

Establece el tipo de contenido que se enviará en la cabecera de las llamadas REST.
Los valores aceptados son:

| literal     | valor de la cabecera `content-type`                                                                                                                                                                                      |
|-------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `ANY`       | `*/*`                                                                                                                                                                                                                    |
| `TEXT`      | `text/plain`                                                                                                                                                                                                             |
| `JSON`      | `application/json, application/javascript, text/javascript, text/json`                                                                                                                                                   |
| `XML`       | `application/xml, text/xml, application/xhtml+xml`                                                                                                                                                                       |
| `HTML`      | `text/html`                                                                                                                                                                                                              |
| `URLENC`    | `application/x-www-form-urlencoded`                                                                                                                                                                                      |
| `BINARY`    | `application/octet-stream`                                                                                                                                                                                               |
| `MULTIPART` | `multipart/form-data`, `multipart/alternative`, `multipart/byteranges`, `multipart/digest`, `multipart/mixed`, `multipart/parallel`, `multipart/related`, `multipart/report`, `multipart/signed`, `multipart/encrypted`  |

Ejemplo:
```yaml
rest:
  contentType: XML
```


### `rest.httpCodeThreshold`
- Tipo: `integer`
- Por defecto: `500`

Establece un límite a los códigos de respuesta HTTP. Cada vez que una llamada REST retorne un código HTTP igual o
superior a este valor, el paso se marcará como fallido automáticamente, sin comprobar ninguna otra condición.

Ejemplo:
```yaml
rest:
  httpCodeThreshold: 999
```


### `rest.timeout`
- Tipo: `integer`
- Por defecto: `60000`

Establece un tiempo máximo de respuesta (en milisegundos) para las siguientes peticiones HTTP. En el caso de exceder el
tiempo indicado se detendrá la llamada y se producirá un error.

Ejemplo:
```yaml
rest:
  timeout: 10000
```


### `rest.oauth2.url`
- Tipo: `URL`

Establece el servicio de autenticación [OAuth 2.0][oauth2] que se usará para generar el token que se enviará en la
cabecera HTTP `Authorization` de las llamadas REST.

Ejemplo:
```yaml
rest:
  oauth2: 
    url: https://accounts.google.com/o/oauth2/auth
```


### `rest.oauth2.clientId`
- Tipo: `string`

Establece el parámetro `clientId` para el servicio de autenticación [OAuth 2.0][oauth2] definido por el valor de la
propiedad de configuración `rest.oauth2.url`.

Ejemplo:
```yaml
rest:
  oauth2: 
    clientId: WEB_APP
```


### `rest.oauth2.clientSecret`
- Tipo: `string`

Establece el parámetro `clientSecret` para el servicio de autenticación [OAuth 2.0][oauth2] definido por el valor de la
propiedad de configuración `rest.oauth2.url`.

Ejemplo:
```yaml
rest:
  oauth2: 
    clientSecret: ABRACADABRAus1ZMGHvq9R
```


### `rest.oauth2.cached`
- Tipo: `boolean`
- Por defecto: `false`

Establece si el token recuperado se guarda en caché para evitar llamadas recurrentes al servicio oauth si los datos son
los mismos.

Ejemplo:
```yaml
rest:
  oauth2:
    cached: true
```


### `rest.oauth2.parameters`
- Tipo: `property[]`

Establece los parámetros por defecto de la autenticación oauth.

Ejemplo:
```yaml
rest:
  oauth2:
    parameters:
      grant_type: password
      username: pepe
      password: 1234asdf
      scope: something
```


### `rest.config.multipart.subtype`
- Tipo: `string`
- Por defecto: `form-data`

Establece el subtipo de las llamadas multiparte. Los valores disponibles son:

| literal       |
|---------------|
| `form-data`   |
| `alternative` | 
| `byteranges`  | 
| `digest`      |
| `mixed`       | 
| `parallel`    | 
| `related`     |
| `report`      |
| `signed`      |
| `encrypted`   |

Ejemplo:
```yaml
rest:
  config:
    multipart:
      subtype: mixed
```


### `rest.config.multipart.filename`
- Tipo: `string`
- Por defecto: `file`

Establece el nombre fichero de las llamadas multiparte.

Ejemplo:
```yaml
rest:
  config:
    multipart:
      filename: otro_nombre
```


### `rest.config.redirect.follow`
- Tipo: `boolean`
- Por defecto: `true`

Establece si se permite seguir las redirecciones en las llamadas HTTP.

Ejemplo:
```yaml
rest:
  config:
    redirect:
      follow: false
```


### `rest.config.redirect.allowCircular`
- Tipo: `boolean`
- Por defecto: `false`

Establece si se permite las redirecciones circulares en las llamadas HTTP.

Ejemplo:
```yaml
rest:
  config:
    redirect:
      allowCircular: true
```


### `rest.config.redirect.rejectRelative`
- Tipo: `boolean`
- Por defecto: `false`

Establece si se rechazan las redirecciones relativas en las llamadas HTTP.

Ejemplo:
```yaml
rest:
  config:
    redirect:
      rejectRelative: true
```


### `rest.config.redirect.max`
- Tipo: `integer`
- Por defecto: `100`

Establece el número de redirecciones máximo en las llamadas HTTP.

Ejemplo:
```yaml
rest:
  config:
    redirect:
      max: 150
```


## Pasos


### Definir tipo de contenido
```text copy=true
{type} como el tipo de contenido REST
```
Establece el tipo de contenido de la API en la cabecera `content-type`. Este paso es equivalente a configurar la
propiedad [`rest.contentType`](#restcontenttype).

#### Parámetros:
| Nombre | Wakamiti type        | Descripción        |
|--------|----------------------|--------------------|
| `type` | `word` *obligatorio* | La URL de conexión |

#### Ejemplos:
```gherkin
Dado XML como el tipo de contenido REST
```


### Definir URL base
```text copy=true
la URL base {url}
```
Establece la ruta base de la API. Este paso es equivalente a configurar la propiedad [`rest.baseURL`](#restbaseurl).

#### Parámetros:
| Nombre | Wakamiti type       | Descripción |
|--------|---------------------|-------------|
| `url`  | `url` *obligatorio* | URL base    |

#### Ejemplos:
```gherkin
Dada la URL base https//example.org/api
```


### Definir servicio
```text copy=true
el servicio REST {service}
```
Establece la ruta del servicio a probar. Se concatenará al valor de la [url base](#definir-url-base).

#### Parámetros:
| Nombre    | Wakamiti type        | Descripción  |
|-----------|----------------------|--------------|
| `service` | `text` *obligatorio* | Segmento URL |

#### Ejemplos:
```gherkin
Dado el servicio REST '/users`
```


### Definir identificador
##### Deprecado
```text copy=true
* identificad(o|a|os|as) por {text}
```
Establece un identificador de recurso REST para ser usado por el servicio. Se concatenará al valor de la
[url base](#definir-url-base) y del [servicio](#definir-servicio) en concreto.

#### Parámetros:
| Nombre | Wakamiti type        | Descripción                 |
|--------|----------------------|-----------------------------|
| `text` | `text` *obligatorio* | Un identificador de recurso |

#### Ejemplos:
```gherkin
Dado un usuario identificado por 'john'
```
```gherkin
Dado un libro identificado por '978-3-16-148410-0'
```


### Definir parámetros o cabeceras
```text copy=true
el parámetro de (solicitud|búsqueda|ruta|formulario) {name} con el valor {value}
```
```text copy=true
las cabecera {name} con el valor {value}
```
Establece una cabecera o parámetro de petición, búsqueda, ruta o formulario REST. Los parámetros de petición se enviarán
como datos de formulario en las llamadas POST, los parámetros de búsqueda se concatenarán a la URL de la petición tras
la ruta (p.e. `/user?param1=abc&param2=123`), los parámetros de ruta reemplazarán los fragmentos de la ruta del servicio
indicados con llaves `{}` y los parámetros de formulario se enviarán con el content-type `application/x-www-form-urlencoded`.

#### Parámetros:
| Nombre  | Wakamiti type        | Descripción                     |
|---------|----------------------|---------------------------------|
| `name`  | `text` *obligatorio* | Nombre del parámetro o cabecera |
| `value` | `text` *obligatorio* | Valor del parámetro o cabecera  |

#### Ejemplos:
```gherkin
Dado el parámetro de solicitud 'age' con el valor '13'
Cuando se envía al servicio la información
```
```gherkin
Dado el parámetro de búsqueda 'city' con el valor 'Valencia'
Cuando se realiza la búsqueda del usuario
```
```gherkin
Dado el servicio 'user/{usuario}/items'
Y el parámetro de ruta 'usuario' con el valor '25'
```
```gherkin
Dado el parámetro de formulario 'city' con el valor 'Valencia'
Cuando se envía al servicio la información
```
```gherkin
Dada la cabeceras 'Keep-alive' con el valor '1200'
```


### Definir parámetros o cabeceras (tabla)
```text copy=true
los siguientes parámetros de (solicitud|búsqueda|ruta|formulario):
    {table}
```
```text copy=true
las siguientes cabeceras:
    {table}
```
Establece varias cabeceras o parámetros de petición, búsqueda, ruta o formulario REST. Los parámetros de petición se
enviarán como datos de formulario en las llamadas POST, los parámetros de búsqueda se concatenarán a la URL de la
petición tras la ruta (p.e. `/user?param1=abc&param2=123`), los parámetros de ruta reemplazarán los fragmentos de la
ruta del servicio indicados con llaves `{}` y los parámetros de formulario se enviarán con el content-type
`application/x-www-form-urlencoded`.


#### Parámetros:
| Nombre  | Wakamiti type         | Descripción                                   |
|---------|-----------------------|-----------------------------------------------|
| `table` | `table` *obligatorio* | Una tabla con las columnas `nombre` y `valor` |

#### Ejemplos:
```gherkin
Dados los siguiente parámetros de solicitud:
  | nombre | valor    |
  | age    | 13       |
  | city   | Valencia |
Cuando se envía al servicio la información
```
```gherkin
Dados los siguiente parámetros de búsqueda:
  | nombre | valor    |
  | age    | 13       |
  | city   | Valencia |
Cuando se realiza la búsqueda del usuario
```
```gherkin
Dado el servicio 'user/{usuario}/items/{item}'
Y los siguientes parámetros de ruta:
  | nombre  | valor    |
  | usuario | 25       |
  | item    | 7        |
```
```gherkin
Dados los siguiente parámetros de formulario:
  | nombre | valor    |
  | age    | 13       |
  | city   | Valencia |
Cuando se envía al servicio la información
```
```gherkin
Dadas las siguientes cabeceras:
  | nombre       | valor |
  | Age          | 3600  |
  | Keep-Alive   | 1200  |
```


### Definir timeout
```text copy=true
un timeout de {duration}
```
Establece un tiempo máximo de respuesta para las siguientes peticiones HTTP. En el caso de
exceder el tiempo indicado se detendrá la llamada y se producirá un error.

#### Parámetros:
| Nombre     | Wakamiti type               | Descripción       |
|------------|-----------------------------|-------------------|
| `duration` | [duration][2] *obligatorio* | El tiempo máximo  |

#### Ejemplos:
```gherkin
Dado un timeout de 12000 milisegundos
```
```gherkin
Dado un timeout de 10 segundos
```


### Definir umbral de códigos HTTP
```text copy=true
(que) toda petición se considera satisfactoria si su código HTTP {matcher}
```
Establece una validación general para el código HTTP de todas las respuestas siguientes. Es similar a la propiedad de
configuración [`rest.httpCodeTreshold`](#resthttpcodethreshold) pero con una validación de enteros personalizada.

#### Parámetros:
| Nombre    | Wakamiti type                     | Descripción              |
|-----------|-----------------------------------|--------------------------|
| `matcher` | `integer-assertion` *obligatorio* | [Comparador][1] numérico |

#### Ejemplo:
```gherkin
* toda petición se considera satisfactoria si su código HTTP es menor que 500
```


### Definir autenticación básica
```text copy=true
(que) el servicio usa autenticación básica con las credenciales {username}:{password}
```
Establece las credenciales de autenticación básica que se enviarán en la cabecera HTTP `Authorization`.

#### Parámetros:
| Nombre     | Wakamiti type        | Descripción       |
|------------|----------------------|-------------------|
| `username` | `text` *obligatorio* | Nombre de usuario |
| `password` | `text` *obligatorio* | Contraseña        |

#### Ejemplos:
```gherkin
Dado que el servicio usa autenticación básica con las credenciales 'us1532':'xxxxx'
```


### Definir autenticación oauth2
```text copy=true
(que) el servicio usa autenticación oauth
```
Establece el token de autenticación "bearer" que se enviará en la cabecera `Authorization`, que se recupera previamente
del servicio oauth2 configurado ([url](#restoauth2url), [clientId](#restoauth2clientid),
[clientSecret](#restoauth2clientsecret), [parámetros](#restoauth2parameters)), para las siguientes peticiones.

#### Ejemplos:
```gherkin
Dado que el servicio usa autenticación oauth
```


### Definir autenticación oauth2 por token
```text copy=true
(que) el servicio usa autenticación oauth con el token {token}
```
Establece el token de autenticación "bearer" que se enviará en la cabecera `Authorization` para las siguientes
peticiones.

#### Parámetros:
| Nombre  | Wakamiti type        | Descripción            |
|---------|----------------------|------------------------|
| `token` | `text` *obligatorio* | token de autenticación |

#### Ejemplos:
```gherkin
Dado que el servicio usa autenticación oauth con el token 'hudytw9834y9cqy32t94'
```


### Definir autenticación oauth2 por token (fichero)
```text copy=true
(que) el servicio usa autenticación oauth con el token del fichero {file}
```
Establece el token de autenticación "bearer" que se enviará en la cabecera `Authorization` para las siguientes llamadas,
obtenido desde un fichero.

#### Parámetros:
| Nombre | Wakamiti type        | Descripción                           |
|--------|----------------------|---------------------------------------|
| `file` | `file` *obligatorio* | Fichero con el token de autenticación |

#### Ejemplo:
```gherkin
Dado que el servicio usa autenticación oauth con el token del fichero 'token.txt'
```


### Definir autenticación oauth2 por credenciales
```text copy=true
(que) el servicio usa autenticación oauth con las credenciales {username}:{password}
```
```text copy=true
(que) el servicio usa autenticación oauth con las credenciales {username}:{password} y los siguientes parámetros:
    {table}
```
Establece el token de autenticación "bearer" que se enviará en la cabecera `Authorization`, que se recupera previamente
del servicio oauth2 configurado ([url](#restoauth2url), [clientId](#restoauth2clientid),
[clientSecret](#restoauth2clientsecret)), usando las credenciales indicadas, para las siguientes peticiones.

También se pueden añadir más parámetros adicionales admitidos por `Oauth` mediante una tabla.

#### Parámetros:
| Nombre     | Wakamiti type        | Descripción                                   |
|------------|----------------------|-----------------------------------------------|
| `username` | `text` *obligatorio* | Nombre de usuario                             |
| `password` | `text` *obligatorio* | Contraseña                                    |
| `table`    | `table`              | Una tabla con las columnas `nombre` y `valor` |

##### Ejemplos:
```gherkin
Dado que el servicio usa autenticación oauth con las credenciales 'us1532':'xxxxx'
```

```gherkin
Dado que el servicio usa autenticación oauth con las credenciales 'us1532':'xxxxx' y los siguientes parámetros:
  | name  | value     |
  | scope | something |
```


### Definir autenticación oauth2 por cliente
```text copy=true
(que) el servicio usa autenticación oauth
```
```text copy=true
(que) el servicio usa autenticación oauth con los siguientes parámetros:
    {table}
```
Establece el token de autenticación "bearer" que se enviará en la cabecera `Authorization`, que se recupera previamente
del servicio oauth2 configurado ([url](#restoauth2url), [clientId](#restoauth2clientid),
[clientSecret](#restoauth2clientsecret)), usando los datos del cliente, para las siguientes peticiones.

También se pueden añadir más parámetros adicionales admitidos por `Oauth` mediante una tabla.

#### Parámetros:
| Nombre  | Wakamiti type | Descripción                                   |
|---------|---------------|-----------------------------------------------|
| `table` | `table`       | Una tabla con las columnas `nombre` y `valor` |


#### Ejemplos:
```gherkin
Dado que el servicio usa autenticación oauth
```

```gherkin
Dado que el servicio usa autenticación oauth con los siguientes parámetros:
  | name  | value     |
  | scope | something |
```


### Limpiar autenticación
```text copy=true
(que) el servicio no usa autenticación
```
Elimina la cabecera con la autenticación.

#### Ejemplos:
```gherkin
Dado que el servicio no usa autenticación
```


### Definir subtipo multiparte
```text copy=true
{type} como subtipo multiparte
```
Establece el subtipo por defecto de las llamadas multiparte. Este paso es equivalente a configurar la propiedad
[`rest.config.multipart.subtype`](#restconfigmultipartsubtype). Los valores disponibles son:

| literal       |
|---------------|
| `form-data`   |
| `alternative` | 
| `byteranges`  | 
| `digest`      |
| `mixed`       | 
| `parallel`    | 
| `related`     |
| `report`      |
| `signed`      |
| `encrypted`   |

El valor por defecto es `form-data`.

#### Parámetros:
| Nombre | Wakamiti type        | Descripción        |
|--------|----------------------|--------------------|
| `type` | `text` *obligatorio* | Subtipo multiparte |

#### Ejemplos:
```gherkin
Dado 'mixed' como subtipo multiparte
```


### Definir nombre de fichero multiparte
```text copy=true
{name} como nombre de fichero adjunto
```
Establece el nombre por defecto de los ficheros multiparte. Este paso es equivalente a configurar la propiedad
[`rest.config.multipart.filename`](#restconfigmultipartfilename).

#### Parámetros:
| Nombre | Wakamiti type        | Descripción                  |
|--------|----------------------|------------------------------|
| `name` | `text` *obligatorio* | Nombre de fichero multiparte |

#### Ejemplos:
```gherkin
Dado 'otro_nombre' como nombre de fichero adjunto
```


### Definir archivo adjunto
```text copy=true
(que) se incluye el fichero adjunto {name} (de tipo {type}) con los siguientes datos:
    {data}
```
Indica el texto que se incluirá como fichero adjunto en datos de formulario.

#### Parámetros:
| Nombre | Wakamiti type            | Descripción           |
|--------|--------------------------|-----------------------|
| `name` | `text` *obligatorio*     | Nombre de control     |
| `type` | `text`                   | Tipo Mime del fichero |
| `data` | `document` *obligatorio* | Contenido a adjuntar  |

#### Ejemplos:
```gherkin
Dado que se incluye el fichero adjunto 'fichero' con los siguientes datos:
  """
  Contenido del fichero
  """
```
```gherkin
Dado que se incluye el fichero adjunto 'fichero' de tipo 'text/csv' con los siguientes datos:
  """
  Contenido,del,fichero
  0,56,26
  """
```


### Definir archivo adjunto (fichero)
```text copy=true
(que) se incluye el fichero adjunto {name} (de tipo {type}) con el contenido del fichero {file}
```
Indica el fichero cuyo contenido se incluirá como fichero adjunto en datos de formulario.

#### Parámetros:
| Nombre | Wakamiti type        | Descripción                         |
|--------|----------------------|-------------------------------------|
| `name` | `text` *obligatorio* | Nombre de control                   |
| `type` | `text`               | Tipo Mime del fichero               |
| `file` | `file` *obligatorio* | Fichero con el contenido a adjuntar |

#### Ejemplos:
```gherkin
Dado que se incluye el fichero adjunto 'fichero' con el contenido del fichero 'data.txt'
```
```gherkin
Dado que se incluye el fichero adjunto 'fichero' de tipo 'image/png' con el contenido del fichero 'img.png'
```


### Realizar llamada GET
```text copy=true
se realiza la búsqueda *
```
```text copy=true
se consulta(n) *
```
Envía una petición `GET` al servicio con los parámetros definidos previamente.

#### Ejemplos:
```gherkin
Dado el servicio REST '/users'
Y los siguientes parámetros de búsqueda:
  | name | value    |
  | age  | 13       |
  | city | Valencia |
Cuando se realiza la búsqueda de usuarios
```
```gherkin
Dado el servicio REST '/users'
Y un usuario identificado por '123'
Cuando se consulta el usuario
```


### Realizar llamada DELETE
```text copy=true
se elimina(n) *
```
Envía una petición `DELETE` al servicio y recurso REST definido previamente.

#### Ejemplos:
```gherkin
Dado el servicio REST '/users'
Y un usuario identificado por '123'
Cuando se elimina el usuario
```


### Realizar llamada PUT con mensaje
```text copy=true
se reemplaza(n) * con los siguientes datos:
    {data}
```
Envía una petición `PUT` al servicio y recurso REST definido previamente. El cuerpo de la petición será el contenido
indicado a continuación.

#### Parámetros:
| Nombre | Wakamiti type            | Descripción              |
|--------|--------------------------|--------------------------|
| `data` | `document` *obligatorio* | El cuerpo de la petición |

#### Ejemplos:
```gherkin
Dado el servicio REST '/users'
Y un usuario identificado por '123'
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


### Realizar llamada PUT con mensaje (fichero)
```text copy=true
se reemplaza(n) * con los datos del fichero {file}
```
Envía una petición `PUT` al servicio y recurso REST definido previamente. El cuerpo de la petición será el contenido del
fichero indicado.

#### Parámetros:
| Nombre | Wakamiti type        | Descripción                          |
|--------|----------------------|--------------------------------------|
| `file` | `file` *obligatorio* | Fichero con el cuerpo de la petición |

#### Ejemplos:
```gherkin
Dado el servicio REST '/users'
Y un usuario identificado por '123'
Cuando se reemplaza el usuario con los datos del fichero 'data/user123.json'
```


### Realizar llamada PATCH
```text copy=true
se modifica(n) * 
```
Envía una petición `PATCH` al servicio y recurso REST definido previamente.

#### Ejemplos:
```gherkin
Dado el servicio REST '/users'
Y un usuario identificado por '123'
Y los siguientes parámetros de búsqueda:
  | name | value    |
  | age  | 13       |
  | city | Valencia |
Cuando se modifica el usuario
```


### Realizar llamada PATCH con mensaje
```text copy=true
se modifica(n) * con los siguientes datos:
    {data}
```
Envía una petición `PATCH` al servicio y recurso REST definido previamente. El cuerpo de la petición será el contenido
indicado a continuación.

#### Parámetros:
| Nombre | Wakamiti type            | Descripción              |
|--------|--------------------------|--------------------------|
| `data` | `document` *obligatorio* | El cuerpo de la petición |

#### Ejemplos:
```gherkin
Dado el servicio REST '/users'
Y un usuario identificado por '123'
Cuando se modifica el usuario con los siguientes datos:
  """json
  {
    "firstName": "Jim"
  }
  """
```


### Realizar llamada PATCH con mensaje (fichero)
```text copy=true
se modifica(n) * con los datos del fichero {file}
```
Envía una petición `PATCH` al servicio y recurso REST definido previamente. El cuerpo de la petición será el contenido
del fichero indicado.

#### Parámetros:
| Nombre | Wakamiti type        | Descripción                          |
|--------|----------------------|--------------------------------------|
| `file` | `file` *obligatorio* | Fichero con el cuerpo de la petición |

#### Ejemplos:
```gherkin
Dado el servicio REST '/users'
Y un usuario identificado por '123'
Cuando se modifica el usuario con los datos del fichero 'data/user123.json'
```


### Realizar llamada POST
```text copy=true
se crea(n) *
```
```text copy=true
se envía al servicio la información
```
Envía una petición `POST` al servicio definido previamente.

#### Ejemplo:
```gherkin
Dado el servicio REST '/users'
Dados los siguiente parámetros de solicitud:
  | nombre | valor    |
  | age    | 13       |
  | city   | Valencia |
Cuando envía al servicio la información
```


### Realizar llamada POST con mensaje
```text copy=true
se crea(n) * con los siguientes datos:
    {data}
```
```text copy=true
se envía al servicio los siguientes datos:
    {data}
```
Envía una petición `POST` al servicio definido previamente. El cuerpo de la petición se rellenará con el contenido
indicado a continuación.

#### Parámetros:
| Nombre | Wakamiti type            | Descripción              |
|--------|--------------------------|--------------------------|
| `data` | `document` *obligatorio* | El cuerpo de la petición |

#### Ejemplos:
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


### Realizar llamada POST con mensaje (fichero)
```text copy=true
se crea(n) * con los datos del fichero {file}
```
```text copy=true
se envía al servicio los datos del fichero {file}
```
Envía una petición `POST` al servicio definido previamente. El cuerpo de la petición se rellenará con el contenido del
fichero indicado.

#### Parámetros:
| Nombre | Wakamiti type        | Descripción                          |
|--------|----------------------|--------------------------------------|
| `file` | `file` *obligatorio* | Fichero con el cuerpo de la petición |

#### Ejemplos:
```gherkin
Cuando se crea un usuario con los datos del fichero 'data/user123.json'
```
```gherkin
Cuando se envía al servicio los datos del fichero 'booking.json'
```


### Comprobar código HTTP de respuesta
```text copy=true
el código de respuesta HTTP {matcher}
```
Comprueba que el código HTTP de la última respuesta satisface una validación de enteros.

#### Parámetros:
| Nombre    | Wakamiti type                        | Descripción               |
|-----------|--------------------------------------|---------------------------|
| `matcher` | [integer-assertion][1] *obligatorio* | Una validación de enteros |

#### Ejemplos:
```gherkin
Entonces el código de respuesta HTTP es 201
```


### Comprobar mensaje de respuesta
```text copy=true
la respuesta es exactamente:
    {data}
```
Comprueba que el cuerpo de la respuesta sea exacto al indicado, incluyendo el orden de los campos.
```text copy=true
la respuesta es exactamente \(en cualquier orden\):
    {data}
```
Comprueba que el cuerpo de la respuesta sea exacto al indicado, pero pueden llegar los campos en diferente orden.
```text copy=true
la respuesta es parcialmente:
    {data}
```
Comprueba que el cuerpo de la respuesta incluya, al menos, los campos indicados.

#### Parámetros:
| Nombre | Wakamiti type            | Descripción           |
|--------|--------------------------|-----------------------|
| `data` | `document` *obligatorio* | El contenido esperado |

#### Ejemplos:
```gherkin
Entonces la respuesta es exactamente:
  """json
  [
    {
      "age": 46,
      "name": "Michael"
    },
    {
       "age": 23,
       "name": "John"
    }
  ]
  """
```
```gherkin
Entonces la respuesta es exactamente (en cualquier orden):
  """json
  [
    {
      "age": 23,
      "name": "John"
    },
    {
      "name": "Michael",
      "age": 46
    }
  ]
  """
```
```gherkin
Entonces la respuesta es parcialmente:
  """json
  [
    {
      "name": "John"
    }
  ]
  """
```


### Comprobar mensaje de respuesta (fichero)
```text copy=true
la respuesta es exactamente el contenido del fichero {file}
```
Comprueba que el cuerpo de la respuesta sea exacto al indicado en el fichero, incluyendo el orden de los campos.
```text copy=true
la respuesta es exactamente el contenido del fichero {file} \(en cualquier orden\)
```
Comprueba que el cuerpo de la respuesta sea exacto al indicado en el fichero, pero pueden llegar los campos en diferente orden.
```text copy=true
la respuesta es parcialmente el contenido del fichero {file}
```
Comprueba que el cuerpo de la respuesta incluya, al menos, los campos indicados en el fichero.

#### Parámetros:
| Nombre | Wakamiti type        | Descripción          |
|--------|----------------------|----------------------|
| `file` | `file` *obligatorio* | Un fichero existente |

#### Ejemplos:
```gherkin
Entonces la respuesta es parcialmente el contenido del fichero 'data/response1.json'
```


### Comprobar fragmento de la respuesta
```text copy=true
el fragmento de la respuesta {fragment} es exactamente:
    {data}
```
Comprueba que un fragmento del cuerpo de respuesta, localizado mediante una ruta dada (usando [JSONPath][jsonpath],
[XPath][xpath] o [GPath][gpath] dependiendo del tipo de contenido) sea exacto al indicado, incluyendo el orden de los
campos.
```text copy=true
el fragmento de la respuesta {fragment} es exactamente \(en cualquier orden\):
    {data}
```
Comprueba que un fragmento del cuerpo de respuesta, localizado mediante una ruta dada (usando [JSONPath][jsonpath],
[XPath][xpath] o [GPath][gpath] dependiendo del tipo de contenido) sea exacto al indicado, pero pueden llegar los campos
en diferente orden.
```text copy=true
el fragmento de la respuesta {fragment} es parcialmente:
    {data}
```
Comprueba que un fragmento del cuerpo de respuesta, localizado mediante una ruta dada (usando [JSONPath][jsonpath],
[XPath][xpath] o [GPath][gpath] dependiendo del tipo de contenido) incluya, al menos, los campos indicados.

#### Parámetros:
| Nombre     | Wakamiti type            | Descripción                      |
|------------|--------------------------|----------------------------------|
| `fragment` | `text` *obligatorio*     | Una ruta JSONPath, XPath o GPath |
| `data`     | `document` *obligatorio* | El contenido esperado            |

#### Ejemplos:
```gherkin
Entonces el fragmento de la respuesta 'users[1]' es exactamente:
  """json
  {
    "age": 23,
    "name": "John"
  }
  """
```
```gherkin
Entonces el fragmento de la respuesta 'users[1]' es exactamente \(en cualquier orden\):
  """json
  {
    "name": "John",
    "age": 23
  }
  """
```
```gherkin
Entonces el fragmento de la respuesta 'users[1]' es parcialmente:
  """json
  {
    "name": "John"
  }
  """
```


### Comprobar fragmento de la respuesta (fichero)
```text copy=true
el fragmento de la respuesta {fragment} es exactamente el contenido del fichero {file}
```
Comprueba que un fragmento del cuerpo de respuesta, localizado mediante una ruta dada (usando [JSONPath][jsonpath],
[XPath][xpath] o [GPath][gpath] dependiendo del tipo de contenido) sea exacto al indicado en el fichero, incluyendo el
orden de los campos.
```text copy=true
el fragmento de la respuesta {fragment} es exactamente el contenido del fichero {file} \(en cualquier orden\)
```
Comprueba que un fragmento del cuerpo de respuesta, localizado mediante una ruta dada (usando [JSONPath][jsonpath],
[XPath][xpath] o [GPath][gpath] dependiendo del tipo de contenido) sea exacto al indicado en el fichero, pero pueden
llegar los campos en diferente orden.
```text copy=true
el fragmento de la respuesta {fragment} es parcialmente el contenido del fichero {file}
```
Comprueba que un fragmento del cuerpo de respuesta, localizado mediante una ruta dada (usando [JSONPath][jsonpath],
[XPath][xpath] o [GPath][gpath] dependiendo del tipo de contenido) incluya, al menos, los campos indicados en el fichero.

#### Parámetros:
| Nombre     | Wakamiti type        | Descripción                      |
|------------|----------------------|----------------------------------|
| `fragment` | `text` *obligatorio* | Una ruta JSONPath, XPath o GPath |
| `file`     | `file` *obligatorio* | Un fichero existente             |

#### Ejemplos:
```gherkin
Entonces el fragmento de la respuesta 'users[1]' es exactamente el contenido del fichero 'data/response1.json'
```
```gherkin
Entonces el fragmento de la respuesta 'users[1]' es exactamente el contenido del fichero 'data/response1.json' \(en cualquier orden\)
```
```gherkin
Entonces el fragmento de la respuesta 'users[1]' es parcialmente el contenido del fichero 'data/response1.json'
```  


### Comprobar fragmento de la respuesta (valor)
```text copy=true
el (texto|entero|decimal) del fragmento de la respuesta {fragment} {matcher}
```
Comprueba el valor (*texto*, *entero* o *decimal*) de un fragmento del cuerpo de respuesta, localizado mediante una ruta
dada (usando [JSONPath][jsonpath], [XPath][xpath] o [GPath][gpath] dependiendo del tipo de contenido).

#### Parámetros:
| Nombre     | Wakamiti type                  | Descripción                      |
|------------|--------------------------------|----------------------------------|
| `fragment` | `text` *obligatorio*           | Una ruta JSONPath, XPath o GPath |
| `matcher`  | [*-assertion][1] *obligatorio* | El comparador del fragmento      |
`*`: `text`, `integer` o `decimal`, dependiendo del tipo indicado en el paso.

#### Ejemplos:
```gherkin
  Entonces el decimal del fragmento de la respuesta 'users[1].account.availableMoney` es mayor que 23.57
```
```gherkin
  Entonces el texto del fragmento de la respuesta 'users[1].name` no es 'John'
```


### Comprobar tipo de contenido de la respuesta
```text copy=true
el tipo de contenido de la respuesta es {word}
```
Valida que el tipo de contenido de la última respuesta es el esperado.
Este paso equivale a validar que el valor de la cabecera `Content-Type` de la respuesta es el tipo MIME correspondiente.

#### Parámetros:
| Nombre  | Wakamiti type        | Descripción                                        |
|---------|----------------------|----------------------------------------------------|
| `word`  | `word` *obligatorio* | `ANY`,`TEXT`,`JSON`,`XML`,`HTML`,`URLENC`,`BINARY` |

#### Ejemplos:
```gherkin
  Entonces el tipo de contenido de la respuesta es JSON
```


### Comprobar tamaño de la respuesta
```text copy=true
el tamaño de la respuesta {matcher}
```
Comprueba que la longitud en bytes de la última respuesta satisface una validación.

#### Parámetros:
| Nombre    | Wakamiti type                        | Descripción               |
|-----------|--------------------------------------|---------------------------|
| `matcher` | [integer-assertion][1] *obligatorio* | Una validación de enteros |

#### Ejemplos:
```gherkin
  Entonces el tamaño de la respuesta es menor que 500
```


### Comprobar cabecera
```text copy=true
el (texto|entero|decimal) de la cabecera de la respuesta {name} {matcher}
```
Comprueba que una determinada cabecera HTTP en la última respuesta satisface una validación de *texto*, *entero* o
*decimal*.

#### Parámetros:
| Nombre    | Wakamiti type                  | Descripción                         |
|-----------|--------------------------------|-------------------------------------|
| `name`    | `text` *obligatorio*           | Nombre de la cabecera               |
| `matcher` | [*-assertion][1] *obligatorio* | [Comparador][1] de texto o numérico |
`*`: `text`, `integer` o `decimal`, dependiendo del tipo indicado en el paso.

#### Ejemplos:
```gherkin
  Entonces el texto de la cabecera de la respuesta Content-Encoding contiene 'gzip'
```
```gherkin
  Entonces el entero de la cabecera de la respuesta Age es mayor que 10
```


### Comprobar esquema de la respuesta
```text copy=true
la respuesta cumple el siguiente esquema:
    {data}
```
Valida que la estructura del cuerpo de la respuesta REST satisface el esquema proporcionado a continuación. Los formatos
de esquema aceptados son [JSON Schema][jsonschema] para respuestas JSON y [XML Schema][xmlschema] para las respuestas
XML (en función de la cabecera de respuesta HTTP `Content-Type`).

#### Parámetros:
| nombre | Wakamiti type            | descripción              |
|--------|--------------------------|--------------------------|
| `data` | `document` *obligatorio* | JSON Schema o XML Schema |

#### Ejemplo:
```gherkin
  Entonces la respuesta cumple el siguiente esquema:
"""json
     {
       "$schema": "http://json-schema.org/draft-04/schema#",
       "type": "object",
       "properties": {
         "id":         { "type": "string", "pattern": "[a-zA-Z0-9]+" },
         "name":       { "type": "string" },
         "age":        { "type": "integer", "minimum": 5 },
         "vegetables": {
           "type": "array",
           "items": [ {
               "type": "object",
               "properties": {
                 "id":          { "type": "integer" },
                 "description": { "type": "string"  }
               },
               "required": [ "id", "description" ]
           } ]
         },
         "contact": {
           "type": "object",
           "properties": {
             "email": { "type": "string", "pattern": "^[a-zA-Z0-9]+@[a-zA-Z0-9\.]+$" }
           }
         }
       }
     }
    """
```


### Comprobar esquema de la respuesta (fichero)
```text copy=true
la respuesta cumple el esquema del fichero {file}
```
Valida que la estructura del cuerpo de la respuesta REST satisface un esquema proporcionado por fichero. Los formatos de
esquema aceptados son [JSON Schema][jsonschema] para respuestas JSON y [XML Schema][xmlschema] para las respuestas XML
(en función de la cabecera de respuesta HTTP `Content-Type`).

#### Parámetros:
| nombre | Wakamiti type        | descripción                                 |
|--------|----------------------|---------------------------------------------|
| `file` | `file` *obligatorio* | Fichero con un JSON Schema o un XML Schema  |

#### Ejemplo:
```gherkin
  Entonces la respuesta cumple el esquema del fichero 'data/user-schema.json'
```



[oauth2]: https://datatracker.ietf.org/doc/html/rfc6749 (OAuth 2.0)
[jsonschema]: https://json-schema.org/ (JSON Schema)
[jsonpath]: https://goessner.net/articles/JsonPath/
[xmlschema]: https://www.w3.org/2001/XMLSchema (XML Schema)
[xpath]: https://en.wikipedia.org/wiki/XPath (XPath)
[gpath]: https://accenture.github.io/bdd-for-all/GPATH.html (GPath)
[1]: wakamiti/architecture#comparadores
[2]: wakamiti/architecture#duration
