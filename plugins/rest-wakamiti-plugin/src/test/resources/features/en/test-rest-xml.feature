# language: en
@launcher
Feature: REST Test Feature

  Background:
    Given the REST service '/users'
    And the REST content type XML

  Scenario: Get a user from a service
    Given a user identified by 'user1'
    When the user is requested
    Then the response HTTP code is greater than or equal to 200
    And the response HTTP code is less than 500
    And the response content type is XML
    And the response is equal to the file '${data.dir}/server/users/user1.xml'
    And the response contains:
      """xml
      <item>
          <name>User One</name>
      </item>
      """
    And the text from response fragment 'item.contact.email' is 'user1@mail'
    And the response satisfies the schema from the file '${data.dir}/data/schema.xml'

  Scenario: URL with parameters
    Given the REST service '/users/{user}/{subject}'
    And the following path parameters:
      | name    | value      |
      | user    | user1      |
      | subject | vegetables |
    When the subject is queried
    Then the response is:
      """xml
      <ArrayList>
        <item>
            <id>1</id>
            <description>Cucumber</description>
        </item>
        <item>
            <id>2</id>
            <description>Gherkin</description>
        </item>
    </ArrayList>
      """
    And the text from response fragment 'ArrayList.item[1].id' is '2'
    And the text from response fragment '*.item[1].description' is not 'Cucumber'
    And the response fragment 'ArrayList.item[1]' is:
      """xml
      <item>
         <id>2</id>
         <description>Gherkin</description>
      </item>
      """
