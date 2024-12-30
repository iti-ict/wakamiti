@launcher
Feature: Testing database steps


  Scenario: Test 1
    * On completion, the table client is not empty
    * On completion, the user identified by '1' exists in the table client
    * On completion, the user identified by '4' does not exist in the table client
    * On completion, the user having birth_date = '2000-10-30' exists in the table client
    * On completion, the user having birth_date = '1982-12-30' does not exist in the table client
    * On completion, the user satisfying the following SQL clause exists in the table client:
      """
      BIRTH_DATE < date '2000-01-01'
      """
    * On completion, the following record exists in the table client:
      | id | first_name | second_name | active | birth_date | creation                |
      | 1  | John       | Smith       | 1      | 2000-10-30 | 2024-07-22              |
    * On completion, the table client is cleared
    * On completion, the table city is cleared
    * On completion, the user satisfying the following SQL clause does not exist in the table client:
      """
      BIRTH_DATE < date '2000-01-01'
      """
    * On completion, the following record does not exist in the table client:
      | id | first_name | second_name | active | birth_date | creation                |
      | 1  | John       | Smith       | 1      | 2000-10-30 | 2024-07-22              |
    When the following users are inserted into the database table client:
      | id | first_name | second_name | active | birth_date | creation                |
      | 1  | John       | Smith       | 1      | 2000-10-30 | 2024-07-22              |
      | 2  | Annie      | Hall        | 0      | 2011-09-12 | 2024-07-22 12:34:56     |
      | 3  | Bruce      | <null>      | 1      | 1982-12-31 | 2024-07-22 12:34:56.000 |
    Then the database table client is not empty
    And the number of users having active = '1' in the database table client is 2
    And the number of users satisfying the following SQL clause in the table client is 2:
      """
      birth_date > date '2000-01-01'
      """
    And the database table city is empty
    And the following record exists in the table client:
      | first_name | second_name | birth_date | creation                |
      | John       | Smith       | 2000-10-30 | 2024-07-22              |


  Scenario: Test 2
    * On completion, the SQL script file '${data.dir}/db/clean.sql' is executed
    * On completion, the number of users satisfying the following SQL clause in the table client is 0:
      """
      1=1
      """
    * On completion, the number of users satisfying the following in the table client is 0:
      | id | first_name | second_name | active | birth_date | creation                |
      | 1  | John       | Smith       | 1      | 2000-10-30 | 2024-07-22              |
    When the SQL script file '${data.dir}/db/dml.sql' is executed
    And the SQL script file '${data.dir}/db/procedure-mysql.sql' is executed
    Then the following record does not exist in the table client:
      | id | first_name | second_name | active  | birth_date |
      | 1  | Rosa       | Melano      | 1       | <null>     |
    But the following records exist in the table client:
      | id | first_name | second_name | active | birth_date | creation            |
      | 1  | Rosa       | Melano      | 0      | 1980-12-25 | 2024-07-22 12:34:56 |
      | 2  | Ester      | Colero      | 0      | 2000-01-02 | <null>              |
    And the following records exist in the table city:
      | id | name     | latitude  | longitude |
      | 1  | Valencia | 39.469906 | -0.376288 |
      | 2  | Madrid   | 40.416775 | -3.703790 |
    And the following records exist in the table client_city:
      | CLIENTID | CITYID |
      | 1        | 1      |
      | 2        | 2      |
      | 2        | 1      |
    And the user identified by '1' exists in the table client


  Scenario: Test 3
    * On completion, the table client is not empty using 'db' connection
    * On completion, the table client is cleared using 'db' connection
    * On completion, the table client is empty using 'db' connection
    * On completion, the content of the XLS file '${data.dir}/data1.xlsx' is inserted into the database using 'db' connection
    * On completion, the content of the XLS file '${data.dir}/data1.xlsx' exists in the database using 'db' connection
    * On completion, the content of the XLS file '${data.dir}/data1.xlsx' is deleted from the database using 'db' connection
    * On completion, the content of the XLS file '${data.dir}/data1.xlsx' does not exist in the database using 'db' connection
    * On completion, the content of the CSV file '${data.dir}/data1.csv' is inserted into the table client using 'db' connection
    * On completion, the content of the CSV file '${data.dir}/data1.csv' exists in the table client using 'db' connection
    * On completion, the content of the CSV file '${data.dir}/data1.csv' is deleted from the table client using 'db' connection
    * On completion, the content of the CSV file '${data.dir}/data1.csv' does not exist in the table client using 'db' connection
    * On completion, the following users are inserted into the table client using 'db' connection:
      | id | first_name | second_name | active | birth_date | creation                |
      | 1  | John       | Smith       | 1      | 2000-10-30 | 2024-07-22              |
      | 2  | Annie      | Hall        | 0      | 2011-09-12 | 2024-07-22 12:34:56     |
      | 3  | Bruce      | <null>      | 1      | 1982-12-31 | 2024-07-22 12:34:56.000 |
    * On completion, the following record exists in the table client using 'db' connection:
      | id | first_name | second_name | active | birth_date | creation                |
      | 1  | John       | Smith       | 1      | 2000-10-30 | 2024-07-22              |
    * On completion, the following user is deleted from the table client using 'db' connection:
      | id | first_name | second_name | active | birth_date | creation                |
      | 1  | John       | Smith       | 1      | 2000-10-30 | 2024-07-22              |
    * On completion, the following record does not exist in the table client using 'db' connection:
      | id | first_name | second_name | active | birth_date | creation                |
      | 1  | John       | Smith       | 1      | 2000-10-30 | 2024-07-22              |
    * On completion, the user identified by '2' exists in the table client using 'db' connection
    * On completion, the user identified by '1' does not exist in the table client using 'db' connection
    * On completion, the user having first_name = 'Annie' is deleted from the table client using 'db' connection
    * On completion, the user having first_name = 'Annie' does not exist in the table client using 'db' connection
    * On completion, the user having second_name = '<null>' exists in the table client using 'db' connection
    * On completion, the user satisfying the following SQL clause exists in the table client using 'db' connection:
      """
      birth_date < date '2000-01-01'
      """
    * On completion, the user satisfying the following SQL clause is deleted from the table client using 'db' connection:
      """
      birth_date < date '2000-01-01'
      """
    * On completion, the user satisfying the following SQL clause does not exist in the table client using 'db' connection:
      """
      birth_date < date '2000-01-01'
      """
    * On completion, the following SQL script is executed using 'db' connection:
      """
      DELETE FROM client_city;
      DELETE FROM city;
      DELETE FROM client;
      DELETE FROM other;
      """
    * On completion, the number of users satisfying the following SQL clause in the table client is 0 using 'db' connection:
      """
      1=1
      """
    Given the database connection URL '${database.connection.url}' using the user '${database.connection.username}' and the password '${database.connection.password}' as 'db'
    And the 'db' connection is used
    When the content of the CSV file '${data.dir}/data1.csv' is inserted into the table client
    And the following SQL script is executed:
      """
        UPDATE client SET active = 0 WHERE second_name = 'Melano';
      """
    And the following SQL query value:
      """
      SELECT * FROM client WHERE second_name = 'Melano';
      """
    Then the user identified by '${-1#[0].id}' exists in the table client in 1 second
    And the user having active = '${-2#[0].active}' exists in the table client in 1 second
    And the user having active = '1' does not exist in the table client in 1 second
    And the number of users having active = '0' in the table client is 1 in 1 second
    And the user satisfying the following SQL clause exists in the table client in 1 second:
      """
      birth_date = date '1980-12-25'
      """
    And the user satisfying the following SQL clause does not exist in the table client in 1 second:
      """
      birth_date > date '2000-01-01'
      """
    And the number of users satisfying the following SQL clause in the table client is 1 in 1 second:
      """
      birth_date = date '1980-12-25'
      """
    And the following record exists in the table client in 1 second:
      | first_name | second_name | active | birth_date | creation            |
      | Rosa       | Melano      | 0      | 1980-12-25 | 2024-07-22 12:34:56 |
    And the following record does not exist in the table client in 1 second:
      | first_name | second_name | active | birth_date | creation            |
      | Rosa       | Melano      | 1      | 1980-12-25 | 2024-07-22 12:34:56 |
    And the number of records satisfying the following in the table client is 1 in 1 second:
      | first_name | second_name | active | birth_date | creation            |
      | Rosa       | Melano      | 0      | 1980-12-25 | 2024-07-22 12:34:56 |
    And the content of the CSV file '${data.dir}/data2.csv' exists in the table client in 1 second
    And the content of the CSV file '${data.dir}/data1.csv' does not exist in the table client in 1 second
    And the table city is empty in 1 second
    And the table client is not empty in 1 second


  Scenario: Test 4
    * On completion, the SQL script file '${data.dir}/db/clean.sql' is executed using 'db' connection
    * On completion, the content of the XLS file '${data.dir}/data1.xlsx' is inserted into the database
    * On completion, the content of the XLS file '${data.dir}/data1.xlsx' exists in the database
    * On completion, the content of the XLS file '${data.dir}/data1.xlsx' is deleted from the database
    * On completion, the content of the XLS file '${data.dir}/data1.xlsx' does not exist in the database
    * On completion, the content of the CSV file '${data.dir}/data1.csv' is inserted into the table client
    * On completion, the content of the CSV file '${data.dir}/data1.csv' exists in the table client
    * On completion, the content of the CSV file '${data.dir}/data1.csv' is deleted from the table client
    * On completion, the content of the CSV file '${data.dir}/data1.csv' does not exist in the table client
    * On completion, the following users are inserted into the table client:
      | id | first_name | second_name | active | birth_date | creation                |
      | 1  | John       | Smith       | 1      | 2000-10-30 | 2024-07-22              |
      | 2  | Annie      | Hall        | 0      | 2011-09-12 | 2024-07-22 12:34:56     |
      | 3  | Bruce      | <null>      | 1      | 1982-12-31 | 2024-07-22 12:34:56.000 |
    * On completion, the following user is deleted from the table client:
      | id | first_name | second_name | active | birth_date | creation                |
      | 1  | John       | Smith       | 1      | 2000-10-30 | 2024-07-22              |
    * On completion, the user having first_name = 'Annie' is deleted from the table client
    * On completion, the user satisfying the following SQL clause is deleted from the table client:
      """
      birth_date < date '2000-01-01'
      """
    Given the database connection URL '${database.connection.url}' using the user '${database.connection.username}' and the password '${database.connection.password}' as 'db'
    And the default connection is used
    When the content of the XLS file '${data.dir}/data1.xlsx' is inserted into the database
    Then the content of the XLS file '${data.dir}/data1.xlsx' exists in the database
    And the content of the XLS file '${data.dir}/data1.xlsx' exists in the database in 1 second
    And the following SQL query value:
      """
      SELECT * FROM client WHERE first_name = 'Rosa';
      """
    And the number of records having id = '${-1#[0].id}' in the table client is 1
    And the following records do not exist in the table client:
      | first_name | second_name | active | birth_date | creation            |
      | Rosa       | Melano      | 0      | 1980-12-25 | 2024-07-22 12:34:56 |
    And the number of records satisfying the following in the table client is 1:
      | first_name | second_name | active | birth_date | creation            |
      | Rosa       | Melano      | 1      | 1980-12-25 | 2024-07-22 12:34:56 |


  Scenario: Test 5
    * On completion, the following SQL script is executed:
      """
      DELETE FROM client_city;
      DELETE FROM city;
      DELETE FROM client;
      DELETE FROM other;
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
      active = 1
      """
    And the users satisfying the following SQL clause do not exist in the table client:
      """
      active = 0
      """


    Scenario: Test 6
      Given the content of the XLS file '${data.dir}/data1.xlsx' inserted into the database
      When the content of the XLS file '${data.dir}/data1.xlsx' is deleted from the database
      Then the content of the XLS file '${data.dir}/data1.xlsx' does not exist in the database
      And the content of the XLS file '${data.dir}/data1.xlsx' does not exist in the database in 1 second


  # database.enableCleanupUponCompletion: true
  Scenario: Cleanup of data with foreign key
    * On completion, the table client is empty
    * On completion, the table city is empty
    * On completion, the table client_city is empty
    Given the following user inserted into the database table client:
      | id | first_name | second_name | active | birth_date | creation            |
      | 1  | Rosa       | Melano      | 1      | 1980-12-25 | 2024-07-22 12:34:56 |
    And the following city is inserted into the database table city:
      | id | name     |
      | 1  | Valencia |
    And the following relationship is inserted into the database table client_city:
      | clientId | cityId |
      | 1        | 1      |
    When the table client_city is cleared
    And the following SQL script is executed:
      """
      DELETE FROM city WHERE id = 1;
      INSERT INTO city (id, name, latitude, longitude) VALUES (1, 'Valencia', 39.469906, -0.376288);
      UPDATE city SET latitude = 40.469906 WHERE id = 1;
      """
    And the content of the CSV file '${data.dir}/data1.csv' is deleted from the table client