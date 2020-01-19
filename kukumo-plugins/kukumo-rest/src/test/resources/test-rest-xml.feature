# language: en
# modules: rest-steps
Feature: REST Test Feature

Background:
 Given the base URL http://localhost:8888
 And the REST service '/users'
 And the REST content type XML

Scenario: Get a user from a service
 Given a user identified by 'user1'
 When the user is requested
 Then the response HTTP code is greater than or equals to 200
 And the response HTTP code is less than 500
 And the response content type is XML
 And the response is equal to the file 'src/test/resources/user1.xml'
 And the response contains:
 """
 <data>
  <name>User One</name>
 </data>
 """
 And the text from response fragment 'data.contact.email' is 'user1@mail'


