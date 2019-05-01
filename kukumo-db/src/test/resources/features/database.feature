Feature: Testing database steps

Background:
    Given the SQL script file 'src/test/resources/create-schema.sql' is executed
    And the database table user is cleared 
    And the database table user is empty


Scenario: Test 1
    When the following users are inserted in the database table user:
    | id | first_name | second_name | active | birth_date |
    | 1  | John       | Smith       | 1      | 2000-10-30 |
    | 2  | Annie      | Hall        | 0      | 2011-09-12 |
    | 3  | Bruce      | <null>      | 1      | 1982-12-31 |
    Then the database table user is not empty
    And the number of users in the database table user having column active = '1' is 2
    And the database table city is empty
    When the following cities are inserted in the database table city:
    | id | name      |
    |  1 | Valencia  |
    |  2 | Barcelona |

# database.enableCleanupUponCompletion: true
Scenario: Cleanup of data with foreign key
    Given that the following user is inserted in the database table user:
    | id | first_name | second_name | active | birth_date |
    | 1  | John       | Smith       | 1      | 2000-10-30 |
    And the following city is inserted in the database table city:
    | id | name      |
    |  1 | Valencia  |
    And the following relationship is inserted in the database table user_city:
    | user_id | city_id |
    |       1 |       1 |
    