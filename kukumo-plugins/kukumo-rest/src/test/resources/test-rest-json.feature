# language: en
# modules: rest-steps
Feature: REST Test Feature

Background:
 Given the base URL http://localhost:8888
 And the REST service '/users'
 And the REST content type JSON

Scenario: Get a user from a service
 Given a user identified by 'user1' 
 When the user is requested
 Then the response HTTP code is greater than or equals to 200
 And the response HTTP code is less than 500
 And the response content type is JSON
 And the response is equal to the file 'src/test/resources/user1.json'
 And the response contains:
 """
  { "name": "User One" }
 """
 And the text from response fragment 'contact.email' is 'user1@mail'
 And the response satisfies the following schema:
 ```json
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
         "email": { "type": "string", "pattern": "^[a-zA-Z0-9]+@[a-zA-Z0-9\\.]+$" }
       }
     }
   }
 }
 ```