This plugin provides a set of steps for conducting performance testing using JMeter DSL

---
## Table of Contents
   1. [Test Execution](#test-execution)
   2. [Configuration](#configuration)
      - [jmeter.baseURL](#jmeterbaseurl)
      - [Plugin Outputs](#plugin-outputs)
        - [jmeter.output.influxdb.enabled](#jmeteroutputinfluxdbenabled)
        - [jmeter.output.influxdb.url](#jmeteroutputinfluxdburl)
        - [jmeter.output.csv.enabled](#jmeteroutputcsvenabled)
        - [jmeter.output.csv.path](#jmeteroutputcsvpath)
        - [jmeter.output.html.enabled](#jmeteroutputhtmlenabled)
        - [jmeter.output.html.path](#jmeteroutputhtmlpath)
        - [jmeter.output.resultstree.enabled](#jmeteroutputresultstreeenabled)
   2. [Steps](#steps)
      - [Set Base URL](#set-base-url)
      - [Define CSV Input](#define-csv-input)
      - [Define CSV Input with Variables](#define-csv-input-with-variables)
      - [Perform GET Request](#perform-get-request)
      - [Perform PUT Request](#perform-put-request)
      - [Perform POST Request](#perform-post-request)
      - [Perform GET Request and Extract Variable with Regular Expression](#perform-get-request-and-extract-variable-with-regular-expression)
      - [Perform GET Request and Extract Variable with Delimiters](#perform-get-request-and-extract-variable-with-delimiters)
      - [Perform GET Request and Extract Variable with JSON Path](#perform-get-request-and-extract-variable-with-json-path)
      - [Perform PUT Request with Extracted Variable](#perform-put-request-with-extracted-variable)
      - [Perform POST Request with Extracted Variable](#perform-post-request-with-extracted-variable)
      - [Perform GET Request to an Endpoint with Extracted Variable](#perform-get-request-to-an-endpoint-with-extracted-variable)
      - [Perform PUT Request to an Endpoint with Extracted Variable](#perform-put-request-to-an-endpoint-with-extracted-variable)
      - [Perform POST Request to an Endpoint with Extracted Variable](#perform-post-request-to-an-endpoint-with-extracted-variable)
      - [Define Basic Authentication with Credentials](#define-basic-authentication-with-credentials)
      - [Define Basic Authentication](#define-basic-authentication)
      - [Disable Cookies](#disable-cookies)
      - [Disable Cache](#disable-cache)
      - [Set Connection Timeout](#set-connection-timeout)
      - [Set Response Timeout](#set-response-timeout)
      - [Download Associated Resources](#download-associated-resources)
      - [Use a Proxy Server](#use-a-proxy-server)
      - [Execute JMX Test Plan](#execute-jmx-test-plan)
      - [Execute Smoke Test](#execute-smoke-test)
      - [Execute Load Test](#execute-load-test)
      - [Execute Stress Test](#execute-stress-test)
      - [Execute Spike Test](#execute-spike-test)
      - [Execute Operational Limit Test](#execute-operational-limit-test)
      - [Check Response Time Percentile](#check-response-time-percentile)
      - [Check Average Response Time](#check-average-response-time)
      - [Check for Request Errors](#check-for-request-errors)

---
## Test Execution

To use the Wakamiti JMeter DSL plugin, you currently need to:

1. Create a `.feature` file inside the `test/resources/features` folder and write the scenarios you want to execute.
2. For JUnit execution: Run tests by right-clicking on the `JMeterTest` class located in `test/java/es.iti.wakamiti.plugins.jmeter.test` and selecting `Run`.
3. For Maven execution: Run `mvn clean install` from the console to start the tests.


## Configuration

To configure the outputs and different options of the plugin, you currently need to modify the JMeterConfigContributor.java file located in `src/main/java/es.iti.wakamiti.plugin.jmeter`. There you can adjust the properties as needed by modifying the Configuration DEFAULTS:

```
private static final Configuration DEFAULTS = Configuration.factory().fromPairs(
   BASE_URL, "http://localhost:8080",
   RESULTSTREE_ENABLED, "true",
   INFLUXDB_ENABLED, "false",
   CSV_ENABLED, "false",
   HTML_ENABLED, "false",
   INFLUXDB_URL, "http://localhost:8086/write?db=jmeter",
   CSV_PATH, "./test-results.csv",
   HTML_PATH, "./test-results.html"
);
```
In this case, the only output would be the JMeter Results Tree.

###  `jmeter.baseURL`
Sets the base URL for JMeter tests, which will define the starting point for all HTTP calls that are made.

Default value:
```
 `http://localhost:8080`
```

Example:
```yaml
jmeter:
  baseURL: https://example.org/api/v2
```

<br /><br />

## Plugin outputs
JMeter can be configured to send test results to different destinations like InfluxDB, CSV files, and HTML reports.

###  `jmeter.output.influxdb.enabled`
Determines whether the output to InfluxDB is enabled. This allows you to maintain a record of all execution statistics through Grafana and view the metrics of interest graphically.
It is important to know that an existing InfluxDB and Grafana server are used, so if you wish to test locally, you should execute a 'docker compose up' within this directory.
Once the containers have started, you can open grafana at http://localhost:3000.


Default value:
```
 `false`
```

Example:
```yaml
jmeter:
  output:
    influxdb:
      enabled: true
```

<br /><br />

### `jmeter.output.influxdb.url`
Defines the InfluxDB endpoint URL to which JMeter will send the results.

Default value:
```
 `"http://localhost:8086/write?db=jmeter"`
```

Example:
```yaml
jmeter:
  output:
    influxdb:
      url: http://tuServidorInfluxDB.com/write?db=nombreDeTuBaseDeDatos
```

<br /><br />

### `jmeter.output.csv.enabled`
Enables the generation of test results into a CSV file. The file can be viewed through Excel by navigating to Data -> Get Data -> From File -> From Text/CSV and selecting the generated file.

Default value:
```
 `true`
```

Example:
```yaml
jmeter:
  output:
    csv:
      enabled: false
```

<br /><br />

### `jmeter.output.csv.path`
Specifies the path where the CSV file with test results will be saved.

Default value:
```
 `"./test-results-csv"`
```

Example:
```yaml
jmeter:
  output:
    csv:
      path: ./resultadosDePruebas/pruebas-csv
```

<br /><br />


### `jmeter.output.html.enabled`
Enables the creation of an HTML report with test results.

Default value:
```
 `false`
```

Example:
```yaml
jmeter:
  output:
    html:
      enabled: true
```

<br /><br />

### `jmeter.output.html.path`
Specifies the path where the HTML report with test results will be saved.

Default value:
```
 `"./test-results-html"`
```

Example:
```yaml
jmeter:
  output:
    html:
      path: ./resultadosDePruebas/pruebas-html
```

<br /><br />

### `jmeter.output.resultstree.enabled`
Enables the JMeter Results Tree, which allows you to review all HTTP requests and responses in detail.

Default value:
```
 `false`
```

Example:
```yaml
jmeter:
  output:
    resultstree:
      enabled: true
```

---
## Steps

### Set Base URL
```
the base URL {text}
```
Sets the base path. This step is equivalent to configuring the [`rest.baseURL`](#restbaseurl) property.

#### Parameters:
| Name   | Wakamiti type | Description |
|--------|---------------|-------------|
| `url`  | `text`        | Base URL    |

#### Example:
```gherkin
  Given the base URL https//example.org/api
```

<br /><br />

### Define CSV Input
```
a file with the following data {file:text}
```
Sets a CSV file as input for JMeter tests.

#### Parameters:
| Name       | Wakamiti type | Description                                  |
|------------|---------------|----------------------------------------------|
| `file`     | `text`        | Path to the CSV file with input data         |

#### Example:
```gherkin
     Given a file with the following data './users.csv'
```

<br /><br />

### Define CSV Input with Variables
```
a file with the following data {file:text} working with the variables:
```
Sets a CSV file as input and specifies the variables to be used during JMeter tests. It allows filtering and using only certain columns from the CSV file as variables within the test.

#### Parameters:
| Name       | Wakamiti type | Description                                       |
|------------|---------------|---------------------------------------------------|
| `file`     | `text`        | Path to the CSV file with input data              |
|            | `DataTable`   | A table specifying the variables to be used       |

#### Example:
```gherkin
Given a file with the following data 'users.csv' working with the variables:
   | username |
   | password |
```

<br /><br />

### Perform GET Request
```
 perform a GET to the endpoint {service:text}
```
Sends a GET request to the previously defined parameters.

#### Parameters:
| Name      | Wakamiti type | Description  |
|-----------|---------------|--------------|
| `service` | `text`        | URL segment  |

#### Example:
```gherkin
 perform a GET to the endpoint '/api/users'
```

<br /><br />

### Perform PUT Request
```
perform a PUT to the endpoint {service:text} with the following message:
```
Sends a PUT request to the previously defined parameters.

#### Parameters:
| Name      | Wakamiti type | Description              |
|-----------|---------------|--------------------------|
| `service` | `text`        | URL segment              |
|           | `document`    | The body of the request  |

#### Example:
```gherkin
 perform a PUT to the endpoint '/api/users/123' with the following message:
    """
    {
        "firstName": "Ana",
        "lastName": "Gómez"
    }
    """
```

<br /><br />

### Perform POST Request
```
perform a POST to the endpoint {service:text} with the following message:
```
Sends a POST request to the previously defined parameters.

#### Parameters:
| Name      | Wakamiti type | Description              |
|-----------|---------------|--------------------------|
| `service` | `text`        | URL segment              |
|           | `document`    | The body of the request  |

#### Example:
```gherkin
 perform a POST to the endpoint '/api/users/123' with the following message:
    """
    {
        "firstName": "Ana",
        "lastName": "Gómez"
    }
    """
```

<br /><br />

### Perform GET Request and Extract Variable with Regular Expression
```
(I) perform a GET to the endpoint {service:text} and extract the value using regular expression {regex:text} and store it in {variableName:text}
```
Performs a GET request to the specified endpoint and extracts a value using a regular expression, storing the result in a variable.

#### Parameters: 
| Name           | Wakamiti type | Description                                              |
|----------------|---------------|----------------------------------------------------------|
| `service`      | `text`        | URL segment                                              |
| `variableName` | `text`        | Name of the variable where the extracted value is stored |
| `regex`        | `text`        | Regular expression for extraction                        |

#### Example:
```gherkin
  (I) perform a GET to the endpoint '/api/users' and extract the value using regular expression '\d+' and store it in 'userID'
```

<br /><br />

### Perform GET Request and Extract Variable with Delimiters
```
(I) perform a GET to the endpoint {service:text} and extract the value between {leftBoundary:text} and {rightBoundary:text} and store it in {variableName:text}
```
Performs a GET request and extracts a value delimited by specific texts.

#### Parameters: 
| Name           | Wakamiti type | Description                                              |
|----------------|---------------|----------------------------------------------------------|
| `service`      | `text`        | URL segment                                              |
| `variableName` | `text`        | Name of the variable where the extracted value is stored |
| `leftBoundary` | `text`        | Left delimiter                                           |
| `rightBoundary`| `text`        | Right delimiter                                          |

#### Example:
```gherkin
    (I) perform a GET to the endpoint '/api/users/details' and extract the value between 'id:' and ',' and store it in 'userID'
```

<br /><br />

### Perform GET Request and Extract Variable with JSON Path
```
(I) perform a GET to the endpoint {service:text} and extract the value at the Json Path {jsonPath:text} and store it in {variableName:text}
```
Performs a GET request and extracts a value specified by a JSON Path.

#### Parameters: 
| Name           | Wakamiti type | Description                                              |
|----------------|---------------|----------------------------------------------------------|
| `service`      | `text`        | URL segment                                              |
| `variableName` | `text`        | Name of the variable where the extracted value is stored |
| `jsonPath`     | `text`        | JSON Path for extraction                                 |

#### Example:
```gherkin
   (I) perform a GET to the endpoint '/api/users/details' and extract the value at the Json Path '$.id' and store it in 'userID'
```

<br /><br />


### Perform PUT Request with Extracted Variable
```
(I) perform a PUT to the endpoint {service:text} with the stored variable {variableName:text} as the message body
```
Sends a PUT request using the value of a previously extracted variable as the message body.

#### Parameters: 
| Name           | Wakamiti type | Description                                              |
|----------------|---------------|----------------------------------------------------------|
| `service`      | `text`        | URL segment                                              |
| `variableName` | `text`        | Name of the variable where the extracted value is stored |

#### Example:
```gherkin
 (I) perform a PUT to the endpoint '/api/users/123' with the stored variable 'userData' as the message body
```

<br /><br />

### Perform POST Request with Extracted Variable
```
(I) perform a POST to the endpoint {service:text} with the stored variable {variableName:text} as the message body
```
Sends a POST request using the value of a previously extracted variable as the message body.

#### Parameters: 
| Name           | Wakamiti type | Description                                              |
|----------------|---------------|----------------------------------------------------------|
| `service`      | `text`        | URL segment                                              |
| `variableName` | `text`        | Name of the variable where the extracted value is stored |

#### Example:
```gherkin
 (I) perform a POST to the endpoint '/api/users/123' with the stored variable 'userData' as the message body
```

<br /><br />

### Perform GET Request to an Endpoint with Extracted Variable
```
(I) perform a GET to the endpoint {service:text} / and the previously extracted variable {variableName:text}
```
Performs a GET request to an endpoint composed of a base URL segment followed by a slash and the value of a previously extracted variable.

#### Parameters: 
| Name           | Wakamiti type | Description                                                    |
|----------------|---------------|----------------------------------------------------------------|
| `service`      | `text`        | Base URL segment                                               |
| `variableName` | `text`        | Name of the variable containing the value to append to the URL |

#### Example:
```gherkin
   (I) perform a GET to the endpoint '/api/user' / and the previously extracted variable 'userId'
```

<br /><br />

### Perform PUT Request to an Endpoint with Extracted Variable
```
(I) perform a PUT to the endpoint {service:text} / and the previously extracted variable {variableName:text} with the following message:
```
Sends a PUT request to an endpoint composed of a base URL segment followed by a slash and the value of a previously extracted variable, with a specific message body.

#### Parameters: 
| Name           | Wakamiti type | Description                                                    |
|----------------|---------------|----------------------------------------------------------------|
| `service`      | `text`        | Base URL segment                                               |
| `variableName` | `text`        | Name of the variable containing the value to append to the URL |
| `body`         | `Document`    | Message body                                                   |


#### Example:
```gherkin
   (I) perform a PUT to the endpoint '/api/user' / and the previously extracted variable 'userId' with the following message:
    """
    {
        "firstName": "Updated",
        "lastName": "User"
    }
    """
```

<br /><br />

### Perform POST Request to an Endpoint with Extracted Variable
```
(I) perform a POST to the endpoint {service:text} / and the previously extracted variable {variableName:text} with the following message:
```
Sends a POST request to an endpoint composed of a base URL segment followed by a slash and the value of a previously extracted variable, with a specific message body.

#### Parameters: 
| Name           | Wakamiti type | Description                                                    |
|----------------|---------------|----------------------------------------------------------------|
| `service`      | `text`        | Base URL segment                                               |
| `variableName` | `text`        | Name of the variable containing the value to append to the URL |
| `body`         | `Document`    | Message body                                                   |


#### Example:
```gherkin
   (I) perform a POST to the endpoint '/api/user' / and the previously extracted variable 'userId' with the following message:
    """
    {
        "firstName": "Updated",
        "lastName": "User"
    }
    """
```

<br /><br />

### Define Basic Authentication with Credentials
```
a service uses basic authentication with the credentials {username:text}:{password:text}
```
Sets up basic authentication for HTTP requests.

#### Parameters:
| Name       | Wakamiti type | Description            |
|------------|---------------|------------------------|
| `username` | `text`        | Username               |
| `password` | `text`        | Password               |

#### Examples:
```gherkin
  Given a service uses basic authentication with the credentials 'Mariano' : 'mariano123@'
```

<br /><br />

### Define Basic Authentication
```
a service uses basic authentication
```
Sets up basic authentication for HTTP requests with default configured credentials.

#### Examples:
```gherkin
  Given a service uses basic authentication
```

<br /><br />

### Disable Cookies
```
(that) I disable cookies
```
Disables cookie handling by JMeter for all HTTP requests.

#### Examples:
```gherkin
   Given that I disable cookies
```

<br /><br />

### Disable Cache
```
(that) I disable the cache
```
Disables caching of HTTP requests in JMeter.

#### Ejemplos:
```gherkin
 Given that I disable the cache
```

<br /><br />

### Set Connection Timeout
```
I set a connection timeout of {duration:int} seconds
```
Defines a maximum wait time for establishing a connection.

#### Parameters:
| Name       | Wakamiti type | Description            |
|------------|---------------|------------------------|
| `duracion` | `int`         | Timeout duration       |

#### Examples:
```gherkin
   Given that I set a connection timeout of 30 seconds
```

<br /><br />

### Set Response  Timeout
```
I set a Response  timeout of {duration:int} minutes
```
Defines a maximum wait time for receiving a response .

#### Parameters:
| Name       | Wakamiti type | Description            |
|------------|---------------|------------------------|
| `duracion` | `int`         | Timeout duration       |

#### Examples:
```gherkin
   Given that I set a response  timeout of 2 minutes
```

<br /><br />

### Download Associated Resources
```
(that) I download all resources associated with the URL
```
Configures JMeter to automatically download embedded resources associated with HTML requests (e.g., images, scripts, styles).

#### Examples:
```gherkin
   Given that I download all resources associated with the URL
```

<br /><br />

### Use a Proxy Server
```
a proxy server is used at the URL {URL:text}
```
Configures JMeter to route all requests through a proxy server.

#### Parameters:
| Name      | Wakamiti type | Description              |
|-----------|---------------|--------------------------|
| `URL`     | `text`        | Proxy server URL         |

#### Examples:
```gherkin
    Given that a proxy server is used at the URL 'http://myProxy:8080'
```

<br /><br />

### Execute JMX Test Plan
```
I execute the test plan from the JMX file {archivo:text}
```
Runs a JMeter test plan specified in a JMX file.

#### Parameters:
| Name      | Wakamiti type | Description              |
|-----------|---------------|--------------------------|
| `archivo` | `text`        | Path to the JMX file     |

#### Ejemplos:
```gherkin
   When I execute the test plan from the JMX file 'plan.jmx'
```

<br /><br />

### Execute Smoke Test
```
I execute a smoke test
```
Runs a basic smoke test to check if the system under test is operational.

#### Example:
```gherkin
 When I execute a smoke test
```

<br /><br />

### Execute Load Test
```
I execute a load test with {users:int} users for {duration:int} minutes
```
Runs a load test simulating the activity of a specific number of users for a set duration.

#### Parameters:
| Name      | Wakamiti type | Description              |
|-----------|---------------|--------------------------|
| `users`   |   `int`       | Number of users          |
| `duration`|   `int`       | Length of the test       |

#### Example:
```gherkin
  When I execute a load test with 500 users for 10 minutes
```

<br /><br />

### Execute Stress Test
```
I execute a stress test starting with {users:int} users, increasing by {incrementUsers:int} up to {maxUsers:int} users over {duration:int} minutes
```
Runs a stress test by gradually increasing the load of users and maintaining it to identify the breaking point of the system.

#### Parameters:
| Name                | Wakamiti type | Description                                   |
|---------------------|---------------|-----------------------------------------------|
| `users`             |   `int`       | Initial number of users                       |
| `incrementUsers`    |   `int`       | User increment per interval                   |
| `maxUsers`          |   `int`       | Maximum number of users                       |
| `duration`          |   `int`       | Duration to maintain the load per interval    |

#### Example:
```gherkin
  When I execute a stress test starting with 100 users, increasing by 100 up to 2000 users over 5 minutes
```

<br /><br />

### Execute Spike Test
```
I execute a spike test with {numberOfSpikes:int} spikes of {usersPerSpike:int} users, dropping to {usersOffPeak:int} users over {duration:int} minutes
```
Runs a spike test to simulate irregular loads on the system, alternating between a high and low number of users.

#### Parameters:
| Name                | Wakamiti type | Description                                              |
|---------------------|---------------|----------------------------------------------------------|
| `numberOfSpikes`    |   `int`       | Number of load spikes                                    |
| `usersPerSpike`     |   `int`       | Number of users at each spike                            |
| `usersOffPeak`      |   `int`       | Number of users off the spikes                           |
| `duration`          |   `int`       | Duration to maintain the load before a spike             |

#### Example:
```gherkin
  When I execute a spike test with 3 spikes of 1000 users, dropping to 200 users over 5 minutes
```

<br /><br />

### Execute Operational Limit Test
```
I execute an operational limit test starting with {users:int} users, increasing by {incrementUsers:int} up to {maxUsers:int} users with ramp-up periods of {duration:int} minutes
```
Runs a test to find the operational limit of the system, progressively increasing the number of users.

#### Parameters:
|  Name               | Wakamiti type | Description                                              |
|---------------------|---------------|----------------------------------------------------------|
| `users`             |   `int`       | Initial number of users                                  |
| `incrementUsers`    |   `int`       | User increment                                           |
| `maxUsers`          |   `int`       | Maximum number of users                                  |
| `duration`          |   `int`       | Ramp-up time before increasing users                     |

#### Example:
```gherkin
  When I execute an operational limit test starting with 100 users, increasing by 100 up to 5000 users with ramp-up periods of 2 minutes
```

<br /><br />

### Check Response Time Percentile
```
check that the {percentile:int} percentile of response time is less than {testDuration:int} seconds
```
Verifies that the specified response time percentile is less than the given duration.

#### Parameters:
| Name          | Wakamiti type | Description                                   |
|---------------|---------------|-----------------------------------------------|
| `percentile`  |   `int`       | Response time percentile to check             |
| `testDuration`|   `int`       | Expected maximum duration                     |

#### Example:
```gherkin
 Then check that the 99 percentile of response time is less than 2 seconds
```

<br /><br />

### Check Average Response Time
```
check that the average response time is less than {testDuration:int} seconds
```
Verifies that the average response time is less than the given duration.

#### Parameters:
| Name          | Wakamiti type | Description                                   |
|---------------|---------------|-----------------------------------------------|
| `testDuration`|   `int`       | Expected maximum duration                     |

#### Example:
```gherkin
  Then check that the average response time is less than 2 seconds
```

<br /><br />

### Check for Request Errors
```
check that the number of requests that returned an error is less than {errors:int}
```
Verifica que el número de peticiones que han devuelto un error es menor que el número especificado.

#### Parameters:
| Name     | Wakamiti type | Description               |
|----------|---------------|---------------------------|
| `errors` |   `int`       | Maximum expected errors   |

#### Example:
```gherkin
  Then check that the number of requests that returned an error is less than 10
```

