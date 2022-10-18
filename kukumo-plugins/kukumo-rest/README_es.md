Wakamiti :: REST plugin
====================================================================================================

Este plugin provee una serie de pasos para interactuar con una API RESTful

Configuración
----------------------------------------------------------------------------------------------------

###  `rest.baseURL`
Estalbece la URL base para las llamadas REST.
Esto es equivalente al paso `{word} como el tipo de contenido REST` si se prefiere una declaración más descriptiva.

Ejemplo:

```yaml
rest:
  baseURL: http://example.org/api/v2
```


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


### `rest.oauth2.url`

Establece el servicio de autenticación [OAuth 2.0][oauth2] que se usará para generar el token que se enviará
en la cabecera HTTP `Authorization` de las llamadas REST.

Ejemplo:

```yaml
rest:
  oauth2: 
    url: https://accounts.google.com/o/oauth2/auth
```



### `rest.oauth2.clientId`
Establece el parámetro `clientId` para el servicio de autenticación [OAuth 2.0][oauth2] definido
por el valor de la propiedad de configuración `rest.oauth2.url`.

Ejemplo:

```yaml
rest:
  oauth2: 
    clientId: WEB_APP
```


### `rest.oauth2.clientSecret`
Establece el parámetro `clientSecret` para el servicio de autenticación [OAuth 2.0][oauth2] definido
por el valor de la propiedad de configuración `rest.oauth2.url`.

Ejemplo:

```yaml
rest:
  oauth2: 
    clientSecret: ABRACADABRAus1ZMGHvq9R
```



Pasos
----------------------------------------------------------------------------------------------------

### Preparación

#### `{word} como el tipo de contenido REST`
Forma declarativa de configurar la propiedad [`rest.contentType`](#restcontenttype)

##### Parámetros:

| nombre  | tipo de dato | descripción       |
|---------|--------------|-------------------|
|         | `word`       | Tipo de contenido |

##### Ejemplo:

```gherkin
Dado XML como el tipo de contenido REST
```


---
#### `la URL base {url}`
Forma declarativa de configurar la propiedad [`rest.baseURL`](#restbaseurl)
##### Parámetros:
| nombre | tipo de dato | descripción |
|--------|--------------|-------------|
|        | `url`        | URL base    |
##### Ejemplo:
```gherkin
Dada la URL base http://example.org/api
```


---
#### `el servicio REST {text}`
Establece la ruta del servicio a probar. Se concatenará al valor de `baseURL`.

##### Parámetros:
| nombre | tipo de dato | descripción  |
|--------|--------------|--------------|
|        | `text`       | Segmento URL |
##### Ejemplo:

```gherkin
Dado el servicio REST '/users`
```


---
#### `* identificad(o|a|os|as) por {text}`
Establece un identificador de recurso REST para ser usado por el servicio.
Se concatenará al valor de `baseURL` y el servicio en concreto.
##### Parámetros:
| nombre | tipo de dato | descripción                 |
|--------|--------------|-----------------------------|
|        | `text`       | Un identificador de recurso |
##### Ejemplos:
```gherkin
Dado un usuario identificado por 'john'
```

```gherkin
Dado un libro identificado por '978-3-16-148410-0'
```


---
#### `los siguiente parámetros de solicitud:`
Establece los parámetros de la siguiente petición REST con los valores de la tabla siguiente.
Estos parámetros se enviaran como datos de formulario para peticiones POST.
##### Parámetros:
| nombre | tipo de dato | descripción                                   |
|--------|--------------|-----------------------------------------------|
|        | `data-table` | Una tabla con las columnas `nombre` y `valor` |
##### Ejemplo:
```gherkin
Dados los siguiente parámetros de solicitud:
| nombre | valor    |
| age    | 13       |
| city   | Valencia |
```


---
#### `los siguiente parámetros de búsqueda:`
Establece los parámetros de la siguiente petición REST con los valores de la tabla siguiente.
Estos parámetros se concatenerán a la URL de la petición tras la ruta
##### Parámetros:
| nombre | tipo de dato  | descripción                                   |
|--------|---------------|-----------------------------------------------|
|        | `data-table`  | Una tabla con las columnas `nombre` y `valor` |
##### Ejemplo:
```gherkin
Dados los siguiente parámetros de búsqueda:
| nombre | valor    |
| age    | 13       |
| city   | Valencia |
```


---
#### `los siguientes parámetros de ruta:`
Establece los parámetros de la siguiente petición REST con los valores de la tabla siguiente.
Estos parámetros formarán parte de la ruta de la URL, sustituyendo a los fragmentos indicados con `{` `}`.
##### Parámetros:
| nombre | tipo de dato | descripción                                    |
|--------|--------------|------------------------------------------------|
|        | `data-table` | Una tabla con las columnas `nombre` y `valor`  |
##### Ejemplo:
```gherkin
Dado el servicio 'user/{usuario}/items/{item}'
Y los siguientes parámetros de ruta:
| nombre  | valor    |
| usuario | 25       |
| item    | 7        |
```


---
#### `las siguientes cabeceras:`
Define las cabeceras HTTP que se enviarán a las llamadas posteriores, mediante una tabla.
##### Parámetros:
| nombre | tipo de dato  | descripción                                   |
|--------|---------------|-----------------------------------------------|
|        | `data-table`  | Una tabla con las columnas `nombre` y `valor` |
##### Ejemplo:
```gherkin
Given the following headers:
| nombre       | valor |
| Age          | 3600  |
| Keep-Alive   | 1200  |
```


---
#### `un timeout de {int} milisegundos`
Establece un tiempo máximo de respuesta (en milisegundos) para las siguientes peticiones HTTP
##### Parámetros:
| nombre | tipo de dato | descripción                       |
|--------|--------------|-----------------------------------|
|        | `int`        | El tiempo máximo, en milisegundos |
##### Ejemplo:
```gherkin
Dado un timeout de 12000 milisegundos
```



---
#### `un timeout de {int} segundos`
Establece un tiempo máximo de respuesta (en segundos) para las siguientes peticiones HTTP
##### Parámetros:
| nombre | tipo de dato | descripción                   |
|--------|--------------|-------------------------------|
|        | `int`        | El tiempo máximo, en segundos |
##### Ejemplo:
```gherkin
Dado un timeout de 2 segundos
```



---
#### `(que) toda petición se considera fallida si su código HTTP {integer-assertion}`
Establece una validación general para el código HTTP de todas las respuestas siguientes. Es 
similar a la propiedad de configuración [`rest.httpCodeTreshold`](#resthttpcodethreshold)
pero con una validación de enteros personalizada.
##### Parámetros:
| nombre | tipo de dato         | descripción               |
|--------|----------------------|---------------------------|
|        | `integer-assertion`  | Una validación de enteros |
##### Ejemplo:
```gherkin
* toda petición se considera fallida si su código HTTP es igual o mayor que 500
```



---
#### `(que) el servicio usa autenticación básica con las credenciales {username:text}:{password:text}`
Establece las credenciales de autenticación básica que se enviarán en la cabecera HTTP `Authorization`.
##### Parámetros:
| nombre      | tipo de dato | descripción       |
|-------------|--------------|-------------------|
| `username`  | `text`       | Nombre de usuario |
| `password`  | `text`       | Contraseña        |
##### Ejemplo:
```gherkin
Dado que el servicio usa autenticación básica con las credenciales 'us1532':'xxxxx'
```


---
#### `(que) el servicio usa el token de autenticación {text}`
Establece el token de autenticación que se enviará en la cabecera `Authorization` para las siguientes peticiones.
##### Parámetros:
| nombre | tipo de dato | descripción            |
|--------|--------------|------------------------|
|        | `text`       | Token de autenticación |
##### Ejemplo:
```gherkin
Dado que el servicio usa el token de autenticación 'hudytw9834y9cqy32t94'
```



---
#### `(que) el servicio usa el token de autenticación del fichero {file}`
Establece el token de autenticación que se enviará en la cabecera `Authorization` para las siguientes 
llamadas, obtenido desde un fichero.
##### Parámetros:
| nombre | tipo de dato | descripción                           |
|--------|--------------|---------------------------------------|
|        | `file`       | Fichero con el token de autenticación |
##### Ejemplo:
```gherkin
Dado que el servicio usa el token de autenticación del fichero 'token.txt'
```


---
#### `(que) el servicio usa el proveedor de autenticación con los siguientes datos:`
Establece el cuerpo que se enviará al servicio de autenticación
##### Parámetros:
| nombre | tipo de dato | descripción                                |
|--------|--------------|--------------------------------------------|
|        | `document`   | Cadena con los parámetros de autenticación |
##### Ejemplo:
```gherkin
Dado que el servicio usa el proveedor de autenticación con los siguientes datos:
"""
grant_type=password&username=OficinaTest4&password=xxxxx
"""
```


---
#### `(que) se incluye el fichero adjunto {file}`
Indica el fichero cuyo contenido se incluirá como fichero adjunto en datos de formulario
##### Parámetros:
| nombre | tipo de dato | descripción                         |
|--------|--------------|-------------------------------------|
|        | `file`       | Fichero con el contenido a adjuntar |
##### Ejemplo:
```gherkin
Dado que se incluye el fichero adjunto 'data.txt'
```


---
#### `(que) se incluye el fichero adjunto con los siguientes datos:`
Indica el texto que se incluirá como fichero adjunto en datos de formulario
##### Parámetros:
| nombre | tipo de dato | descripción          |
|--------|--------------|----------------------|
|        | `document`   | Contenido a adjuntar |
##### Ejemplo:
```gherkin
Dado que se incluye el fichero adjunto con los siguientes datos:
"""
Contenido del fichero
"""
```


### Acciones





#### `se realiza la búsqueda *`
Envía una petición `GET` al servicio con los parámetros definidos previamente.
##### Ejemplo:
```gherkin
Dado el servicio 'users'
Y los siguientes parámetros de búsqueda:
| nombre | value    |
| age    | 13       |
| city   | Valencia |
Cuando se realiza la búsqueda de usuarios
```


---
####  `se consulta(n) *`
Envía una petición `GET` al servicio y recurso REST definido previamente.
##### Ejemplo:
```gherkin
Dada la URL base 'http://host.com/api/v2'
Y el servicio REST 'users'
Y un usuario identificado por '123''
Cuando se consulta el usuario
```


---
#### `se elimina(n) *`
Envía una petición `DELETE` al servicio y recurso REST definido previamente.
##### Ejemplo:
```gherkin
Dada la URL base 'http://host.com/api/v2'
Y el servicio REST 'users'
Y un usuario identificado por '123'
Cuando se elimina el usuario
```


---
####  `se reemplaza(n) * con los siguientes datos:`
Envía una petición `PUT` al servicio y recurso REST definido previamente. El cuerpo de la petición
será el contenido indicado a continuación.
##### Parámetros:
| nombre | tipo de dato | descripción              |
|--------|--------------|--------------------------|
|        | `document`   | El cuerpo de la petición |
##### Ejemplo:
```gherkin
Dada la URL base 'http://host.com/api/v2'
Y el servicio REST 'users'
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


---
#### `se reemplaza(n) * con los datos del fichero {file}`
Envía una petición `PUT` al servicio y recurso REST definido previamente. El cuerpo de la petición
será el contenido del fichero indicado.
##### Parámetros:
| nombre | tipo de dato | descripción                          |
|--------|--------------|--------------------------------------|
|        | `file`       | Fichero con el cuerpo de la petición |
##### Ejemplo:
```gherkin
Dada la URL base 'http://host.com/api/v2'
Y el servicio REST 'users'
Y un usuario identificado por '123'
Cuando se reemplaza el usuario con los datos del fichero 'data/user123.json'
```


---
#### `se modifica(n) * con los siguientes datos:`
Envía una petición `PATCH` al servicio y recurso REST definido previamente. El cuerpo de la petición
será el contenido indicado a continuación.
##### Parámetros:
| nombre | tipo de dato | descripción              |
|--------|--------------|--------------------------|
|        | `document`   | El cuerpo de la petición |
##### Ejemplo:
```gherkin
Dada la URL base 'http://host.com/api/v2'
Y el servicio REST 'users'
Y un usuario identificado por '123'
Cuando se modifica el usuario con los siguientes datos:
"""json
{
    "firstName": "Jim"
}
"""
```

---
#### `se modifica(n) * con los datos del fichero {file}`
Envía una petición `PATCH` al servicio y recurso REST definido previamente. El cuerpo de la petición
será el contenido del fichero indicado.
##### Parámetros:
| nombre | tipo de dato | descripción                            |
|--------|--------------|----------------------------------------|
|        | `file`       | Fichero con el cuerpo de la petición   |
##### Ejemplo:
```gherkin
Dada la URL base 'http://host.com/api/v2'
Y el servicio REST 'users'
Y un usuario identificado por '123'
Cuando se modifica el usuario con los datos del fichero 'data/user123.json'
```


---
#### `se crea(n) * con los siguientes datos:`
Envía una petición `POST` al servicio definido previamente. El cuerpo de la petición se rellenará
con el contenido indicado a continuación.
##### Parámetros:
| nombre | tipo de dato | descripción              |
|--------|--------------|--------------------------|
|        | `document`   | El cuerpo de la petición |
##### Ejemplo:
```gherkin
Dada la URL base 'http://host.com/api/v2'
Y el servicio REST 'users'
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


---
#### `se crea(n) * con los datos del fichero {file}`
Envía una petición `POST` al servicio definido previamente. El cuerpo de la petición se rellenará
con el contenido del fichero indicado.
##### Parámetros:
| nombre | tipo de dato | descripción                          |
|--------|--------------|--------------------------------------|
|        | `file`       | Fichero con el cuerpo de la petición |
##### Ejemplo:
```gherkin
Dada la URL base 'http://host.com/api/v2'
Y el servicio REST 'users'
Cuando se crea un usuario con los datos del fichero 'data/user123.json'
```



---
#### `se crea(n) (!con los)`
Envía una petición `POST` al servicio definido previamente. El cuerpo de la petición estará vacío.
##### Ejemplo:
```gherkin
Dado el servicio REST 'bookings'
Cuando se crea
```


---
#### `se envía al servicio los siguientes datos:`
Envía una petición `POST` al servicio definido previamente. El cuerpo de la petición se rellenará
con el contenido indicado a continuación.
##### Parámetros:
| nombre | tipo de dato | descripción           |
|--------|--------------|-----------------------|
|        | `document`   | Cuerpo de la petición |
##### Ejemplo:
```gherkin
Dado el servicio REST 'bookings'
Cuando se envía al servicio los siguientes datos:
"""json
{
    "date": "2021-10-30"
}
"""
```


---
#### `se envía al servicio los datos del fichero {file}`
Envía una petición `POST` al servicio definido previamente. El cuerpo de la petición se rellenará
con el contenido del fichero indicado.
##### Parámetros:
| nombre | tipo de dato | descripción                          |
|--------|--------------|--------------------------------------|
|        | `file`       | Fichero con el cuerpo de la petición |
##### Ejemplo:
```gherkin
Dado el servicio REST 'bookings'
Cuando se envía al servicio los datos del fichero 'booking.json'
```



### Validaciones


#### `el código de respuesta HTTP {integer-assertion}`
Comprueba que el código HTTP de la última respuesta satisface una validación de enteros.
##### Parámetros:
| nombre | tipo de dato         | descripción               |
|--------|----------------------|---------------------------|
|        | `integer-assertion`  | Una validación de enteros |
##### Ejemplo:
```gherkin
Entonces el código de respuesta HTTP es 201
```


---
#### `la respuesta es exactamente:`
Valida que el cuerpo de la última respuesta es exactamente el indicado a continuación.
Dependiendo del tipo de contenido indicado, la forma específica de comparar los valores puede variar.
##### Parámetros:
| nombre | tipo de dato | descripción           |
|--------|--------------|-----------------------|
|        | `document`   | El contenido esperado |
##### Ejemplo:
```gherkin
Y la respuesta es exactamente:
"""json
{
    "name": "John",
    "age": 23,
}
"""
```


---
#### `la respuesta es exactamente el contenido del fichero {file}`
Valida que el cuerpo de la última respuesta es exacto al contenido del fichero indicado.
Dependiendo del tipo de contenido de la respuesta, la forma específica de comparar el contenido
puede variar.
##### Parámetros:
| nombre | tipo de dato | descripción          |
|--------|--------------|----------------------|
|        | `file`       | Un fichero existente |
##### Ejemplo:
```gherkin
Entonces la respuesta es exactamente el contenido del fichero 'data/response1.json'
```


---
#### `la respuesta es exactamente \(en cualquier orden\):`
Valida que el cuerpo de la última respuesta concuerda con el texto indicado a continuación,
aunque el orden de los elementos pueda variar.
Dependiendo del tipo de contenido de la respuesta, la forma específica de comparar el contenido
puede variar.
##### Parámetros:
| nombre | tipo de dato | descripción           |
|--------|--------------|-----------------------|
|        | `document`   | El contenido esperado |
##### Ejemplo:
```gherkin
Entonces la respuesta es exactamente (en cualquier orden):
"""json
{
    "age": 23,
    "name": "John"
}
"""
```


---
#### `la respuesta es exactamente el contenido del fichero {file} \(en cualquier orden\)`
Valida que el cuerpo de la última respuesta concuerda con el contenido del fichero indicado, aunque el orden de 
los elementos pueda variar.
Dependiendo del tipo de contenido de la respuesta, la forma específica de comparar el contenido
puede variar.
##### Parámetros:
| nombre | tipo de dato | descripción          |
|--------|--------------|----------------------|
|        | `file`       | Un fichero existente |
##### Ejemplo:
```gherkin
Entonces la respuesta es exactamente el contenido del fichero 'data/response1.json' (en cualquier orden)
```




---
#### `la respuesta es parcialmente:`
Valida que el cuerpo de la última respuesta incluye el texto siguiente.
Dependiendo del tipo de contenido de la respuesta, la forma específica de comparar el contenido
puede variar.
##### Parámetros:
| nombre | tipo de dato | descripción                        |
|--------|--------------|------------------------------------|
|        | `document`   | El contenido parcialmente esperado |
##### Ejemplo:
```gherkin
Entonces la respuesta es parcialmente:
"""json
{
    "age": 23
}
"""
```



---
#### `la respuesta es parcialmente el contenido del fichero {file}`
Valida que el cuerpo de la última respuesta incluye el contenido del fichero indicado.
Dependiendo del tipo de contenido de la respuesta, la forma específica de comparar el contenido
puede variar.
##### Parámetros:
| nombre | tipo de dato | descripción          |
|--------|--------------|----------------------|
|        | `file`       | Un fichero existente |
##### Ejemplo:
```gherkin
Entonces la respuesta es parcialmente el contenido del fichero 'data/response1.json'
```


---
#### `el tipo de contenido de la respuesta es {word}`
Valida que el tipo de contenido de la última respuesta es el esperado.

Este paso equivale a validar que el valor de la cabecera `Content-Type` de la respuesta 
es el tipo MIME correspondiente.

##### Parámetros:
| nombre | tipo de dato | descripción                                         |
|--------|--------------|-----------------------------------------------------|
|        | `word`       | `ANY`,`TEXT`,`JSON`,`XML`,`HTML`,`URLENC`,`BINARY`  |
##### Ejemplo:
```gherkin
Entonces el tipo de contenido de la respuesta es JSON
```


---
####  `el tamaño de la respuesta {matcher:integer-assertion}`
Comprueba que la longitud en bytes de la última respuesta satisface una validación.
##### Parámetros:
| nombre | tipo de dato         | descripción               |
|--------|----------------------|---------------------------|
|        | `integer-assertion`  | Una validación de enteros |
##### Ejemplo:
```gherkin
Entonces el tamaño de la respuesta es menor que 500
```


---
#### `el texto de la cabecera de la respuesta {name:word} {matcher:text-assertion}`
Comprueba que una determinada cabecera HTTP en la última respuesta satisface una validación de texto.
##### Parámetros:
| nombre     | tipo de dato      | descripción                   |
|------------|-------------------|-------------------------------|
| `name`     | `word`            | La clave de una cabecera HTTP |
| `matcher`  | `text-assertion`  | Una validación de texto       |
##### Ejemplo:
```gherkin
Entonces el texto de la cabecera de la respuesta Content-Encoding contiene 'gzip'
```



---
#### `el entero de la cabecera de la respuesta {name:word} {matcher:integer-assertion}`
Comprueba que el valor *entero* de una cabecera HTTP de la última respuesta satisface una validación.
##### Parámetros:
| nombre     | tipo de dato         | descripción               |
|------------|----------------------|---------------------------|
| `name`     | `word`               | Clave de la cabecera HTTP |
| `matcher`  | `integer-assertion`  | Una validación de enteros |
##### Ejemplo:
```gherkin
Entonces el entero de la cabecera de la respuesta Age es mayor que 10
```



---
#### `el decimal la cabecera de la respuesta {name:word} {matcher:decimal-assertion}`
Comprueba que el valor *decimal* de una cabecera HTTP de la última respuesta satisface una validación.
##### Parámetros:
| nombre     | tipo de dato         | descripción                 |
|------------|----------------------|-----------------------------|
| `name`     | `word`               | Clave de la cabecera HTTP   |
| `matcher`  | `decimal-assertion`  | Una validación de decimales |
##### Ejemplo:
```gherkin
Entonces el decimal la cabecera de la respuesta Custom-Header es mayor que 123.54
```



---
#### `el texto del fragmento de la respuesta {fragment:text} {matcher:text-assertion}`
Valida el valor *textual* de un fragmento del cuerpo de respuesta, localizado mediante una ruta dada
(usando [JSONPath][jsonpath] o [XPath][xpath] dependiendo del tipo de contenido).
##### Parámetros:
| nombre       | tipo de dato      | descripción                |
|--------------|-------------------|----------------------------|
| `fragment`   | `text`            | Una ruta JSONPath or XPath |
| `matcher`    | `text-assertion`  | Una validación de texto    |
##### Ejemplo:
```gherkin
Entonces el texto del fragmento de la respuesta 'users[0].lastName' empieza por 'J'
```



---
#### `el entero del fragmento de la respuesta {fragment:text} {matcher:integer-assertion}`
Valida el valor *entero* de un fragmento del cuerpo de respuesta, localizado mediante una ruta dada
(usando [JSONPath][jsonpath] o [XPath][xpath] dependiendo del tipo de contenido).
##### Parámetros:
| nombre      | tipo de dato         | descripción                |
|-------------|----------------------|----------------------------|
| `fragment`  | `text`               | Una ruta JSONPath or XPath |
| `matcher`   | `decimal-assertion`  | Una validación de enteros  |
##### Ejemplo:
```gherkin
Then the integer from response fragment 'users[0].birthDate.year` is less than 1980
```




---
#### `el decimal del fragmento de la respuesta {fragment:text} {matcher:decimal-assertion}`
Valida el valor *decimal* de un fragmento del cuerpo de respuesta, localizado mediante una ruta dada
(usando [JSONPath][jsonpath] o [XPath][xpath] dependiendo del tipo de contenido). 
##### Parámetros:
| nombre      | tipo de dato        | descripción                 |
|-------------|---------------------|-----------------------------|
| `fragment`  | `text`              | Una ruta JSONPath or XPath  |
| `matcher`   | `decimal-assertion` | Una validación de decimales |
##### Ejemplo:
```gherkin
Entonces el decimal del fragmento de respuesta 'users[1].account.availableMoney` es mayor que 23.57
```


---
#### `la respuesta cumple el siguiente esquema:`
Valida que la estructura del cuerpo de la respuesta REST satisface en esquema proporcionado a continuación.
Los formatos de esquema aceptados son [JSON Schema][jsonschema] para respuestas JSON y [XML Schema][xmlschema]
para las respuestas XML (en función de la cabecera de respuesta HTTP `Content-Type`).
##### Parámetros:
| nombre | tipo de dato | descripción              |
|--------|--------------|--------------------------|
|        | `document`   | JSON Schema o XML Schema |
##### Ejemplo:
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




---
#### `la respuesta cumple el esquema del fichero {file}`
Valida que la estructura del cuerpo de la respuesta REST satisface un esquema proporcionado por fichero.
Los formatos de esquema aceptados son [JSON Schema][jsonschema] para respuestas JSON y [XML Schema][xmlschema] 
para las respuestas XML (en función de la cabecera de respuesta HTTP `Content-Type`).
##### Parámetros:
| nombre | tipo de dato | descripción                                 |
|--------|--------------|---------------------------------------------|
|        | `file`       | Fichero con un JSON Schema o un XML Schema  |
##### Ejemplo:
```gherkin
Entonces la respuesta cumple el esquema del fichero 'data/user-schema.json'
```






[oauth2]: https://datatracker.ietf.org/doc/html/rfc6749 (OAuth 2.0)
[jsonschema]: https://json-schema.org/ (JSON Schema)
[jsonpath]: https://goessner.net/articles/JsonPath/
[xmlschema]: https://www.w3.org/2001/XMLSchema (XML Schema)
[xpath]: https://en.wikipedia.org/wiki/XPath (XPath)