Feature: Testing database steps


  Scenario: Test 1
    * At finish, the table client is cleared
    * At finish, the table CITY is cleared
    When the following users are inserted into the database table CLIENT:
      | ID | FIRST_NAME | SECOND_NAME | ACTIVE | BIRTH_DATE |
      | 1  | John       | Smith       | 1      | 2000-10-30 |
      | 2  | Annie      | Hall        | 0      | 2011-09-12 |
      | 3  | Bruce      | <null>      | 1      | 1982-12-31 |
    Then the database table client is not empty
    And the number of users having active = '1' in the database table client is 2
    And the number of users satisfying the following SQL clause in the table client is 2:
      """
      BIRTH_DATE > '2000-01-01'
      """
    And the database table city is empty
    And the following record exists in the table client:
      | FIRST_NAME | SECOND_NAME | BIRTH_DATE |
      | John       | Smith       | 2000-10-30 |


  Scenario: Test 2
    * At finish, the SQL script file '${data.dir}/db/clean.sql' is executed
    When the SQL script file '${data.dir}/db/dml-postgres.sql' is executed
    And the SQL procedure file '${data.dir}/db/procedure-postgres.sql' is executed
    Then the following record does not exist in the table client:
      | ID             | FIRST_NAME             | SECOND_NAME             | ACTIVE             | BIRTH_DATE             |
      | ${3#[0][0].id} | ${3#[0][0].first_name} | ${3#[0][0].second_name} | ${3#[0][0].active} | ${3#[0][0].birth_date} |
    But the following records exist in the table client:
      | ID | FIRST_NAME | SECOND_NAME | ACTIVE | BIRTH_DATE |
      | 1  | Rosa       | Melano      | 0      | <null>     |
      | 2  | Ester      | Colero      | 0      | 2000-01-02 |
    And the following records exist in the table city:
      | ID | NAME     | LATITUDE  | LONGITUDE |
      | 1  | Valencia | 39.469906 | -0.376288 |
      | 2  | Madrid   | 40.416775 | -3.703790 |
    And the following records exist in the table client_city:
      | CLIENTID | CITYID |
      | 1        | 1      |
      | 2        | 2      |
      | 2        | 1      |
    And the user identified by '1' exists in the table client


  Scenario: Test 3
    * At finish, the table client is cleared using 'db' connection
    * At finish, the content of the XLS file '${data.dir}/data1.xlsx' is inserted into the database using 'db' connection
    * At finish, the content of the XLS file '${data.dir}/data1.xlsx' is deleted from the database using 'db' connection
    * At finish, the content of the CSV file '${data.dir}/data1.csv' is inserted into the table client using 'db' connection
    * At finish, the content of the CSV file '${data.dir}/data1.csv' is deleted from the table client using 'db' connection
    * At finish, the following users are inserted into the table client using 'db' connection:
      | id | first_name | second_name | active | birth_date |
      | 1  | John       | Smith       | 1      | 2000-10-30 |
      | 2  | Annie      | Hall        | 0      | 2011-09-12 |
      | 3  | Bruce      | <null>      | 1      | 1982-12-31 |
    * At finish, the following user is deleted from the table client using 'db' connection:
      | id | first_name | second_name | active | birth_date |
      | 1  | John       | Smith       | 1      | 2000-10-30 |
    * At finish, the user having first_name = 'Annie' is deleted from the table client using 'db' connection
    * At finish, the user satisfying the following SQL clause is deleted from the table client using 'db' connection:
      """
      BIRTH_DATE < '2000-01-01'
      """
    * At finish, the following SQL script is executed using 'db' connection:
      """
      DELETE FROM CLIENT_CITY;
      DELETE FROM CITY;
      DELETE FROM CLIENT;
      DELETE FROM OTHER;
      """
    Given the database connection URL '${database.connection.url}' using the user '${database.connection.username}' and the password '${database.connection.password}' as 'db'
    And the 'db' connection is used
    When the content of the CSV file '${data.dir}/data1.csv' is inserted into the table client
    And the following SQL procedure is executed:
      """
      UPDATE CLIENT SET ACTIVE = 0 WHERE ID = 1;
      SELECT * FROM CLIENT WHERE ID = 1;
      """
    Then the user identified by '${-1#[0][0].id}' exists in the table client in 1 second
    And the user having active = '${-2#[0][0].active}' exists in the table client in 1 second
    And the user having active = '1' does not exist in the table client in 1 second
    And the number of users having active = '0' in the table client is 1 in 1 second
    And the user satisfying the following SQL clause exists in the table client in 1 second:
      """
      BIRTH_DATE = '1980-12-25'
      """
    And the user satisfying the following SQL clause does not exist in the table client in 1 second:
      """
      BIRTH_DATE > '2000-01-01'
      """
    And the number of users satisfying the following SQL clause in the table client is 1 in 1 second:
      """
      BIRTH_DATE = '1980-12-25'
      """
    And the following record exists in the table client in 1 second:
      | id | first_name | second_name | active | birth_date |
      | 1  | Rosa       | Melano      | 0      | 1980-12-25 |
    And the following record does not exist in the table client in 1 second:
      | id | first_name | second_name | active | birth_date |
      | 1  | Rosa       | Melano      | 1      | 1980-12-25 |
    And the number of records satisfying the following in the table client is 1 in 1 second:
      | id | first_name | second_name | active | birth_date |
      | 1  | Rosa       | Melano      | 0      | 1980-12-25 |
    And the content of the CSV file '${data.dir}/data2.csv' exists in the table client in 1 second
    And the content of the CSV file '${data.dir}/data1.csv' does not exist in the table client in 1 second
    And the table city is empty in 1 second
    And the table client is not empty in 1 second


  Scenario: Test 4
    * At finish, the SQL script file '${data.dir}/db/clean.sql' is executed using 'db' connection
    * At finish, the content of the XLS file '${data.dir}/data1.xlsx' is inserted into the database
    * At finish, the content of the XLS file '${data.dir}/data1.xlsx' is deleted from the database
    * At finish, the content of the CSV file '${data.dir}/data1.csv' is inserted into the table client
    * At finish, the content of the CSV file '${data.dir}/data1.csv' is deleted from the table client
    * At finish, the following users are inserted into the table client:
      | id | first_name | second_name | active | birth_date |
      | 1  | John       | Smith       | 1      | 2000-10-30 |
      | 2  | Annie      | Hall        | 0      | 2011-09-12 |
      | 3  | Bruce      | <null>      | 1      | 1982-12-31 |
    * At finish, the following user is deleted from the table client:
      | id | first_name | second_name | active | birth_date |
      | 1  | John       | Smith       | 1      | 2000-10-30 |
    * At finish, the user having first_name = 'Annie' is deleted from the table client
    * At finish, the user satisfying the following SQL clause is deleted from the table client:
      """
      BIRTH_DATE < '2000-01-01'
      """
    Given the database connection URL '${database.connection.url}' using the user '${database.connection.username}' and the password '${database.connection.password}' as 'db'
    And the default connection is used
    When the content of the XLS file '${data.dir}/data1.xlsx' is inserted into the database
    Then the content of the XLS file '${data.dir}/data1.xlsx' exists in the database
    And the content of the XLS file '${data.dir}/data1.xlsx' exists in the database in 1 second
    And the following SQL query value:
      """
      SELECT * FROM CLIENT WHERE FIRST_NAME = 'Rosa';
      """
    And the number of records having id = '${-1#[0].id}' in the table client is 1
    And the following records do not exist in the table client:
      | first_name | second_name | active | birth_date |
      | Rosa       | Melano      | 0      | 1980-12-25 |
    And the number of records satisfying the following in the table client is 1:
      | first_name | second_name | active | birth_date |
      | Rosa       | Melano      | 1      | 1980-12-25 |


  Scenario: Test 5
    * At finish, the following SQL script is executed:
      """
      DELETE FROM CLIENT_CITY;
      DELETE FROM CITY;
      DELETE FROM CLIENT;
      DELETE FROM OTHER;
      """
    Given the content of the XLS file '${data.dir}/data1.xlsx' inserted into the database
    And the SQL query value from the file '${data.dir}/db/select.sql'
    When the table client_city is cleared
    And the users having name = 'Valencia' are deleted from the table city
    Then the content of the CSV file '${data.dir}/data1.csv' exists in the table client
    And the content of the CSV file '${data.dir}/data2.csv' does not exist in the table client
    And the city identified by '1' does not exist in the table city
    And the city having name = 'Valencia' does not exist in the table city
    And the number of records satisfying the following SQL clause in the table client is 1:
      """
      ACTIVE = 1
      """
    And the users satisfying the following SQL clause do not exist in the table client:
      """
      ACTIVE = 0
      """


    Scenario: Test 6
      Given the content of the XLS file '${data.dir}/data1.xlsx' inserted into the database
      When the content of the XLS file '${data.dir}/data1.xlsx' is deleted from the database
      Then the content of the XLS file '${data.dir}/data1.xlsx' does not exist in the database
      And the content of the XLS file '${data.dir}/data1.xlsx' does not exist in the database in 1 second


  # database.enableCleanupUponCompletion: true
  Scenario: Cleanup of data with foreign key
    * At finish, the table client is empty
    * At finish, the table city is empty
    * At finish, the table client_city is empty
    Given the following user inserted into the database table client:
      | id | first_name | second_name | active | birth_date |
      | 1  | Rosa       | Melano      | 1      | 1980-12-25 |
    And the following city is inserted into the database table city:
      | id | name     |
      | 1  | Valencia |
    And the following relationship is inserted into the database table client_city:
      | clientId | cityId |
      | 1        | 1      |
    When the table client_city is cleared
    And the following SQL script is executed:
      """
      DELETE FROM CITY WHERE id = 1;
      INSERT INTO city (ID, name, latitude, longitude) VALUES (1, 'Valencia', 39.469906, -0.376288);
      UPDATE city SET latitude = 40.469906 WHERE id = 1;
      """
    And the content of the CSV file '${data.dir}/data1.csv' is deleted from the table client
