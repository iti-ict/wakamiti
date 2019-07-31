# language: en
@report-test
Feature: REST Test Feature

Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna
aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.
Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.

Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.

Background:
 Given the host localhost:8888
 And the REST service '/users'
 And the REST content type JSON

@CP-1 @tag1
Scenario: Success test case
 Given a user identified by 'user2' 
 
@CP-2 @tag1 @tag2
Scenario: Get a user from a service
 Given a user identified by 'user1' 
 When the user is requested
 Then the response HTTP code is greater than or equals to 200
 And the response HTTP code is less than 500
 And the response content type is JSON
 And the response is equal to the file 'features/json1.json'
 And the response contains:
 """
  { "name": "User One" }
 """ 

@CP-3 @tag2
Scenario: This scenario use undefined steps
 Given the following data table with no logic whatsoever:
 | column one | column two | column three |
 | A | B | C |
 |Lorem ipsum dolor sit amet, consectetur adipiscing elit | 12313213 | XXXxx |
 | a | b | c |
 | 1 | 2 | 3 |


@CP-4 @tag1 @tag2
Scenario: Get a user from a service
 Given a user identified by 9999 
