
@launcher
Feature: Jmeter test

  Scenario: Smoke test
    Given TEXT_XML as content type
    And the base URL ${jmeter.baseURL}
    And a timeout of 1 second
    And variable 'userId' with value 'user1'
    And a GET call to the service '/users/{userId}'
    When 1 thread is executed
    Then the number of samples is 1
    And the number of errors is 0


