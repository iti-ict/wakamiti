Kukumo :: REST plugin
====================================================================================================

This plugin provides a set of steps to interact with a RESTful API


Configuration
----------------------------------------------------------------------------------------------------

###  `rest.baseURL`
Set the base URL for subsequents API calls.
This is equivalent to the step [xx] in you prefer the descriptive configuration
Example:

```yaml
rest:
  baseURL: http://example.org/api/v2
```


### `rest.contentType`
Set the content type that would be sent in the request header of subsequent API calls.
Accepted values are:

| literal in step | `content-type` header value                                            |
|-----------------|------------------------------------------------------------------------|
| `ANY`           | `*/*`                                                                  |
| `TEXT`          | `text/plain`                                                           |
| `JSON`          | `application/json, application/javascript, text/javascript, text/json` |
| `XML`           | `application/xml, text/xml, application/xhtml+xml`                     |
| `HTML`          | `text/html`                                                            |
| `URLENC`        | `application/x-www-form-urlencoded`                                    |
| `BINARY`        | `application/octet-stream`                                             |


Default value is `JSON`.

Example:

```yaml
rest:
  contentType: XML
```


### `rest.httpCodeThreshold`

Set a global HTTP response code threshold. Every time an API call returns a HTTP code
equals or greater, the step would automatically fail regardless any other condition.

Default value is `500`.

Example:

```yaml
rest:
  httpCodeThreshold: 999
```


### `rest.oauth2.url`

Set an [OAuth 2.0][oauth2] authentication service that would be used to generate a token
in the request header `Authorization` of the API calls.

Example:

```yaml
rest:
  oauth2: 
    url: https://accounts.google.com/o/oauth2/auth
```



### `rest.oauth2.clientId`
Set the parameter `clientId` of the [OAuth 2.0][oauth2] authentication service defined
by the `rest.oauth2.url` value.

Example:

```yaml
rest:
  oauth2: 
    clientId: WEB_APP
```


### `rest.oauth2.clientSecret`
Set the parameter `clientSecret` of the [OAuth 2.0][oauth2] authentication service defined
by the `rest.oauth2.url` value.

Example:

```yaml
rest:
  oauth2: 
    clientSecret: ABRACADABRAus1ZMGHvq9R
```



Steps
----------------------------------------------------------------------------------------------------

### Setup steps

#### `the REST content type {word}`
Declarative-way for setting the configuration property [`rest.contentType`](#restcontenttype)

##### Parameters:

| name | Kukumo type | description  |
|------|-------------|--------------|
|      | `word`      | Content type |

##### Example:

```gherkin
Given the REST content type XML
```

##### Localizations:
- :es: `{type} como el tipo de contenido REST`

---

#### `the base URL {url}`
Declarative-way for setting the configuration property [`rest.baseURL`](#restbaseurl)

##### Parameters:
| name | Kukumo type | description |
|------|-------------|-------------|
|      | `url`       | Base URL    |
##### Example:

```gherkin
Given the base URL http://example.org/api
```
##### Localizations:
- :es: `la URL base {url}`


---
#### `the REST service {text}`
Set the service path that would be tested. It would be appended to the `baseURL`.

##### Parameters:
| name | Kukumo type | description      |
|------|-------------|------------------|
|      | `text`      | URL segment path |
##### Example:

```gherkin
Given the REST service '/users`
```
##### Localizations:
- :es: `el servicio REST {text}`

---
#### `* identified by {text}`
Set an entity identifier to be used by the REST service. It would be appended to 
the `baseURL` and the service.

##### Parameters:
| name | Kukumo type | description          |
|------|-------------|----------------------|
|      | `text`      | An entity identifier |
##### Examples:

```gherkin
Given a user identified by 'john'
Given the borrowed book identified by '978-3-16-148410-0'
```
##### Localizations:
- :es: `* identificad(o|a|os|as) por {text}`

---
#### `the following request parameters:`
Set the request parameters using a data table. 
##### Parameters:
| name | Kukumo type  | description                             |
|------|--------------|-----------------------------------------|
|      | `data-table` | A table with `name` and `value` columns |
##### Examples:
```gherkin
Given the following request parameters:
| name | value    |
| age  | 13       |
| city | Valencia |
```
##### Localizations:
- :es: `los siguiente parámetros de solicitud:`

---
#### `the following query parameters:`
Set the request parameters for a query
##### Parameters:
| name | Kukumo type  | description                             |
|------|--------------|-----------------------------------------|
|      | `data-table` | A table with `name` and `value` columns |
##### Examples:
```gherkin
Given the following query parameters:
| name | value    |
| age  | 13       |
| city | Valencia |
```
##### Localizations:
- :es: `los siguiente parámetros de búsqueda:`


#### `the following path parameters:`
Set parameterized path segments for a query
##### Parameters:
| name | Kukumo type  | description                             |
|------|--------------|-----------------------------------------|
|      | `data-table` | A table with `name` and `value` columns |
##### Examples:
```gherkin
Given the service 'user/{user}/items/{item}'
And the following path parameters:
| name  | value    |
| user  | 25       |
| item  | 7        |
```
##### Localizations:
- :es: `los siguientes parámetros de ruta:`



#### `the following headers:`
Define the HTTP headers that would be sent by the subsequent requests
##### Parameters:
| name | Kukumo type  | description                              |
|------|--------------|------------------------------------------|
|      | `data-table` | A table with `name` and `value` columns  |
##### Examples:
```gherkin
Given the following headers:
| name       | value |
| Age        | 3600  |
| Keep-Alive | 1200  |
```
##### Localizations:
- :es: `las siguientes cabeceras:`




#### `a timeout of {int} milliseconds`
Set a response timeout for the subsequent HTTP requests
##### Parameters:
| name | Kukumo type | description                 |
|------|-------------|-----------------------------|
|      | `int`       | The timeout in milliseconds |
##### Examples:
```gherkin
Given a timeout of 12000 milliseconds
```
##### Localizations:
- :es: `un timeout de {int} milisegundos`





#### `a timeout of {int} seconds`
Set a response timeout for the subsequent HTTP requests
##### Parameters:
| name | Kukumo type | description            |
|------|-------------|------------------------|
|      | `int`       | The timeout in seconds |
##### Examples:
```gherkin
Given a timeout of 2 seconds
```
##### Localizations:
- :es: `un timeout de {int} segundos`





#### `any request will fail when response HTTP code {integer-assertion}`
Similar to setting the configuration property [`rest.httpCodeTreshold`](#resthttpcodethreshold)
but using any integer assertion
##### Parameters:
| name | Kukumo type         | description          |
|------|---------------------|----------------------|
|      | `integer-assertion` | An integer assertion |
##### Examples:
```gherkin
* any request will fail when response HTTP code is greater than 500
```
##### Localizations:
- :es: `(que) toda petición se considera fallida si su código HTTP {integer-assertion}`





#### `the service use the basic authentication credentials {username:text}:{password:text}`
Set the authentication credentials to be sent in the `Authorization` header for the
##### Parameters:
| name       | Kukumo type | description |
|------------|-------------|-------------|
| `username` | `text`      | Username    |
| `password` | `text`      | Password    |
##### Examples:
```gherkin
Given the service use the basic authentication credentials 'us1532':'xxxxx'
```
##### Localizations:
- :es: `(que) el servicio usa autenticación básica con las credenciales {username:text}:{password:text}`





#### `the service use the authentication token {text}`
Set the authentication token to be sent in the `Authorization` header for the 
subsequent requests
##### Parameters:
| name | Kukumo type | description            |
|------|-------------|------------------------|
|      | `text`      | An authorization token |
##### Examples:
```gherkin
Given the service use the authentication token 'hudytw9834y9cqy32t94'
```
##### Localizations:
- :es: `(que) el servicio usa el token de autenticación {text}`





#### `the service use the authentication token of file {file}`
Set the authentication token to be sent in the `Authorization` header for the
subsequent requests
##### Parameters:
| name | Kukumo type | description                        |
|------|-------------|------------------------------------|
|      | `file`      | File with the authentication token |
##### Examples:
```gherkin
Given the service use the authentication token of file 'token.txt'
```
##### Localizations:
- :es: `(que) el servicio usa el token de autenticación del fichero {file}`




#### `the service use the authentication provider with the following data:`
Set the [OAuth 2.0][oauth2] authentication parameters
##### Parameters:
| name | Kukumo type | description                                 |
|------|-------------|---------------------------------------------|
|      | `document`  | A string with the authentication parameters |
##### Examples:
```gherkin
Given the service use the authentication provider with the following data:
"""
grant_type=password&username=OficinaTest4&password=xxxxx
"""
```
##### Localizations:
- :es: `(que) el servicio usa el proveedor de autenticación con los siguientes datos:`





#### `the attached file {file} is included`
Set a multipart form-data including an attachment from the given file
##### Parameters:
| name | Kukumo type | description       |
|------|-------------|-------------------|
|      | `file`      | File to be attach |
##### Examples:
```gherkin
Given the attached file 'data.txt' is included
```
##### Localizations:
- :es: `(que) se incluye el fichero adjunto {file}`





#### `the attached file is included with the following data:`
Set a multipart form-data including an attachment from the given in-document content
##### Parameters:
| name | Kukumo type | description          |
|------|-------------|----------------------|
|      | `document`  | Content to be attach |
##### Examples:
```gherkin
Given the attached file is included with the following data:
"""
File contents
"""
```
##### Localizations:
- :es: `(que) se incluye el fichero adjunto con los siguientes datos:`




### Action steps





#### `* (is|are) queried`
Send a `GET` request to the previously defined endpoint formed with the base URL,
the REST service and the query parameters
##### Examples:
```gherkin
Given the service 'users'
Given the following query parameters:
| name | value    |
| age  | 13       |
| city | Valencia |
When the users are queried
```
##### Localizations:
- :es: `se realiza la búsqueda *`





#### `* (is|are) requested`
Send a `GET` request to the previously defined endpoint formed with the base URL,
the REST service and the entity id.
##### Examples:
```gherkin
Given the base URL 'http://host.com/api/v2'
And the REST service 'users'
And a user identified by '123'
When the user is requested
```
##### Localizations:
- :es: `se consulta(n) *`





#### `* (is|are) deleted`
Send a `DELETE` request to the previously defined endpoint formed with the base URL, 
the REST service and the entity id.
##### Examples:
```gherkin
Given the base URL 'http://host.com/api/v2'
And the REST service 'users'
And a user identified by '123'
When the user is deleted
```
##### Localizations:
- :es: `se elimina(n) *`





#### `* (is|are) modified with following data:`
Send a `PUT` request to the previously defined endpoint formed with the base URL,
the REST service and the entity id. The payload is provided in-document.
##### Parameters:
| name | Kukumo type | description              |
|------|-------------|--------------------------|
|      | `document`  | The request payload body |
##### Examples:
```gherkin
Given the base URL 'http://host.com/api/v2'
And the REST service 'users'
And a user identified by '123'
When the user is modified with the following data:
"""json
{
    "firstName": "John",
    "lastName": "Doe",
    "birthDate": "1980-02-20",
    "address": "221B, Baker Street"
}
"""
```
##### Localizations:
- :es: `se reemplaza(n) * con los siguientes datos:`





#### `* (is|are) modified with the data from the file {file}`
Send a `PUT` request to the previously defined endpoint formed with the base URL,
the REST service and the entity id. The payload is provided by the given file.
##### Parameters:
| name | Kukumo type | description                                |
|------|-------------|--------------------------------------------|
|      | `file`      | A file containing the request payload body |
##### Examples:
```gherkin
Given the base URL 'http://host.com/api/v2'
And the REST service 'users'
And a user identified by '123'
When the user is modified with the data from the file 'data/user123.json'
```
##### Localizations:
- :es: `se reemplaza(n) * con los datos del fichero {file}`





#### `* (is|are) patched with the following data:`
Send a `PATCH` request to the previously defined endpoint formed with the base URL,
the REST service and the entity id. The payload is provided in-document.
##### Parameters:
| name | Kukumo type | description              |
|------|-------------|--------------------------|
|      | `document`  | The request payload body |
##### Examples:
```gherkin
Given the base URL 'http://host.com/api/v2'
And the REST service 'users'
And a user identified by '123'
When the user is patched with the following data:
"""json
{
    "firstName": "Jim"
}
"""
```
##### Localizations:
- :es: `se modifica(n) * con los siguientes datos:`





#### `* (is|are) patched with the data from the file {file}`
Send a `PATCH` request to the previously defined endpoint formed with the base URL,
the REST service and the entity id. The payload is provided by the given file.
##### Parameters:
| name | Kukumo type | description                                |
|------|-------------|--------------------------------------------|
|      | `file`      | A file containing the request payload body |
##### Examples:
```gherkin
Given the base URL 'http://host.com/api/v2'
And the REST service 'users'
And a user identified by '123'
When the user is patched with the data from the file 'data/user123.json'
```
##### Localizations:
- :es: `se modifica(n) * con los datos del fichero {file}`





#### `* (is|are) created with the following data:`
Send a `POST` request to the previously defined endpoint formed with the base URL and 
the REST service. The payload is provided in-document.
##### Parameters:
| name | Kukumo type | description              |
|------|-------------|--------------------------|
|      | `document`  | The request payload body |
##### Examples:
```gherkin
Given the base URL 'http://host.com/api/v2'
And the REST service 'users'
When a user is created with the following data:
"""json
{
    "firstName": "John",
    "lastName": "Doe",
    "birthDate": "1980-02-20",
    "address": "221B, Baker Street"
}
"""
```
##### Localizations:
- :es: `se crea(n) * con los siguientes datos:`





#### `* (is|are) created with the data from the file {file}`
Send a `POST` request to the previously defined endpoint formed with the base URL
and the REST service. The payload is provided by the given file.
##### Parameters:
| name | Kukumo type | description                                |
|------|-------------|--------------------------------------------|
|      | `file`      | A file containing the request payload body |
##### Examples:
```gherkin
Given the base URL 'http://host.com/api/v2'
And the REST service 'users'
When a user is created with the data from the file 'data/user123.json'
```
##### Localizations:
- :es: `se crea(n) * con los datos del fichero {file}`





#### `* (is|are) created`
Send a `POST` request to the previously defined endpoint formed with the base URL
and the REST service. The payload is empty.
##### Examples:
```gherkin
Given the REST service 'bookings'
When a new booking is created
```
##### Localizations:
- :es: `se crea(n) (!con los)`





#### `the following data is sent to the service:`
Send a `POST` request to the previously defined endpoint formed with the base URL
and the REST service.  The payload is provided in-document.
##### Parameters:
| name | Kukumo type | description     |
|------|-------------|-----------------|
|      | `document`  | Request payload |
##### Examples:
```gherkin
Given the REST service 'bookings'
When the following data is sent to the service
```
##### Localizations:
- :es: `se envía al servicio los siguientes datos:`





#### `the data from the file {file} is sent to the service`
Send a `POST` request to the previously defined endpoint formed with the base URL
and the REST service. The payload is provided by the given file.
##### Parameters:
| name | Kukumo type | description                                |
|------|-------------|--------------------------------------------|
|      | `file`      | A file containing the request payload body |
##### Examples:
```gherkin
Given the REST service 'bookings'
When the data from the file 'booking.json' is sent to the service
```
##### Localizations:
- :es: `se envía al servicio los datos del fichero {file}`




### Validation steps


#### `the response HTTP code {integer-assertion}`
Validate that the HTTP code of the last response satisfies the given assertion
##### Parameters:
| name | Kukumo type         | description                       |
|------|---------------------|-----------------------------------|
|      | `integer-assertion` | The integer assertion to validate |
##### Examples:
```gherkin
Then the response HTTP code is equals to 201
```
##### Localizations:
- :es: `el código de respuesta HTTP {integer-assertion}`





#### `the response is:`
Validates that the last response body is exactly the content of the in-document
content.
According to the content type response, the specific comparing operation might vary.
##### Parameters:
| name | Kukumo type | description            |
|------|-------------|------------------------|
|      | `document`  | The expected content   |
##### Examples:
```gherkin
Then the response is (in any order):
"""json
{
    "name": "John",
    "age": 23,
}
"""
```
##### Localizations:
- :es: `la respuesta es exactamente:`





#### `the response is equal to the file {file}`
Validates that the last response body is exactly the content of the given file.
According to the content type response, the specific comparing operation might vary.
##### Parameters:
| name | Kukumo type | description      |
|------|-------------|------------------|
|      | `file`      | An existing file |
##### Examples:
```gherkin
Then the response is equal to the file 'data/response1.json'
```
##### Localizations:
- :es: `la respuesta es exactamente el contenido del fichero {file}`





#### `the response is \\(in any order\\):`
Validates that the last response body has all the elements specified by the in-document
content, but in any order.
According to the content type response, the specific comparing operation might vary
##### Parameters:
| name | Kukumo type | description            |
|------|-------------|------------------------|
|      | `document`  | The expected content   |
##### Examples:
```gherkin
Then the response is (in any order):
"""json
{
    "age": 23,
    "name": "John"
}
"""
```
##### Localizations:
- :es: `la respuesta es exactamente \\(en cualquier orden\\):`





#### `the response is equal to the file {file} \\(in any order\\)`
Validates that the last response body has all the elements provided by the given file, 
but in any order.
According to the content type response, the specific comparing operation might vary.
##### Parameters:
| name | Kukumo type | description      |
|------|-------------|------------------|
|      | `file`      | An existing file |
##### Examples:
```gherkin
Then the response is equal to the file 'data/response1.json' (in any order)
```
##### Localizations:
- :es: `la respuesta es exactamente el contenido del fichero {file} \\(en cualquier orden\\)`





#### `the response contains:`
Validates that the last response body contains the given in-document content.
According to the content type response, the specific comparing operation might vary.
##### Parameters:
| name | Kukumo type | description                  |
|------|-------------|------------------------------|
|      | `document`  | The expected partial content |
##### Examples:
```gherkin
Then the response contains:
"""json
{
    "age": 23
}
"""
```
##### Localizations:
- :es: `la respuesta es parcialmente:`





#### `the response contains the file {file}`
Validates that the last response body contains the content of the given file. 
According to the content type response, the specific comparing operation might vary.
##### Parameters:
| name | Kukumo type | description      |
|------|-------------|------------------|
|      | `file`      | An existing file |
##### Examples:
```gherkin
Then the response contains the file 'data/response1.json'
```
##### Localizations:
- :es: `la respuesta es parcialmente el contenido del fichero {file}`





#### `the response content type is {word}`
Validate that the content type of the last response is the expected.
This step would be equivalent to validate the `Content-Type` response header against 
the corresponding MIME type.
##### Parameters:
| name | Kukumo type | description                                         |
|------|-------------|-----------------------------------------------------|
|      | `word`      | `ANY`,`TEXT`,`JSON`,`XML`,`HTML`,`URLENC`,`BINARY`  |
##### Examples:
```gherkin
Then the response content type is JSON
```
##### Localizations:
- :es: `el tipo de contenido de la respuesta es {word}`





#### `the response length {matcher:integer-assertion}`
Validate that the length in bytes of the last response satisfies the given assertion
##### Parameters:
| name | Kukumo type         | description                       |
|------|---------------------|-----------------------------------|
|      | `integer-assertion` | The integer assertion to validate |
##### Examples:
```gherkin
Then the response length is less than 500
```
##### Localizations:
- :es: `el tamaño de la respuesta {matcher:integer-assertion}`





#### `the text response header {name:word} {matcher:text-assertion}`
Validate that a header value from the last REST response satisfies the given assertion
##### Parameters:
| name      | Kukumo type      | description         |
|-----------|------------------|---------------------|
| `name`    | `word`           | The HTTP header key |
| `matcher` | `text-assertion` | A text assertion    |
##### Examples:
```gherkin
Then the text response header Content-Encoding contains 'gzip'
```
##### Localizations:
- :es: `el texto de la cabecera de la respuesta {name:word} {matcher:text-assertion}`





#### `the integer response header {name:word} {matcher:integer-assertion}`
Validate that a header value from the last REST response satisfies the given assertion
##### Parameters:
| name      | Kukumo type         | description          |
|-----------|---------------------|----------------------|
| `name`    | `word`              | The HTTP header key  |
| `matcher` | `integer-assertion` | An integer assertion |
##### Examples:
```gherkin
Then the integer response header Age is greater than 10
```
##### Localizations:
- :es: `el entero de la cabecera de la respuesta {name:word} {matcher:integer-assertion}`





#### `the decimal response header {name:word} {matcher:decimal-assertion}`
Validate that a header value from the last REST response satisfies the given assertion
##### Parameters:
| name      | Kukumo type         | description         |
|-----------|---------------------|---------------------|
| `name`    | `word`              | The HTTP header key |
| `matcher` | `decimal-assertion` | A decimal assertion |
##### Examples:
```gherkin
Then the decimal response header Custom-Header is greater than 123.54
```
##### Localizations:
- :es: `el decimal la cabecera de la respuesta {name:word} {matcher:decimal-assertion}`





#### `the text from response fragment {fragment:text} {matcher:text-assertion}`
Validates the value from a [JSONPath][jsonpath] or [XPath][xpath] response fragment according
a text assertion
##### Parameters:
| name        | Kukumo type      | description               |
|-------------|------------------|---------------------------|
| `fragment`  | `text`           | A JSONPath or XPath query |
| `matcher`   | `text-assertion` | A text assertion          |
##### Examples:
```gherkin
Then the text from response fragment 'users[0].lastName' starts with 'J'
```
##### Localizations:
- :es: `el texto del fragmento de la respuesta {fragment:text} {matcher:text-assertion}`





#### `the integer from response fragment {fragment:text} {matcher:integer-assertion}`
Validates the value from a [JSONPath][jsonpath] or [XPath][xpath] response fragment according
an integer assertion
##### Parameters:
| name        | Kukumo type         | description               |
|-------------|---------------------|---------------------------|
| `fragment`  | `text`              | A JSONPath or XPath query |
| `matcher`   | `decimal-assertion` | An integer assertion      |
##### Examples:
```gherkin
Then the integer from response fragment 'users[0].birthDate.year` is less than 1980
```
##### Localizations:
- :es: `el entero del fragmento de la respuesta {fragment:text} {matcher:integer-assertion}`





#### `the decimal from response fragment {fragment:text} {matcher:decimal-assertion}`
Validates the value from a [JSONPath][jsonpath] or [XPath][xpath] response fragment according 
a decimal assertion
##### Parameters:
| name        | Kukumo type         | description               |
|-------------|---------------------|---------------------------|
| `fragment`  | `text`              | A JSONPath or XPath query |
| `matcher`   | `decimal-assertion` | A decimal assertion       |
##### Examples:
```gherkin
Then the decimal from response fragment 'users[1].account.availableMoney` is greater than 23.57
```
##### Localizations:
- :es: `el decimal del fragmento de la respuesta {fragment:text} {matcher:decimal-assertion}`





#### `the response satisfies the following schema:`
Asserts that the response body structure satisfies a given schema. The accepted 
schema formats are [JSON Schema](https://json-schema.org/) for JSON responses 
and [XML Schema](https://www.w3.org/2001/XMLSchema) for XML responses (according the 
`Content-Type` response header).
##### Parameters:
| name | Kukumo type | description               |
|------|-------------|---------------------------|
|      | `document`  | JSON Schema or XML Schema |
##### Examples:
```gherkin
Then the response satisfies the following schema:
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
         "email": { "type": "string", "pattern": "^[a-zA-Z0-9]+@[a-zA-Z0-9\\.]+$" }
       }
     }
   }
 }
"""
```

##### Localizations:
- :es: `la respuesta cumple el siguiente esquema:`





#### `the response satisfies the schema from the file {file}`
Asserts that the response body structure satisfies a schema from a given file. The accepted
schema formats are [JSON Schema][jsonschema] for JSON responses
and [XML Schema][xmlschema] for XML responses (according the
`Content-Type` response header).
##### Parameters:
| name | Kukumo type | description                    |
|------|-------------|--------------------------------|
|      | `file`      | JSON Schema or XML Schema file |
##### Examples:
```gherkin
Then the response satisfies the schema from the file 'data/user-schema.json'
```
##### Localizations:
- :es: `la respuesta cumple el esquema del fichero {file}`






[oauth2]: https://datatracker.ietf.org/doc/html/rfc6749 (OAuth 2.0)
[jsonschema]: https://json-schema.org/ (JSON Schema)
[jsonpath]: https://goessner.net/articles/JsonPath/
[xmlschema]: https://www.w3.org/2001/XMLSchema (XML Schema)
[xpath]: https://en.wikipedia.org/wiki/XPath (XPath)