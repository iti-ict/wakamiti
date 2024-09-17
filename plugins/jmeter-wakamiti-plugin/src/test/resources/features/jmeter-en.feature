
@launcher
Feature: Jmeter test


  Scenario: Smoke test
    Given APPLICATION_XML as content type
    And that any request will be successful if its HTTP code is less than 300
    And a timeout of 1 second
    And that cookies are disabled
    And that cache is disabled
    And that embedded resources will not be downloaded
    And variable 'userId' with value 'user1'
    And a GET call to the service '/users/{userId}'
    * with the following data:
      """xml
      <user>
        <name>User1</name>
      </user>
      """
    When 1 thread is executed
    Then the number of samples is 1
    And the number of errors is 0
    And the number of samples per second is greater than 0.0


  Scenario Outline: multiple calls
    Given that cookies are enabled
    And that any request will be successful if its HTTP code is less than 300
    And that cache is enabled
    And that embedded resources will be downloaded
    And that embedded resources matching the '.*' pattern are downloaded
    And a GET call to the service '/users'
    * with the following headers:
      | name   | value |
      | x-data | test  |
    * with the following parameters:
      | name   | value  |
      | param1 | value1 |
    * with <extractor> extracted in the variable 'userId'
    And a GET call to the service '/users/{userId}'
    * with the header 'x-data' with value 'test'
    * with the form parameter 'param1' with value 'value1'
    When 1 thread is executed
    Then the number of samples is 2
    And the number of errors is 0

    Examples:
      | extractor                                  |
      | json value '[0].id'                        |
      | the regular expression '"id":\s*"([^"]+)"' |
      | the fragment between '"id":"' and '"'      |


  Scenario Outline: proxy
    Given that embedded resources that do not match the '.*' pattern are downloaded
    And that any request will be successful if its HTTP code is less than 300
    And a proxy with the URL <step>
    And the following variables:
      | name   | value |
      | userId | user1 |
    And a GET call to the service '/users/{userId}'
    * with the data from the file '${data.dir}/data/token.json'
    When 1 thread is executed
    Then the number of samples is 1
    And the number of errors is 0

    Examples:
      | step                                          |
      | ${jmeter.baseURL}                             |
      | ${jmeter.baseURL} and credentials 'abc':'123' |


  Scenario Outline: authentication <info>
    Given the service uses <authentication>
    And that any request will be successful if its HTTP code is less than 300
    And a GET call to the service '/users'
    When 1 thread is executed
    Then the number of samples is 1
    And the number of errors is 0

    Examples:
      | authentication                                                             | info        |
      | the basic authentication credentials 'abc':'123'                           | basic       |
      | the oauth authentication token 'abc123'                                    | token       |
      | the oauth authentication token from the file '${data.dir}/data/token.json' | token       |
      | the oauth authentication                                                   | default     |
      | the oauth authentication credentials 'abc':'123'                           | credentials |
      | the oauth authentication client credentials                                | client      |


  Scenario Outline: authentication <info> with parameters
    Given the service uses <authentication> with the following parameters:
      | name  | value |
      | scope | test  |
    And that any request will be successful if its HTTP code is less than 300
    And a GET call to the service '/users'
    When 1 thread is executed
    Then the number of samples is 1
    And the number of errors is 0

    Examples:
      | authentication                                   | info        |
      | the oauth authentication credentials 'abc':'123' | credentials |
      | the oauth authentication client credentials      | client      |


  Scenario: without authentication
    Given the service does not use authentication
    And that any request will be successful if its HTTP code is equal to 204
    And a GET call to the service '/users'
    When 1 thread is executed
    Then the number of samples is 1
    And the number of errors is 1


  Scenario: form
    Given that any request will be successful if its HTTP code is less than 300
    And the base URL http://localhost:8888/
    And a POST call to the service '/token'
    * with the following form parameters:
      | name     | value |
      | clientId | WEB   |
    * with the form parameter 'clientSecret' with value 's3cr3t'
    * with the attached file '${data.dir}/data/token.json' named 'file'
    When 1 thread is executed
    Then the number of samples is 1
    And the number of errors is 0


  Scenario Outline: csv
    Given the data in file '${data.dir}/data/users.csv'
    And that any request will be successful if its HTTP code is less than 300
    And a GET call to the service '/users/{userId}'
    When 1 thread is executed in <test>
    Then the number of samples is 3
    And the number of errors is 0

    Examples:
      | test                           |
      | 1 second holding 1 second      |
      | 1 second 1 time                |
      | 1 second every 1 second 1 time |


    Scenario: csv stretches
      Given the data in file '${data.dir}/data/users.csv'
      And that any request will be successful if its HTTP code is less than 300
      And a GET call to the service '/users/{userId}'
      When a test is executed with the following stretches:
        | threads | ramp     | hold     |
        | 1       | 1 second | 1 second |
      Then the number of samples is 3
      And the number of errors is 0
