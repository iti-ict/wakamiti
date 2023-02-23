
This plugin provides a set of steps to interact with a RESTful API.

---

**Configuration**:
- [`rest.baseURL`](#restbaseurl)
- [`rest.contentType`](#restcontenttype)
- [`rest.httpCodeThreshold`](#resthttpcodethreshold)
- [`rest.timeout`](#resttimeout)
- [`rest.oauth2.url`](#restoauth2url)
- [`rest.oauth2.clientId`](#restoauth2clientid)
- [`rest.oauth2.clientSecret`](#restoauth2clientsecret)
- [`rest.oauth2.cached`](#restoauth2cached)
- [`rest.oauth2.parameters`](#restoauth2parameters)
- [`rest.config.multipart.subtype`](#restconfigmultipartsubtype)
- [`rest.config.redirect.follow`](#restconfigredirectfollow)
- [`rest.config.redirect.allowCircular`](#restconfigredirectallowcircular)
- [`rest.config.redirect.rejectRelative`](#restconfigredirectrejectrelative)
- [`rest.config.redirect.max`](#restconfigredirectmax)


**Steps**:
- [Define content type](#define-content-type)
- [Define base URL](#define-base-url)
- [Define service](#define-service)
- [Define id](#define-id)
- [Define parameters or headers](#define-parameters-or-headers)
- [Define parameters or headers (table)](#define-parameters-or-headers-table)
- [Define timeout](#define-timeout)
- [Define HTTP code threshold](#define-http-code-threshold)
- [Define basic authentication](#define-basic-authentication)
- [Define oauth2 authentication](#define-oauth2-authentication)
- [Define oauth2 authentication by token](#define-oauth2-authentication)
- [Define oauth2 authentication by token (file)](#define-oauth2-authentication-file)
- [Define oauth2 authentication by credentials](#define-oauth2-authentication-by-credentials)
- [Define oauth2 authentication by client](#define-oauth2-authentication-by-client)
- [Clear authentication](#clear-authentication)
- [Define multipart subtype](#define-multipart-subtype)
- [Define attached file](#define-attached-file)
- [Define attached file (file)](#define-attached-file-file)
- [Execute GET request](#execute-get-request)
- [Execute DELETE request](#execute-delete-request)
- [Execute PUT request with body](#execute-put-request-with-body)
- [Execute PUT request with body (file)](#execute-put-request-with-body-file)
- [Execute PATCH request](#execute-patch-request)
- [Execute PATCH request with body](#execute-patch-request-with-body)
- [Execute PATCH request with body (file)](#execute-patch-request-with-body-file)
- [Execute POST request](#execute-post-request)
- [Execute POST request with body](#execute-post-request-with-body)
- [Execute POST request with body (file)](#execute-post-request-with-body-file)
- [Check response HTTP code](#check-response-http-code)
- [Check response body](#check-response-body)
- [Check response body (file)](#check-response-body-file)
- [Check response body fragment](#check-response-body-fragment)
- [Check response content type](#check-response-content-type)
- [Check response size](#check-response-size)
- [Check response header](#check-response-header)
- [Check response schema](#check-response-schema)
- [Check response schema (file)](#check-response-schema-file)




## Configuration

---
###  `rest.baseURL`
Set the base URL for subsequents API calls. This is equivalent to the step "[Define base URL](#define-base-url)" in you 
prefer the descriptive configuration.

Example:

```yaml
rest:
  baseURL: https://example.org/api/v2
```

---
### `rest.contentType`
Set the content type that would be sent in the request header of subsequent API calls.
Accepted values are:

| literal    | `content-type` header value                                            |
|------------|------------------------------------------------------------------------|
| `ANY`      | `*/*`                                                                  |
| `TEXT`     | `text/plain`                                                           |
| `JSON`     | `application/json, application/javascript, text/javascript, text/json` |
| `XML`      | `application/xml, text/xml, application/xhtml+xml`                     |
| `HTML`     | `text/html`                                                            |
| `URLENC`   | `application/x-www-form-urlencoded`                                    |
| `BINARY`   | `application/octet-stream`                                             |


Default value is `JSON`.

Example:
```yaml
rest:
  contentType: XML
```

---
### `rest.httpCodeThreshold`
Sets a global HTTP response code threshold. Every time an API call returns an HTTP code equals or greater, the step 
would automatically fail regardless any other condition.

Default value is `500`.

Example:
```yaml
rest:
  httpCodeThreshold: 999
```

---
### `rest.timeout`

Sets a response timeout (in milliseconds) for the subsequent HTTP requests. In case of exceeding the specified time, the 
request will be stopped and an error will occur.

Default value is `60000`.

Example:
```yaml
rest:
  timeout: 10000
```

---
### `rest.oauth2.url`
Set an [OAuth 2.0][oauth2] authentication service that would be used to generate a token in the request header 
`Authorization` of the API calls.

Example:
```yaml
rest:
  oauth2: 
    url: https://accounts.google.com/o/oauth2/auth
```


---
### `rest.oauth2.clientId`
Sets the parameter `clientId` of the [OAuth 2.0][oauth2] authentication service defined by the `rest.oauth2.url` value.

Example:
```yaml
rest:
  oauth2: 
    clientId: WEB_APP
```

---
### `rest.oauth2.clientSecret`
Sets the parameter `clientSecret` of the [OAuth 2.0][oauth2] authentication service defined by the `rest.oauth2.url` 
value.

Example:
```yaml
rest:
  oauth2: 
    clientSecret: ABRACADABRAus1ZMGHvq9R
```

---
### `rest.oauth2.cached`
Sets whether the retrieved token is cached to avoid recurring calls to the oauth service if the data is the same.

Default value is `false`.

Example:
```yaml
rest:
  oauth2:
    cached: true
```

---
### `rest.oauth2.parameters`
Sets the default parameters for OAuth authentication.

Example:
```yaml
rest:
  oauth2:
    parameters:
      grant_type: password
      username: pepe
      password: 1234asdf
      scope: something
```

---
### `rest.config.multipart.subtype`
Sets the subtype of multipart HTTP requests. The available values are:

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

Default value is `form-data`.

Example:
```yaml
rest:
  config:
    multipart:
      subtype: mixed
```

---
### `rest.config.redirect.follow`
Sets whether to allow following redirects in HTTP requests.

Default value is `true`.

Example:
```yaml
rest:
  config:
    redirect:
      follow: false
```

---
### `rest.config.redirect.allowCircular`
Sets whether circular redirects are allowed in HTTP requests.

Default value is `false`.

Example:
```yaml
rest:
  config:
    redirect:
      allowCircular: true
```

---
### `rest.config.redirect.rejectRelative`
Sets whether to reject relative redirects in HTTP requests.

Default value is `false`.

Example:
```yaml
rest:
  config:
    redirect:
      rejectRelative: true
```

---
### `rest.config.redirect.max`
Sets the maximum number of redirects in HTTP requests.

Default value is `100`.

Example:
```yaml
rest:
  config:
    redirect:
      max: 150
```


## Steps

---
### Define content type
```
the REST content type {word}
```
Declarative-way for setting the configuration property [`rest.contentType`](#restcontenttype).

#### Parameters:
| Name   | Wakamiti type | Description    |
|--------|---------------|----------------|
| `type` | `word`        | Connection URL |

#### Examples:
```gherkin
  Given the REST content type XML
```

---
### Define base URL
```
the base URL {url}
```
Declarative-way for setting the configuration property [`rest.baseURL`](#restbaseurl). 

#### Parameters:
| Name  | Wakamiti type | Description |
|-------|---------------|-------------|
| `url` | `url`         | Base URL    |

#### Examples:
```gherkin
  Given the base URL https://example.org/api
```

---
### Define service
```
the REST service {text}
```
Sets the service path that would be tested. It would be appended to the `baseURL`.

#### Parameters:
| Name      | Wakamiti type | Description      |
|-----------|---------------|------------------|
| `service` | `text`        | URL segment path |

#### Examples:
```gherkin
  Given the REST service '/users`
```

---
### Define id
```
* identified by {text}
```
Sets an entity identifier to be used by the REST service. It would be appended to the `baseURL` and the service.

#### Parameters:
| Name   | Wakamiti type | Description          |
|--------|---------------|----------------------|
| `text` | `text`        | An entity identifier |

#### Examples:
```gherkin
  Given a user identified by 'john'
```
```gherkin
  Given the borrowed book identified by '978-3-16-148410-0'
```

---
### Define parameters or headers
```
the (request|query|path) parameter {name} with value {value}
```
```
the header {name} with value {value}
```
Sets a header or request, query or path parameter. The request parameters will be sent as form data for POST 
requests, the query parameters will be pass after the service URL (like `/user?param1=abc&param2=123`), and path 
parameters will be part of the request URL replacing the segments marked with `{}`.

##### Parámetros:
| Nombre  | Wakamiti type | Descripción               |
|---------|---------------|---------------------------|
| `name`  | `text`        | Header or parameter name  |
| `value` | `text`        | Header or parameter value |

##### Ejemplos:
```gherkin
  Given the request parameter 'age' with value '13'
  When the data info is sent to the service
```
```gherkin
  Given the query parameter 'city' with value 'Valencia'
  When the user is queried
```
```gherkin
  Given the REST service 'user/{usuario}/items'
  And the path parameter 'usuario' with value '25'
```
```gherkin
  Given the header 'Keep-alive' with value '1200'
```

---
### Define parameters or headers (table)
```
the following (request|query|path) parameters:
```
```
the following headers:
```
Sets headers or request, query or path parameters. The request parameters will be sent as form data for POST
requests, the query parameters will be pass after the service URL (like `/user?param1=abc&param2=123`), and path
parameters will be part of the request URL replacing the segments marked with `{}`.

##### Parameters:
| Name | Wakamiti type | Description                             |
|------|---------------|-----------------------------------------|
|      | `table`       | A table with `name` and `value` columns |

##### Examples:
```gherkin
  Given the following request parameters:
    | Name | Value    |
    | age  | 13       |
    | city | Valencia |
  When the data info is sent to the service
```
```gherkin
  Given the following query parameters:
    | Name | Value    |
    | age  | 13       |
    | city | Valencia |
  When the user is queried
```
```gherkin
  Given the service 'user/{user}/items/{item}'
  And the following path parameters:
    | Name | Value |
    | user | 25    |
    | item | 7     |
```
```gherkin
  Given the following headers:
    | Name       | Value |
    | Age        | 3600  |
    | Keep-Alive | 1200  |
```

---
### Define timeout
```
a timeout of {int} (milli)seconds
```
Sets a response timeout (in second or milliseconds) for the subsequent HTTP requests.

##### Parameters:
| Name   | Wakamiti type | Description                 |
|--------|---------------|-----------------------------|
| `int`  | `int`         | The timeout in milliseconds |

##### Examples:
```gherkin
  Given a timeout of 12000 milliseconds
```
```gherkin
  Given a timeout of 2 seconds
```

---
### Define HTTP code threshold
```
any request will fail when response HTTP code {matcher}
```
Similar to setting the configuration property [`rest.httpCodeTreshold`](#resthttpcodethreshold) but using any integer 
assertion.

##### Parameters:
| Name      | Wakamiti type       | Description             |
|-----------|---------------------|-------------------------|
| `matcher` | `integer-assertion` | Numeric [comparator][1] |

##### Example:
```gherkin
  * any request will fail when response HTTP code is greater than 500
```

---
### Define basic authentication
```
the service use the basic authentication credentials {username}:{password}
```
Sets the basic authentication credentials to be sent in the `Authorization` header for the subsequent requests.

##### Parameters:
| Name       | Wakamiti type | Description  |
|------------|---------------|--------------|
| `username` | `text`        | The username |
| `password` | `text`        | The password |

##### Examples:
```gherkin
  Given the service use the basic authentication credentials 'us1532':'xxxxx'
```

---
### Define oauth2 authentication
```
the service use the oauth authentication
```
Sets the bearer authentication token to be sent in the `Authorization` header, which is previously retrieved from the
configured oauth2 service ([url](#restoauth2url), [clientId](#restoauth2clientid), 
[clientSecret](#restoauth2clientsecret), [parameters](#restoauth2parameters)), for the following requests.


##### Ejemplos:
```gherkin
  Given the service use the oauth authentication
```

---
### Define oauth2 authentication by token
```
the service use the oauth authentication token {token}
```
Sets the bearer authentication token to be sent in the `Authorization` header for subsequent requests.

##### Parameters:
| Name    | Wakamiti type | Description          |
|---------|---------------|----------------------|
| `token` | `text`        | Authentication token |

##### Ejemplos:
```gherkin
  Given the service use the oauth authentication token 'hudytw9834y9cqy32t94'
```

---
### Define oauth2 authentication by token (file)
```
the service use the oauth authentication token from the file {file}
```
Sets the bearer authentication token to be sent in the `Authorization` header for subsequent requests, from file.

##### Parameters:
| Name   | Wakamiti type | Description                        |
|--------|---------------|------------------------------------|
| `file` | `file`        | File with the authentication token |

##### Ejemplos:
```gherkin
  Given the service use the oauth authentication token from the file 'token.txt'
```

---
### Define oauth2 authentication by credentials
```
the service use the oauth authentication credentials {username}:{password}
```
```
the service use the oauth authentication credentials {username}:{password} with the following parameters:
```
Sets the bearer authentication token to be sent in the `Authorization` header, which is previously retrieved from the 
configured oauth2 service ([url](#restoauth2url), [clientId](#restoauth2clientid), [clientSecret](#restoauth2clientsecret)), 
using the indicated credentials, for the following requests.

Additional parameters supported by `Oauth` can also be added using a table.

##### Parameters:
| Name       | Wakamiti type | Description                             |
|------------|---------------|-----------------------------------------|
| `username` | `text`        | The username                            |
| `password` | `text`        | The password                            |
|            | `table`       | A table with `name` and `value` columns |

##### Examples:
```gherkin
  Given the service use the oauth authentication credentials 'us1532':'xxxxx'
```

```gherkin
  Given the service use the oauth authentication credentials 'us1532':'xxxxx' with the following parameters:
    | name  | value     |
    | scope | something |
```

---
### Define oauth2 authentication by client
```
the service use the oauth authentication
```
```
the service use the oauth authentication with the following parameters:
```
Sets the bearer authentication token to be sent in the `Authorization` header, which is previously retrieved from the
configured oauth2 service ([url](#restoauth2url), [clientId](#restoauth2clientid), [clientSecret](#restoauth2clientsecret)),
using client data, for the following requests.

Additional parameters supported by `Oauth` can also be added using a table.

##### Parameters:
| Name       | Wakamiti type | Description                             |
|------------|---------------|-----------------------------------------|
|            | `table`       | A table with `name` and `value` columns |

##### Examples:
```gherkin
  Given the service use the oauth authentication
```

```gherkin
  Given the service use the oauth authentication with the following parameters:
    | name  | value     |
    | scope | something |
```

---
### Clear authentication
```
the service does not use authentication
```
Deletes the authentication header.

##### Examples:
```gherkin
  Given the service does not use authentication
```

---
### Define multipart subtype
```
{type} as subtype multipart
```
Sets the default subtype for multipart requests. This step is equivalent to setting the 
[`rest.config.multipart.subtype`](#restconfigmultipartsubtype) property.

##### Parameters:
| Name   | Wakamiti type | Description       |
|--------|---------------|-------------------|
| `type` | `text`        | Multipart subtype |

##### Examples:
```gherkin
  Given 'mixed' as subtype multipart
```

---
### Define attached file
```
the attached file is included with the following data:
```
Sets a multipart form-data including an attachment from the given in-document content.

##### Parameters:
| Name | Wakamiti type | Description          |
|------|---------------|----------------------|
|      | `document`    | Content to be attach |

##### Examples:
```gherkin
  Given the attached file is included with the following data:
    """
    File content
    """
```

---
### Define attached file (file)
```
the attached file {file} is included
```
Sets a multipart form-data including an attachment from the given in-document content.

##### Parameters:
| Name   | Wakamiti type | Description       |
|--------|---------------|-------------------|
| `file` | `file`        | File to be attach |

##### Examples:
```gherkin
  Given the attached file 'data/data.txt' is included
```

---
### Execute GET request
```
* (is|are) (queried|requested)
```
Sends a `GET` request to the previously defined parameters.

##### Examples:
```gherkin
  Given the service 'users'
  And the following query parameters:
    | Name | Value |
    | user | 25    |
    | item | 7     |
  When the users are queried
```
```gherkin
  Given the REST service 'users'
  And a user identified by '123'
  When the user is requested
```

---
### Execute DELETE request
```
* (is|are) deleted
```
Sends a `DELETE` request to the previously defined endpoint formed with the base URL, the REST service and the entity id.

##### Examples:
```gherkin
  Given the REST service 'users'
  And a user identified by '123'
  When the user is deleted
```

---
### Execute PUT request with body
```
* (is|are) modified with following data:
```
Sends a `PUT` request to the previously defined endpoint formed with the base URL, the REST service and the entity id. 
The payload is provided in-document.

##### Parameters:
| Name | Wakamiti type | Description              |
|------|---------------|--------------------------|
|      | `document`    | The request payload body |

##### Examples:
```gherkin
  Given the REST service 'users'
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

---
### Execute PUT request with body (file)
```
* (is|are) modified with the data from the file {file}
```
Sends a `PUT` request to the previously defined endpoint formed with the base URL, the REST service and the entity id. 
The payload is provided by the given file.

##### Parameters:
| Name   | Wakamiti type | Description                                |
|--------|---------------|--------------------------------------------|
| `file` | `file`        | A file containing the request payload body |

##### Examples:
```gherkin
  Given the REST service 'users'
  And a user identified by '123'
  When the user is modified with the data from the file 'data/user123.json'
```

---
### Execute PATCH request
```
* (is|are) patched
```
Sends a `PATCH` request to the previously defined endpoint formed with the base URL, the REST service and the entity id.

##### Examples:
```gherkin
  Given the REST service 'users'
  And a user identified by '123'
  And the following query parameters:
    | Name | Value    |
    | age  | 13       |
    | city | Valencia |
  When the user is patched
```

---
### Execute PATCH request with body
```
* (is|are) patched with the following data:
```
Sends a `PATCH` request to the previously defined endpoint formed with the base URL, the REST service and the entity id. 
The payload is provided in-document.

##### Parameters:
| Name | Wakamiti type | Description              |
|------|---------------|--------------------------|
|      | `document`    | The request payload body |

##### Examples:
```gherkin
  Given the REST service 'users'
  And a user identified by '123'
  When the user is patched with the following data:
    """json
    {
        "firstName": "Jim"
    }
    """
```

---
### Execute PATCH request with body (file)
```
* (is|are) patched with the data from the file {file}
```
Sends a `PATCH` request to the previously defined endpoint formed with the base URL, the REST service and the entity id. 
The payload is provided by the given file.

##### Parameters:
| Name   | Wakamiti type | Description                                |
|--------|---------------|--------------------------------------------|
| `file` | `file`        | A file containing the request payload body |

##### Examples:
```gherkin
  Given the REST service 'users'
  And a user identified by '123'
  When the user is patched with the data from the file 'data/user123.json'
```

---
### Execute POST request
```
* (is|are) created
```
```
the data info is sent to the service
```
Sends a `POST` request to the previously defined endpoint formed with the base URL and the REST service. The payload is 
empty.

##### Example:
```gherkin
  Given the REST service 'users'
  And the following request parameters:
    | nombre | valor    |
    | age    | 13       |
    | city   | Valencia |
  When the data info is sent to the service
```

---
### Execute POST request with body
```
* (is|are) created with the following data:
```
```
the following data is sent to the service:
```
Send a `POST` request to the previously defined endpoint formed with the base URL and the REST service. The payload is 
provided in-document.

##### Parameters:
| Name | Wakamiti type | Description     |
|------|---------------|-----------------|
|      | `document`    | Request payload |

##### Examples:
```gherkin
  When the user is created with the following data:
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
  When the following data is sent to the service:
    """json
    {
        "date": "2021-10-30"
    }
    """
```

---
### Execute POST request with body (file)
```
* (is|are) created with the data from the file {file}
```
```
the data from the file {file} is sent to the service
```
Sends a `POST` request to the previously defined endpoint formed with the base URL and the REST service. The payload is 
provided by the given file.

##### Parameters:
| Name   | Wakamiti type | Description                                |
|--------|---------------|--------------------------------------------|
| `file` | `file`        | A file containing the request payload body |

##### Examples:
```gherkin
  When the user is created with the data from the file 'data/user123.json'
```
```gherkin
  When the data from the file 'data/booking.json' is sent to the service
```

---
### Check response HTTP code
```
the response HTTP code {matcher}
```
Validate that the HTTP code of the last response satisfies the given assertion.

##### Parameters:
| Name      | Wakamiti type        | Description            |
|-----------|----------------------|------------------------|
| `matcher` | `integer-assertion`  | Number [comparator][1] |

##### Examples:
```gherkin
  Then the response HTTP code is equals to 201
```


---
### Check response body
```
the response is:
```
Validates that the last response body is exactly the content of the in-document content.
```
the response is \(in any order\):
```
Validates that the last response body has all the elements specified by the in-document content, but in any order.
```
the response contains:
```
Validates that the last response body contains the given in-document content.

##### Parameters:
| Name | Wakamiti type | Description                  |
|------|---------------|------------------------------|
|      | `document`    | The expected partial content |

##### Examples:
```gherkin
  Then the response is:
    """json
    {
        "age": 23,
        "name": "John"
    }
    """
```

---
### Check response body (file)
```
the response is equal to the file {file}
```
Validates that the last response body is exactly the content of the given file.
```
the response is equal to the file {file} \(in any order\)
```
Validates that the last response body has all the elements provided by the given file, but in any order.
```
the response contains the file {file}
```
Validates that the last response body contains the content of the given file.

##### Parameters:
| Name   | Wakamiti type | Description      |
|--------|---------------|------------------|
| `file` | `file`        | An existing file |

##### Examples:
```gherkin
  Then the response contains the file 'data/response1.json'
```

---
### Check response body fragment
```
the (text|integer|decimal) from response fragment {fragment} {matcher}
```
Validates the value from a [JSONPath][jsonpath] or [XPath][xpath] response fragment according a 
*text*, *integer* or *decimal* assertion.

##### Parameters:
| Name       | Wakamiti type  | Description               |
|------------|----------------|---------------------------|
| `fragment` | `text`         | A JSONPath or XPath query |
| `matcher`  | `*-assertion`  | [Comparator][1]           |
`*`: `text`, `integer` o `decimal`.

##### Examples:
```gherkin
  Then the decimal from response fragment 'users[1].account.availableMoney' is greater than 23.57
```
```gherkin
  Then the decimal from response fragment 'users[0].lastName' starts with 'J'
```


---
### Check response content type
```
the response content type is {word}
```
Validate that the content type of the last response is the expected. This step would be equivalent to validate the 
`Content-Type` response header against the corresponding MIME type.

##### Parameters:
| Name   | Wakamiti type | Description                                        |
|--------|---------------|----------------------------------------------------|
| `word` | `word`        | `ANY`,`TEXT`,`JSON`,`XML`,`HTML`,`URLENC`,`BINARY` |

##### Examples:
```gherkin
  Then the response content type is JSON
```

---
### Check response size
```
the response length {matcher}
```
Validate that the length in bytes of the last response satisfies the given assertion.

##### Parameters:
| Name      | Wakamiti type       | Description            |
|-----------|---------------------|------------------------|
| `matcher` | `integer-assertion` | Number [comparator][1] |

##### Examples:
```gherkin
  Then the response length is less than 500
```

---
### Check response header
```
the (texto|entero|decimal) response header {name} {matcher}
```
Validate that a header value from the last REST response satisfies the *text*, *integer* or *decimal* assertion.

##### Parameters:
| Name      | Wakamiti type | Description          |
|-----------|---------------|----------------------|
| `name`    | `text`        | The HTTP header name |
| `matcher` | `*-assertion` | [Comparator][1]      |
`*`: `text`, `integer` o `decimal`.

##### Examples:
```gherkin
  Then the text response header Content-Encoding contains 'gzip'
```
```gherkin
  Then the integer response header Age is greater than 10
```

---
### Check response schema
```
the response satisfies the following schema:
```
Asserts that the response body structure satisfies a given schema. The accepted schema formats are 
[JSON Schema](https://json-schema.org/) for JSON responses and [XML Schema](https://www.w3.org/2001/XMLSchema) for XML 
responses (according the`Content-Type` response header).

#### Parameters:
| name | Wakamiti type | description               |
|------|---------------|---------------------------|
|      | `document`    | JSON Schema or XML Schema |

#### Examples:
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
             "email": { "type": "string", "pattern": "^[a-zA-Z0-9]+@[a-zA-Z0-9\.]+$" }
           }
         }
       }
     }
    """
```

---
### Check response schema (file)
```
the response satisfies the schema from the file {file}
```
Asserts that the response body structure satisfies a schema from a given file. The accepted schema formats are 
[JSON Schema][jsonschema] for JSON responses and [XML Schema][xmlschema] for XML responses (according the `Content-Type` 
response header).

##### Parameters:
| name | Wakamiti type | description                    |
|------|---------------|--------------------------------|
|      | `file`        | JSON Schema or XML Schema file |

##### Examples:
```gherkin
  Then the response satisfies the schema from the file 'data/user-schema.json'
```




[oauth2]: https://datatracker.ietf.org/doc/html/rfc6749 (OAuth 2.0)
[jsonschema]: https://json-schema.org/ (JSON Schema)
[jsonpath]: https://goessner.net/articles/JsonPath/
[xmlschema]: https://www.w3.org/2001/XMLSchema (XML Schema)
[xpath]: https://en.wikipedia.org/wiki/XPath (XPath)
[1]: kukumo/architecture#comparators