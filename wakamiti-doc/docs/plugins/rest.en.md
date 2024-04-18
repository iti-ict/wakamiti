---
title: REST steps
date: 2022-09-20
slug: /en/plugins/rest
---


This plugin provides a set of steps to interact with a RESTful API.


---
## Tabla de contenido

---


## Install


Include the module in the corresponding section.

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


## Options

### `rest.baseURL`
- Type: `URL`

Defines the base URL for REST calls. This setting is equivalent to the [Define base URL](#define-base-url) step if a 
more descriptive statement is preferred.

Example:
```yaml
rest:
  baseURL: https://example.org/api/v2
```


### `rest.contentType`
- Type: `string`
- Default: `JSON`

Specifies the type of content to send in the header of REST calls. Accepted values are:

| literal     | `content-type` header value                                                                                                                                                                                             |
|-------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `ANY`       | `*/*`                                                                                                                                                                                                                   |
| `TEXT`      | `text/plain`                                                                                                                                                                                                            |
| `JSON`      | `application/json, application/javascript, text/javascript, text/json`                                                                                                                                                  |
| `XML`       | `application/xml, text/xml, application/xhtml+xml`                                                                                                                                                                      |
| `HTML`      | `text/html`                                                                                                                                                                                                             |
| `URLENC`    | `application/x-www-form-urlencoded`                                                                                                                                                                                     |
| `BINARY`    | `application/octet-stream`                                                                                                                                                                                              |
| `MULTIPART` | `multipart/form-data`, `multipart/alternative`, `multipart/byteranges`, `multipart/digest`, `multipart/mixed`, `multipart/parallel`, `multipart/related`, `multipart/report`, `multipart/signed`, `multipart/encrypted` |

Example:
```yaml
rest:
  contentType: XML
```


### `rest.httpCodeThreshold`
- Type: `integer`
- Default: `500`

Sets a limit on HTTP response codes. Whenever a REST call returns an HTTP code equal to or greater than this value, the 
step is automatically marked as failed without checking any other conditions.

Example:
```yaml
rest:
  httpCodeThreshold: 999
```


### `rest.timeout`
- Type: `integer`
- Default: `60000`

Sets a maximum response time (in milliseconds) for subsequent HTTP requests. If this time is exceeded, the request is 
aborted and an error is raised.

Example:
```yaml
rest:
  timeout: 10000
```


### `rest.oauth2.url`
- Type: `URL`

Specifies the [OAuth 2.0][oauth2] authentication service to use to generate the token to be sent in the `Authorization` 
HTTP header of REST calls.

Example:
```yaml
rest:
  oauth2: 
    url: https://accounts.google.com/o/oauth2/auth
```


### `rest.oauth2.clientId`
- Type: `string`

Sets the `clientId` parameter for the [OAuth 2.0][oauth2] authentication service defined by the value of the 
`rest.oauth2.url` configuration property.

Example:
```yaml
rest:
  oauth2: 
    clientId: WEB_APP
```


### `rest.oauth2.clientSecret`
- Type: `string`

Sets the `clientSecret` parameter for the [OAuth 2.0][oauth2] authentication service defined by the value of the 
`rest.oauth2.url` configuration property.

Example:
```yaml
rest:
  oauth2: 
    clientSecret: ABRACADABRAus1ZMGHvq9R
```


### `rest.oauth2.cached`
- Type: `boolean`
- Default: `false`

Specifies whether to cache the retrieved token to avoid repeated calls to the oauth service for the same data.

Example:
```yaml
rest:
  oauth2:
    cached: true
```


### `rest.oauth2.parameters`
- Type: `property[]`

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


### `rest.config.multipart.subtype`
- Type: `string`
- Default: `form-data`

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

Example:
```yaml
rest:
  config:
    multipart:
      subtype: mixed
```


### `rest.config.multipart.filename`
- Type: `string`
- Default: `file`

Sets the filename of multipart HTTP requests.

Example:
```yaml
rest:
  config:
    multipart:
      filename: other_name
```


### `rest.config.redirect.follow`
- Type: `boolean`
- Default: `true`

Specifies whether to allow redirects in HTTP calls.

Example:
```yaml
rest:
  config:
    redirect:
      follow: false
```


### `rest.config.redirect.allowCircular`
- Type: `boolean`
- Default: `false`

Specifies whether to allow circular redirects in HTTP calls.

Example:
```yaml
rest:
  config:
    redirect:
      allowCircular: true
```


### `rest.config.redirect.rejectRelative`
- Type: `boolean`
- Default: `false`

Specifies whether to reject relative redirects in HTTP calls.

Example:
```yaml
rest:
  config:
    redirect:
      rejectRelative: true
```


### `rest.config.redirect.max`
- Type: `integer`
- Default: `100`

Sets the maximum number of redirects for HTTP calls.

Example:
```yaml
rest:
  config:
    redirect:
      max: 150
```


## Steps


### Define content type
```text copy=true
the REST content type {word}
```
Declarative-way for setting the configuration property [`rest.contentType`](#restcontenttype).

#### Parameters:
| Name   | Wakamiti type     | Description    |
|--------|-------------------|----------------|
| `type` | `word` *required* | Connection URL |

#### Examples:
```gherkin
Given the REST content type XML
```


### Define base URL
```text copy=true
the base URL {url}
```
Declarative-way for setting the configuration property [`rest.baseURL`](#restbaseurl).

#### Parameters:
| Name  | Wakamiti type    | Description |
|-------|------------------|-------------|
| `url` | `url` *required* | Base URL    |

#### Examples:
```gherkin
Given the base URL https://example.org/api
```


### Define service
```text copy=true
the REST service {text}
```
Sets the service path that would be tested. It would be appended to the `baseURL`.

#### Parameters:
| Name      | Wakamiti type     | Description      |
|-----------|-------------------|------------------|
| `service` | `text` *required* | URL segment path |

#### Examples:
```gherkin
Given the REST service '/users`
```


### Define id
###### Deprecated
```text copy=true
* identified by {text}
```
Sets an entity identifier to be used by the REST service. It would be appended to the `baseURL` and the service.

#### Parameters:
| Name   | Wakamiti type     | Description          |
|--------|-------------------|----------------------|
| `text` | `text` *required* | An entity identifier |

#### Examples:
```gherkin
Given a user identified by 'john'
```
```gherkin
Given the borrowed book identified by '978-3-16-148410-0'
```


### Define parameters or headers
```text copy=true
the (request|query|path|form) parameter {name} with value {value}
```
```text copy=true
the header {name} with value {value}
```
Sets a header or request, query, path or form parameter. Request parameters will be sent as form data in POST requests,
query parameters will be concatenated to the request URL after the path (e.g. `/user?param1=abc&param2=123`), path
parameters will replace the service path fragments indicate with `{}`, and form parameters will be sent with
content-type `application/x-www-form-urlencoded`.

##### Parameters:
| Name    | Wakamiti type     | Description               |
|---------|-------------------|---------------------------|
| `name`  | `text` *required* | Header or parameter name  |
| `value` | `text` *required* | Header or parameter value |

##### Examples:
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
Given the form parameter 'age' with value '13'
When the data info is sent to the service
```
```gherkin
Given the header 'Keep-alive' with value '1200'
```


### Define parameters or headers (table)
```text copy=true
the following (request|query|path) parameters:
    {table}
```
```text copy=true
the following headers:
    {table}
```
Sets multiple headers or request, query, path or form parameters. Request parameters will be sent as form data in POST
requests, query parameters will be concatenated to the request URL after the path (e.g. `/user?param1=abc&param2=123`),
path parameters will replace the service path fragments indicate with `{}`, and form parameters will be sent with
content-type `application/x-www-form-urlencoded`.

##### Parameters:
| Name    | Wakamiti type      | Description                             |
|---------|--------------------|-----------------------------------------|
| `table` | `table` *required* | A table with `name` and `value` columns |

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
Given the following form parameters:
  | Name | Value    |
  | age  | 13       |
  | city | Valencia |
When the data info is sent to the service
```
```gherkin
Given the following headers:
  | Name       | Value |
  | Age        | 3600  |
  | Keep-Alive | 1200  |
```


### Define timeout
```text copy=true
a timeout of {int} (milli)seconds
```
Sets a response timeout (in second or milliseconds) for the subsequent HTTP requests.

##### Parameters:
| Name   | Wakamiti type    | Description                 |
|--------|------------------|-----------------------------|
| `int`  | `int` *required* | The timeout in milliseconds |

##### Examples:
```gherkin
Given a timeout of 12000 milliseconds
```
```gherkin
Given a timeout of 2 seconds
```


### Define HTTP code threshold
```text copy=true
any request will fail when response HTTP code {matcher}
```
Similar to setting the configuration property [`rest.httpCodeTreshold`](#resthttpcodethreshold) but using any integer
assertion.

##### Parameters:
| Name      | Wakamiti type                  | Description             |
|-----------|--------------------------------|-------------------------|
| `matcher` | `integer-assertion` *required* | Numeric [comparator][1] |

##### Example:
```gherkin
* any request will fail when response HTTP code is greater than 500
```


### Define basic authentication
```text copy=true
the service uses the basic authentication credentials {username}:{password}
```
Sets the basic authentication credentials to be sent in the `Authorization` header for the subsequent requests.

##### Parameters:
| Name       | Wakamiti type     | Description  |
|------------|-------------------|--------------|
| `username` | `text` *required* | The username |
| `password` | `text` *required* | The password |

##### Examples:
```gherkin
Given the service uses the basic authentication credentials 'us1532':'xxxxx'
```


### Define oauth2 authentication
```text copy=true
the service uses the oauth authentication
```
Sets the bearer authentication token to be sent in the `Authorization` header, which is previously retrieved from the
configured oauth2 service ([url](#restoauth2url), [clientId](#restoauth2clientid),
[clientSecret](#restoauth2clientsecret), [parameters](#restoauth2parameters)), for the following requests.


##### Examples:
```gherkin
Given the service uses the oauth authentication
```


### Define oauth2 authentication by token
```text copy=true
the service uses the oauth authentication token {token}
```
Sets the bearer authentication token to be sent in the `Authorization` header for subsequent requests.

##### Parameters:
| Name    | Wakamiti type     | Description          |
|---------|-------------------|----------------------|
| `token` | `text` *required* | Authentication token |

##### Examples:
```gherkin
Given the service uses the oauth authentication token 'hudytw9834y9cqy32t94'
```


### Define oauth2 authentication by token (file)
```text copy=true
the service uses the oauth authentication token from the file {file}
```
Sets the bearer authentication token to be sent in the `Authorization` header for subsequent requests, from file.

##### Parameters:
| Name   | Wakamiti type     | Description                        |
|--------|-------------------|------------------------------------|
| `file` | `file` *required* | File with the authentication token |

##### Examples:
```gherkin
Given the service uses the oauth authentication token from the file 'token.txt'
```


### Define oauth2 authentication by credentials
```text copy=true
the service uses the oauth authentication credentials {username}:{password}
```
```text copy=true
the service uses the oauth authentication credentials {username}:{password} with the following parameters:
    {table}
```
Sets the bearer authentication token to be sent in the `Authorization` header, which is previously retrieved from the
configured oauth2 service ([url](#restoauth2url), [clientId](#restoauth2clientid), [clientSecret](#restoauth2clientsecret)),
using the indicated credentials, for the following requests.

Additional parameters supported by `Oauth` can also be added using a table.

##### Parameters:
| Name       | Wakamiti type     | Description                             |
|------------|-------------------|-----------------------------------------|
| `username` | `text` *required* | The username                            |
| `password` | `text` *required* | The password                            |
| `table`    | `table`           | A table with `name` and `value` columns |

##### Examples:
```gherkin
Given the service uses the oauth authentication credentials 'us1532':'xxxxx'
```

```gherkin
Given the service uses the oauth authentication credentials 'us1532':'xxxxx' with the following parameters:
  | name  | value     |
  | scope | something |
```


### Define oauth2 authentication by client
```text copy=true
the service uses the oauth authentication
```
```text copy=true
the service uses the oauth authentication with the following parameters:
    {table}
```
Sets the bearer authentication token to be sent in the `Authorization` header, which is previously retrieved from the
configured oauth2 service ([url](#restoauth2url), [clientId](#restoauth2clientid), [clientSecret](#restoauth2clientsecret)),
using client data, for the following requests.

Additional parameters supported by `Oauth` can also be added using a table.

##### Parameters:
| Name    | Wakamiti type | Description                             |
|---------|---------------|-----------------------------------------|
| `table` | `table`       | A table with `name` and `value` columns |

##### Examples:
```gherkin
Given the service uses the oauth authentication
```

```gherkin
Given the service uses the oauth authentication with the following parameters:
  | name  | value     |
  | scope | something |
```


### Clear authentication
```text copy=true
the service does not use authentication
```
Deletes the authentication header.

##### Examples:
```gherkin
Given the service does not use authentication
```


### Define multipart subtype
```text copy=true
{type} as subtype multipart
```
Sets the default subtype for multipart requests. This step is equivalent to setting the
[`rest.config.multipart.subtype`](#restconfigmultipartsubtype) property. Available values are:

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

##### Parameters:
| Name   | Wakamiti type     | Description       |
|--------|-------------------|-------------------|
| `type` | `text` *required* | Multipart subtype |

##### Examples:
```gherkin
Given 'mixed' as subtype multipart
```


### Define multipart filename
```text copy=true
{name} as attached file name
```
Sets the default name for multipart files. This step is equivalent to setting the
[`rest.config.multipart.filename`](#restconfigmultipartfilename).

##### Parameters:
| Name   | Wakamiti type     | Description         |
|--------|-------------------|---------------------|
| `name` | `text` *required* | Multipart file name |

##### Examples:
```gherkin
Given 'other_name' as attached file name
```


### Define attached file
```text copy=true
the attached file is included with the following data:
    {data}
```
Sets a multipart form-data including an attachment from the given in-document content.

##### Parameters:
| Name   | Wakamiti type         | Description          |
|--------|-----------------------|----------------------|
| `data` | `document` *required* | Content to be attach |

##### Examples:
```gherkin
Given the attached file is included with the following data:
  """
  File content
  """
```


### Define attached file (file)
```text copy=true
the attached file {file} is included
```
Sets a multipart form-data including an attachment from the given in-document content.

##### Parameters:
| Name   | Wakamiti type     | Description       |
|--------|-------------------|-------------------|
| `file` | `file` *required* | File to be attach |

##### Examples:
```gherkin
Given the attached file 'data/data.txt' is included
```


### Execute GET request
```text copy=true
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


### Execute DELETE request
```text copy=true
* (is|are) deleted
```
Sends a `DELETE` request to the previously defined endpoint formed with the base URL, the REST service and the entity id.

##### Examples:
```gherkin
Given the REST service 'users'
And a user identified by '123'
When the user is deleted
```


### Execute PUT request with body
```text copy=true
* (is|are) modified with the following data:
    {data}
```
Sends a `PUT` request to the previously defined endpoint formed with the base URL, the REST service and the entity id.
The payload is provided in-document.

##### Parameters:
| Name   | Wakamiti type         | Description              |
|--------|-----------------------|--------------------------|
| `data` | `document` *required* | The request payload body |

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


### Execute PUT request with body (file)
```text copy=true
* (is|are) modified with the data from the file {file}
```
Sends a `PUT` request to the previously defined endpoint formed with the base URL, the REST service and the entity id.
The payload is provided by the given file.

##### Parameters:
| Name   | Wakamiti type     | Description                                |
|--------|-------------------|--------------------------------------------|
| `file` | `file` *required* | A file containing the request payload body |

##### Examples:
```gherkin
Given the REST service 'users'
And a user identified by '123'
When the user is modified with the data from the file 'data/user123.json'
```


### Execute PATCH request
```text copy=true
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


### Execute PATCH request with body
```text copy=true
* (is|are) patched with the following data:
    {data}
```
Sends a `PATCH` request to the previously defined endpoint formed with the base URL, the REST service and the entity id.
The payload is provided in-document.

##### Parameters:
| Name   | Wakamiti type         | Description              |
|--------|-----------------------|--------------------------|
| `data` | `document` *required* | The request payload body |

##### Examples:
```gherkin
Given the REST service 'users'
And a user identified by '123'
When the user is patched with the following data:
  """json
  { "firstName": "Jim" }
  """
```


### Execute PATCH request with body (file)
```text copy=true
* (is|are) patched with the data from the file {file}
```
Sends a `PATCH` request to the previously defined endpoint formed with the base URL, the REST service and the entity id.
The payload is provided by the given file.

##### Parameters:
| Name   | Wakamiti type     | Description                                |
|--------|-------------------|--------------------------------------------|
| `file` | `file` *required* | A file containing the request payload body |

##### Examples:
```gherkin
Given the REST service 'users'
And a user identified by '123'
When the user is patched with the data from the file 'data/user123.json'
```


### Execute POST request
```text copy=true
* (is|are) created
```
```text copy=true
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


### Execute POST request with body
```text copy=true
* (is|are) created with the following data:
    {data}
```
```text copy=true
the following data is sent to the service:
    {data}
```
Send a `POST` request to the previously defined endpoint formed with the base URL and the REST service. The payload is
provided in-document.

##### Parameters:
| Name   | Wakamiti type         | Description     |
|--------|-----------------------|-----------------|
| `data` | `document` *required* | Request payload |

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
  { "date": "2021-10-30" }
  """
```


### Execute POST request with body (file)
```text copy=true
* (is|are) created with the data from the file {file}
```
```text copy=true
the data from the file {file} is sent to the service
```
Sends a `POST` request to the previously defined endpoint formed with the base URL and the REST service. The payload is
provided by the given file.

##### Parameters:
| Name   | Wakamiti type     | Description                                |
|--------|-------------------|--------------------------------------------|
| `file` | `file` *required* | A file containing the request payload body |

##### Examples:
```gherkin
When the user is created with the data from the file 'data/user123.json'
```
```gherkin
When the data from the file 'data/booking.json' is sent to the service
```


### Check response HTTP code
```text copy=true
the response HTTP code {matcher}
```
Validate that the HTTP code of the last response satisfies the given assertion.

##### Parameters:
| Name      | Wakamiti type                  | Description            |
|-----------|--------------------------------|------------------------|
| `matcher` | `integer-assertion` *required* | Number [comparator][1] |

##### Examples:
```gherkin
Then the response HTTP code is equal to 201
```


### Check response body
```text copy=true
the response is:
    {data}
```
Validates that the response body is exactly the content of the in-document content.
```text copy=true
the response is \(in any order\):
    {data}
```
Validates that the response body has all the elements specified by the in-document content, but in any order.
```text copy=true
the response contains:
    {data}
```
Validates that the response body contains the given in-document content.

##### Parameters:
| Name   | Wakamiti type         | Description                  |
|--------|-----------------------|------------------------------|
| `data` | `document` *required* | The expected partial content |

##### Examples:
```gherkin
Then the response is:
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
Then the response is (in any order):
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
Then the response contains:
  """json
  [ { "name": "John" } ]
  """
```


### Check response body (file)
```text copy=true
the response is equal to the file {file}
```
Validates that the response body is exactly the content of the given file.
```text copy=true
the response is equal to the file {file} \(in any order\)
```
Validates that the response body has all the elements provided by the given file, but in any order.
```text copy=true
the response contains the file {file}
```
Validates that the response body contains the content of the given file.

##### Parameters:
| Name   | Wakamiti type     | Description      |
|--------|-------------------|------------------|
| `file` | `file` *required* | An existing file |

##### Examples:
```gherkin
Then the response contains the file 'data/response1.json'
```


### Check response body fragment
```text copy=true
the response fragment {fragment} is:
    {data}
```
Validates that a [JSONPath][jsonpath], [XPath][xpath] or [GPath][gpath] response fragment is exactly the content of
the in-document content.
```text copy=true
the response fragment {fragment} is \(in any order\):
    {data}
```
Validates that a [JSONPath][jsonpath], [XPath][xpath] or [GPath][gpath] response fragment has all the elements specified
by the in-document content, but in any order.
```text copy=true
the response fragment {fragment} contains:
    {data}
```
Validates that a [JSONPath][jsonpath], [XPath][xpath] or [GPath][gpath] response fragment contains the given in-document
content.

##### Parameters:
| Name       | Wakamiti type         | Description                      |
|------------|-----------------------|----------------------------------|
| `fragment` | `text`                | A JSONPath, XPath or GPath query |
| `data`     | `document` *required* | The expected partial content     |

##### Examples:
```gherkin
Then the response fragment 'users[1]' is:
  """json
  {
    "age": 23,
    "name": "John"
  }
  """
```
```gherkin
Then the response fragment 'users[1]' is (in any order):
  """json
  {
    "name": "John",
    "age": 23
  }
  """
```
```gherkin
Then the response fragment 'users[1]' contains:
  """json
  { "name": "John" }
  """
```


### Check response body fragment (file)
```text copy=true
the response is equal to the file {file}
```
Validates that a [JSONPath][jsonpath], [XPath][xpath] or [GPath][gpath] response fragment is exactly the content of the
given file.
```text copy=true
the response is equal to the file {file} \(in any order\)
```
Validates that a [JSONPath][jsonpath], [XPath][xpath] or [GPath][gpath] response fragment has all the elements specified
by the given file, but in any order.
```text copy=true
the response contains the file {file}
```
Validates that a [JSONPath][jsonpath], [XPath][xpath] or [GPath][gpath] response fragment contains the given file.

##### Parameters:
| Name       | Wakamiti type     | Description                      |
|------------|-------------------|----------------------------------|
| `fragment` | `text`            | A JSONPath, XPath or GPath query |
| `file`     | `file` *required* | An existing file                 |

##### Examples:
```gherkin
Then the response fragment 'users[1]' contains the file 'data/response1.json'
```


### Check response body fragment (value)
```text copy=true
the (text|integer|decimal) from response fragment {fragment} {matcher}
```
Validates the value from a [JSONPath][jsonpath], [XPath][xpath] or [GPath][gpath] response fragment according a
*text*, *integer* or *decimal* assertion.

##### Parameters:
| Name       | Wakamiti type            | Description                      |
|------------|--------------------------|----------------------------------|
| `fragment` | `text` *required*        | A JSONPath, XPath or GPath query |
| `matcher`  | `*-assertion` *required* | [Comparator][1]                  |
`*`: `text`, `integer` o `decimal`.

##### Examples:
```gherkin
Then the decimal from response fragment 'users[1].account.availableMoney' is greater than 23.57
```
```gherkin
Then the decimal from response fragment 'users[0].lastName' starts with 'J'
```


### Check response content type
```text copy=true
the response content type is {word}
```
Validate that the content type of the last response is the expected. This step would be equivalent to validate the
`Content-Type` response header against the corresponding MIME type.

##### Parameters:
| Name   | Wakamiti type     | Description                                        |
|--------|-------------------|----------------------------------------------------|
| `word` | `word` *required* | `ANY`,`TEXT`,`JSON`,`XML`,`HTML`,`URLENC`,`BINARY` |

##### Examples:
```gherkin
Then the response content type is JSON
```


### Check response size
```text copy=true
the response length {matcher}
```
Validate that the length in bytes of the last response satisfies the given assertion.

##### Parameters:
| Name      | Wakamiti type                  | Description            |
|-----------|--------------------------------|------------------------|
| `matcher` | `integer-assertion` *required* | Number [comparator][1] |

##### Examples:
```gherkin
Then the response length is less than 500
```


### Check response header
```text copy=true
the (text|integer|decimal) response header {name} {matcher}
```
Validate that a header value from the last REST response satisfies the *text*, *integer* or *decimal* assertion.

##### Parameters:
| Name      | Wakamiti type            | Description          |
|-----------|--------------------------|----------------------|
| `name`    | `text` *required*        | The HTTP header name |
| `matcher` | `*-assertion` *required* | [Comparator][1]      |
`*`: `text`, `integer` o `decimal`.

##### Examples:
```gherkin
Then the text response header Content-Encoding contains 'gzip'
```
```gherkin
Then the integer response header Age is greater than 10
```


### Check response schema
```text copy=true
the response satisfies the following schema:
    {data}
```
Asserts that the response body structure satisfies a given schema. The accepted schema formats are
[JSON Schema](https://json-schema.org/) for JSON responses and [XML Schema](https://www.w3.org/2001/XMLSchema) for XML
responses (according the`Content-Type` response header).

#### Parameters:
| name   | Wakamiti type         | description               |
|--------|-----------------------|---------------------------|
| `data` | `document` *required* | JSON Schema or XML Schema |

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


### Check response schema (file)
```text copy=true
the response satisfies the schema from the file {file}
```
Asserts that the response body structure satisfies a schema from a given file. The accepted schema formats are
[JSON Schema][jsonschema] for JSON responses and [XML Schema][xmlschema] for XML responses (according the `Content-Type`
response header).

##### Parameters:
| name   | Wakamiti type     | description                    |
|--------|-------------------|--------------------------------|
| `file` | `file` *required* | JSON Schema or XML Schema file |

##### Examples:
```gherkin
Then the response satisfies the schema from the file 'data/user-schema.json'
```




[oauth2]: https://datatracker.ietf.org/doc/html/rfc6749 (OAuth 2.0)
[jsonschema]: https://json-schema.org/ (JSON Schema)
[jsonpath]: https://goessner.net/articles/JsonPath/
[xmlschema]: https://www.w3.org/2001/XMLSchema (XML Schema)
[xpath]: https://en.wikipedia.org/wiki/XPath (XPath)
[gpath]: https://accenture.github.io/bdd-for-all/GPATH.html (GPath)
[1]: wakamiti/architecture#comparators
