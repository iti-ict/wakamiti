#modules: kukumo-database, kukumo-rest
Feature: User service operations

   Background:
     Given the REST service '/users'

   Scenario: An existing user is requested
      Given a user identified by '3'
      And the following data inserted in the database table USER:
        | ID | FIRST_NAME | LAST_NAME |
        | 3  | Samuel L.  | Jackson   |
      When the user is requested
      Then the response HTTP code is 200
      And bla bla bala bala
      And the response contains:
      """
      { "firstName": "Samuel L." }
      """

   Scenario: An existing user is requested (will fail)
      Given a user identified by '4'
      And the following data inserted in the database table USER:
        | ID | FIRST_NAME | LAST_NAME      |
        | 4  | Arnorld    | Chuarcheneguer |
      When the user is requested
      Then the response HTTP code is 200
      And the response contains:
      """
      { "lastName": "Schwarzenegger" }
      """