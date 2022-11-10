Feature: REST Test Feature


  Background:
    Given the base URL http://localhost:8888
    And the REST service '/users'
    And the REST content type JSON


  Scenario: Get a user from a service
    Given a user identified by 'user1'
    When the user is requested
    Then the response HTTP code is 200
    And the response HTTP code is not 201
    And the response content type is JSON
    And the response is equal to the file 'src/test/resources/server/users/user1.json'
    And the response contains:
      """
{ "name": "User One" }
      """
    And the text from response fragment 'contact.email' is 'user1@mail'
    And the text from response fragment 'contact.email' is not 'user2@mail'
    And the response satisfies the following schema:
      """json
{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "type": "object",
  "properties": {
    "id": {
      "type": "string",
      "pattern": "[a-zA-Z0-9]+"
    },
    "name": {
      "type": "string"
    },
    "age": {
      "type": "integer",
      "minimum": 5
    },
    "vegetables": {
      "type": "array",
      "items": [
        {
          "type": "object",
          "properties": {
            "id": {
              "type": "integer"
            },
            "description": {
              "type": "string"
            }
          },
          "required": [
            "id",
            "description"
          ]
        }
      ]
    },
    "contact": {
      "type": "object",
      "properties": {
        "email": {
          "type": "string",
          "pattern": "^[a-zA-Z0-9]+@[a-zA-Z0-9\\.]+$"
        }
      }
    }
  }
}
      """


  Scenario: Obtain complex JSON in any order
    When the users are queried
    Then the response is (in any order):
      """json
      [
        {
          "id" : "user3",
          "name" : "User Three",
          "age" : 13,
          "vegetables": [],
          "contact" : {
            "email": "user3@mail"
          }
        },
        {
          "id" : "user1",
          "name" : "User One",
          "age" : 11,
          "vegetables": [
            { "id": 1, "description": "Cucumber" },
            { "id": 2, "description": "Gherkin" }
          ],
          "contact" : {
            "email": "user1@mail"
          }
        },
        {
          "id" : "user2",
          "name" : "User Two",
          "age" : 12,
          "vegetables": [
            { "id": 1, "description": "Cucumber" },
            { "id": 3, "description": "Pickle" }
          ],
          "contact" : {
            "email": "user2@mail"
          }
        }
      ]
      """


  Scenario: URL with parameters
    Given the REST service '/users/{user}/{subject}'
    And the following path parameters:
      | name    | value      |
      | user    | user1      |
      | subject | vegetables |
    When the subject is queried
    Then the response is:
      """json
      [
        { "id": 1, "description": "Cucumber" },
        { "id": 2, "description": "Gherkin" }
      ]
      """
