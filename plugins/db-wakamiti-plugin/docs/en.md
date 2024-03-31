

This plugin offers a set of steps for interacting with a JDBC database, simplifying the process of data loading and 
validation.

> **KEEP IN MIND**
>
> Regarding the multiple database implementations existing, this plugin does not include any specific driver. In order
> to work properly, remember to include the module with the JDBC driver(s) that your database connection would
> require.

<br />


## Install

---

Include the module and the necessary JDBC driver(s) in the corresponding section.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:db-wakamiti-plugin:3.0.0
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>db-wakamiti-plugin</artifactId>
  <version>3.0.0</version>
</dependency>
```

<br />


## Options

---


### `database.connection.url`
- Type: `string` *required*

Set the default JDBC connection URL to the database. The URL format will determine the driver used to access the 
database.

Example:
```yaml
database:
  connection:
    url: jdbc:h2:tcp://localhost:9092/~/test
```

<br />

---


### `database.connection.username`
- Type: `string` *required*

Set the default JDBC connection username.

Example:
```yaml
database:
  connection:
    username: test
```

<br />

---


### `database.connection.password`
- Type: `string` *required*

Set the default JDBC connection password.

Example:
```yaml
database:
  connection:
    password: test
```

<br />

---


### `database.metadata.schema`
- Type: `string`

Set the default schema that should be used to retrieve metadata as primary keys and nullability. If not specified, the 
default schema returned by the connection will be used.

Example:
```yaml
database:
  metadata:
    schema: TESTDB
```

<br />

---


### `database.metadata.catalog`
- Type: `string`

Set the default catalog that should be used to retrieve metadata as primary keys and nullability. If not specified, the 
default catalog returned by the connection will be used (in case the database system uses one).

Example:
```yaml
database:
  metadata:
    catalog: TESTCAT
```

<br />

---


### `database.{alias}...`
 
Set the JDBC connection parameters and/or metadata of a database identified by an alias. You can establish as many named 
connections as you want. The first database will be taken as the default configuration.

Example:
```yaml
database:
  db1:
    connection:
      url: jdbc:h2:tcp://localhost:9092/~/test
      username: test1
      password: test1
    metadata:
      schema: TESTDB1
      catalog: TESTCAT1
  db2:
    connection:
      url: jdbc:mysql://other.host:3306/test
      username: test2
      password: test2
    metadata:
      schema: TESTDB2
      catalog: TESTCAT2
```

<br />

---


### `database.csv.format`
- Type: `string`
- Default `DEFAULT`

Set the format used to parse CSV files. The accepted values are directly imported from the [Commons CSV][1] project,
check it for detailed explanation of each format. Possible values are:
- `DEFAULT`
- `INFORMIX_UNLOAD`
- `INFORMIX_UNLOAD_CSV`
- `MYSQL`
- `ORACLE`
- `POSTGRESQL_CSV`
- `POSTGRESQL_TEXT`
- `RFC4180`

Example:
```yaml
database:
  csv:
    format: ORACLE
```

<br />

---


### `database.xls.ignoreSheetPattern`
- Type: `regex`
- Default `#.*`

Set the regex pattern used to determine what sheets to be ignored when loading XLS files.

Example:
```yaml
database:
  xls:
    ignoreSheetPattern: //.*
```

<br />

---


### `database.nullSymbol`
- Type: `string`
- Default `<null>`

Set literal used to handle a specific cell value as the SQL `NULL` element. It is used in any data source like CSV, XLS,
and in-line tables.

Example:
```yaml
database:
  nullSymbol: (null)
```

<br />

---


### `database.enableCleanupUponCompletion`
- Type: `boolean`
- Default `false`

The default behavior of the plugin does not perform any database cleanup operation after the tests are finished. 
This is to be able to check results manually and debug errors.
Possible values are:
- `false`: no cleanup action will be performed.
- `true`: the database will be forced to be cleaned by undoing the test data entered during the execution.

Example:
```yaml
database:
  enableCleanupUponCompletion: "true"
```

<br />


## Usage

---

This plugin provides the following steps:

### Define connection 
```text copy=true
the database connection URL {url} using the user {username} and the password {password} (as {alias})
```

Configure the connection parameters to the database identified by the specified alias.
If not alias is included, it will be set as the default connection.

This step is the declarative equivalent to set the configuration properties 
[`database.connection.url`](#databaseconnectionurl), [`database.connection.username`](#databaseconnectionusername),
[`database.connection.password`](#databaseconnectionpassword) or [`database.{alias}...`](#databasealias).


#### Parameters:
| Name       | Wakamiti type     | Description          |
|------------|-------------------|----------------------|
| `url`      | `text` *required* | The URL connection   |
| `username` | `text` *required* | User name            |
| `password` | `text` *required* | User password        |
| `alias`    | `text`            | The connection alias |

#### Examples:
```gherkin
Given the database connection URL 'jdbc:h2:tcp://localhost:9092/~/test' using the user 'test' and the password 'test'
```
```gherkin
Given the database connection URL 'jdbc:mysql://other.host:3306/test' using the user 'test' and the password 'test' as 'db1'
```

<br />

---


### Switch connection
```text copy=true
the (default|{alias}) connection is used
```

Switch the active database connection to the one specified, or to the default.

#### Parameters:
| Name       | Wakamiti type | Description          |
|------------|---------------|----------------------|
| `alias`    | `text`        | The connection alias |

#### Examples:
```gherkin
When the default connection is used
```
```gherkin
When the 'db1' connection is used
```

<br />

---


### Execute script
```text copy=true
the following SQL (script|procedure) is executed:
   {script}
```
- [Post-execution mode][3]

Execute the specified SQL statements or procedure and retrieve the inserted, updated or selected data as a JSON object.


#### Parameters:
| Name     | Wakamiti type         | Description    |
|----------|-----------------------|----------------|
| `script` | `document` *required* | Script content |

#### Examples:
```gherkin
When the following SQL script is executed:
  """sql
  INSERT INTO users (id, first_name) VALUES (1, 'Rosa');
  INSERT INTO users (id, first_name) VALUES (2, 'Pepe');
  """
```

It could return the following result:
```json
[
  {
    "id": 1, 
    "first_name": "Rosa"
  },
  {
    "id": 2,
    "first_name": "Pepe"
  }
]
```

<br />

---


### Execute script (file)
```text copy=true
the SQL (script|procedure) file {script} is executed
```
- [Post-execution mode][3]

Execute the specified SQL statements or procedure and retrieve the inserted, updated or selected data as a JSON object.

#### Parameters:
| Name     | Wakamiti type     | Description    |
|----------|-------------------|----------------|
| `script` | `file` *required* | Script content |

#### Examples:
```gherkin
When the SQL script file 'data/script.sql' is executed
```

<br />

---


### Select data
```text copy=true
the following SQL query value(s):
   {script}
```

Retrieve data from the specified SQL SELECT as a JSON object. 

#### Parameters:
| Name     | Wakamiti type         | Description    |
|----------|-----------------------|----------------|
| `script` | `document` *required* | Script content |

#### Examples:
```gherkin
Given the following SQL query values:
  """sql
  SELECT id, first_name FROM users
  """
```

It could return the following result:
```json
[
  {
    "id": 1, 
    "first_name": "Rosa"
  },
  {
    "id": 2,
    "first_name": "Pepe"
  }
]
```

<br />

---


### Select data (file)
```text copy=true
the SQL query value(s) from the file {sql}
```

Retrieve data from the specified SQL SELECT as a JSON object.

#### Parameters:
| Name  | Wakamiti type     | Description    |
|-------|-------------------|----------------|
| `sql` | `file` *required* | Script content |

#### Examples:
```gherkin
Given the SQL query values from the file 'data/select.sql'
```

<br />

---


### Insert data
```text copy=true
the following * (is|are) inserted into the (database) table {table}:
    {data}
```
- [Post-execution mode][3]

Insert rows from the provided DataTable into the specified table and retrieve the inserted data as a JSON object.

#### Parameters:
| Name    | Wakamiti type      | Description    |
|---------|--------------------|----------------|
| `table` | `word` *required*  | The table name |
| `data`  | `table` *required* | The table data |

#### Examples:
```gherkin
When the following users are inserted into the table USER:
    | USER  | STATE | BLOCKING_DATE |   
    | user1 | 2     | <null>        |
    | user2 | 3     | 2020-02-13    |
```

It could return the following result:
```json
[
  {
    "USER": "user1", 
    "STATE": "2",
    "BLOCKING_DATE": null
  },
  {
    "USER": "user2",
    "STATE": "3",
    "BLOCKING_DATE": "2020-02-13"
  }
]
```

<br />

---


### Insert data (file)
```text copy=true
the content of the XLS file {file} (is) inserted into the database
```
```text copy=true
the content of the CSV file {file} (is) inserted into the (database) table {table}
```
- [Post-execution mode][3]

Insert rows of the provided XLS or CSV file into the database and retrieve the inserted data as a JSON object. If it is 
an XLS file, each sheet will represent a table and should be named as such. If it is a CSV file, the name of the table 
where the data shall be inserted shall be indicated.

#### Parameters:
| Name    | Wakamiti type     | Description    |
|---------|-------------------|----------------|
| `file`  | `file` *required* | The file name  |
| `table` | `word`            | The table name |

#### Examples:
```gherkin
When the content of the XLS file 'data/users.xls' is inserted into the database
``` 
```gherkin
When the content of the CSV file 'data/users.csv' is inserted into the table USER
```

<br />

---


### Delete data
```text copy=true
the following * (is|are) deleted from the (database) table {table}:
    {data}
```
- [Post-execution mode][3]

Delete the rows in the given table that match the specified values.

#### Parameters:
| Name    | Wakamiti type      | Description    |
|---------|--------------------|----------------|
| `table` | `word` *required*  | The table name |
| `data`  | `table` *required* | The table data |

#### Examples:
```gherkin
When the following users are deleted from the table USER:
    | USER  | STATE | BLOCKING_DATE |   
    | user1 | 2     | <null>        |
    | user2 | 3     | 2020-02-13    |
```

<br />

---


### Delete data (column)
```text copy=true
* having {column} = {value} (is|are) deleted from the (database) table {table}
```
- [Post-execution mode][3]

Delete the rows in the given table that match the specified value.

#### Parameters:
| Name     | Wakamiti type     | Description    |
|----------|-------------------|----------------|
| `column` | `word` *required* | Column name    |
| `value`  | `text` *required* | Column value   |
| `table`  | `word` *required* | The table name |

#### Examples:
```gherkin
When users having STATE = '2' are deleted from the table USER 
```

<br />

---


### Delete data (where)
```text copy=true
* satisfying the following SQL clause (is|are) deleted from the (database) table {table}:
    {where}
```
- [Post-execution mode][3]

Deletes rows from the specified database table based on the provided SQL WHERE clause.

#### Parameters:
| Name    | Wakamiti type         | Description      |
|---------|-----------------------|------------------|
| `table` | `word` *required*     | The table name   |
| `where` | `document` *required* | The where clause |

#### Examples:
```gherkin
When the user satisfying the following SQL clause is deleted from the table client:
    """
    birth_date < date '2000-01-01'
    """
```

<br />

---


### Delete data (file)
```text copy=true
the content of the XLS file {file} is deleted from the database
```
```text copy=true
the content of the CSV file {file} is deleted from the database table {table}
```
- [Post-execution mode][3]

Delete rows of the provided XLS or CSV file into the database. If it is an XLS file, each sheet will represent a table
and should be named as such. If it is a CSV file, the name of the table where the data shall be deleted shall be
indicated.

#### Parameters:
| Name    | Wakamiti type     | Description    |
|---------|-------------------|----------------|
| `file`  | `file` *required* | The file name  |
| `table` | `word`            | The table name |

#### Examples:
```gherkin
When the content of the XLS file 'data/users.xls' is deleted from the database
```
```gherkin
When the content of the CSV file 'data/users.csv' is deleted from the database table USER
```

<br />

---


### Clear table
```text copy=true
the (database) table {word} is cleared
```
- [Post-execution mode][3]

Delete all rows from the given database table.

#### Parameters:
| Name   | Wakamiti type     | Description    |
|--------|-------------------|----------------|
| `word` | `word` *required* | The table name |

#### Examples:
```gherkin
When the table USER is cleared
```

<br />

---


### Check data existence 
```text copy=true
the following record(s) (not) exist(s) in the (database) table {table}:
    {data}
```
- [Post-execution mode][3]
- [Async mode][4]

Assert that the data provided in the DataTable exists or not in the given table.

#### Parameters:
| Name    | Wakamiti type      | Description    |
|---------|--------------------|----------------|
| `table` | `word` *required*  | The table name |
| `data`  | `table` *required* | Data table     |

#### Examples:
```gherkin
Then the following user exists in the table USER:
    | USER  | STATE | BLOCKING_DATE |
    | user2 | 3     | 2020-02-13    |
```
```gherkin
Then the following users not exist in the table USER:
    | USER  | STATE | BLOCKING_DATE |
    | user1 | 2     | <null>        |
    | user2 | 3     | 2020-02-13    |
```

<br />

---


### Check data existence (id)
```text copy=true
* identified by {id} (not) exist(s) in the (database) table {table}
```
- [Post-execution mode][3]
- [Async mode][4]

Assert that a row in the given table has or not a primary key matching the specified value. The table must have a 
single-column primary key accessible from the database metadata.

#### Parameters:
| Name    | Wakamiti type     | Description       |
|---------|-------------------|-------------------|
| `id`    | `text` *required* | Primary key value |
| `table` | `word` *required* | The table name    |

#### Examples:
```gherkin
Then a user identified by 'user1' exists in the table USER
```
```gherkin
Then a user identified by 'user1' not exist in the table USER
```

<br />

---


### Check data existence (column)
```text copy=true
* having {column} = {value} (not) exist(s) in the (database) table {table}
```
- [Post-execution mode][3]
- [Async mode][4]

Assert that rows with the specified value in the given column exist or not in the given table.

#### Parameters:
| Name     | Wakamiti type     | Description    |
|----------|-------------------|----------------|
| `column` | `word` *required* | Column name    |
| `value`  | `text` *required* | Column value   |
| `table`  | `word` *required* | The table name |

#### Examples:
```gherkin
Then a user having STATE = '1' exists in the table USER
```
```gherkin
Then users having STATE = '1' not exist in the table USER
```

<br />

---


### Check data existence (where)
```text copy=true
* satisfying the following SQL clause (not) exist(s) in the (database) table {table}:
    {where}
```
- [Post-execution mode][3]
- [Async mode][4]

Assert that rows with the specified values in the given clause exist or not in the table.

#### Parameters:
| Name    | Wakamiti type         | Description    |
|---------|-----------------------|----------------|
| `table` | `word` *required*     | The table name |
| `where` | `document` *required* | Where clause   |

#### Examples:
```gherkin
Then a user satisfying the following SQL clause exists in the database table USER:
    """
    STATE IN (2,3) OR BLOCKING_DATE IS NULL
    """
```
```gherkin
Then users satisfying the following SQL clause not exist in the database table USER:
    """
    STATE IN (2,3) OR BLOCKING_DATE IS NULL
    """
```

<br />

---


### Check data existence (file)
```text copy=true
the content of the XLS file {file} (not) exist(s) in the database
```
```text copy=true
the content of the CSV file {file} (not) exist(s) in the (database) table {table}
```
- [Post-execution mode][3]
- [Async mode][4]

Assert that rows of the provided XLS or CSV file exist or not in the database. If it is an XLS file, each sheet will 
represent a table and should be named as such. If it is a CSV file, the name of the table where the data shall be 
matched shall be indicated.

#### Parameters:
| Name    | Wakamiti type     | Description    |
|---------|-------------------|----------------|
| `file`  | `file` *required* | The file name  |
| `table` | `word`            | The table name |

#### Examples:
```gherkin
Then the content of the XLS file 'data/users.xls' exists in the database
```
```gherkin
Then the content of the CSV file 'data/users.csv' not exist in the table USERS
```

<br />

---


### Check data count
```text copy=true
the number of * satisfying the following (info) in the (database) table {table} {matcher}:
    {data}
```
- [Post-execution mode][3]
- [Async mode][4]

Asserts that the number of rows in the given table matching the specified values for every column satisfies a numeric
expression. 

#### Parameters:
| Name      | Wakamiti type                | Description             |
|-----------|------------------------------|-------------------------|
| `table`   | `word` *required*            | The table name          |
| `matcher` | `long-assertion` *required*  | Numeric [comparator][2] |
| `data`    | `table` *required*           | Data table              |

#### Examples:
```gherkin
Then the number of users satisfying the following in the table USER is 0:
    | USER  | STATE | BLOCKING_DATE |
    | user1 | 2     | <null>        |
```
```gherkin
Then the number of records satisfying the following info in the table USER is more than 0:
    | USER  | STATE | BLOCKING_DATE |
    | user1 | 2     | <null>        |
```

<br />

---


### Check data count (column)
```text copy=true
the number of * having {column} = {value} in the (database) table {table} {matcher}
```
- [Post-execution mode][3]
- [Async mode][4]

Assert that the number of rows satisfying the condition meets the given numerical comparison.

#### Parameters:
| Name      | Wakamiti type               | Description             |
|-----------|-----------------------------|-------------------------|
| `column`  | `word` *required*           | Column name             |
| `value`   | `text` *required*           | Column value            |
| `table`   | `word` *required*           | The table name          |
| `matcher` | `long-assertion` *required* | Numeric [comparator][2] |

#### Examples:
```gherkin
Given that the number of users having STATE = '1' in the database table USER is greater than 5
```

<br />

---


### Check data count (where)
```text copy=true
* satisfying the following SQL clause exist(s) in the (database) table {table}:
    {where}
```
- [Post-execution mode][3]
- [Async mode][4]

Asserts that the number of rows in the given table matching the SQL clause satisfies a numeric assertion.

#### Parameters:
| Name      | Wakamiti type               | Description             |
|-----------|-----------------------------|-------------------------|
| `table`   | `word` *required*           | The table name          |
| `matcher` | `long-assertion` *required* | Numeric [comparator][2] |
| `where`   | `document` *required*       | Where clause            |

#### Examples:
```gherkin
Then the number of users satisfying the following SQL clause in the table USER is less than 10:
    """
    STATE IN (2,3) OR BLOCKING_DATE IS NULL
    """
```

<br />

---


### Check table content
```text copy=true
the (database) table {table} is (not) empty
```

Assert that the given table is empty or not.

#### Parameters:
| Name    | Wakamiti type     | Description    |
|---------|-------------------|----------------|
| `table` | `word` *required* | The table name |

#### Examples:
```gherkin
Then the table USER is empty
```
```gherkin
Then the table USER is not empty
```

<br />


## Special modes

---

Some steps may be executed with a different behavior if they are defined in the following ways:

### Post-execution mode
```text copy=true
At finish, * (using {alias} connection)
```

The step shall be executed once the scenario has finished, regardless of the outcome of the execution.
If the alias is not included, the default connection will be used.

#### Parameters:
| Name    | Wakamiti type | Description          |
|---------|---------------|----------------------|
| `alias` | `text`        | The connection alias |

#### Examples:
```gherkin
* At finish, the SQL script file 'data/insert-users.sql' is executed
```
```gherkin
* At finish, the following SQL script is executed using 'db1' connection:
  """
  UPDATE AAAA SET STATE = 2 WHERE ID = 1;
  DELETE FROM BBBB WHERE ID = 2;
  """
```

<br />

---


### Async mode
```text copy=true
* in {time} seconds
```

The step waits for a maximum of the indicated seconds until the condition indicated in the step is fulfilled.

#### Parameters:
| Name   | Wakamiti type    | Description |
|--------|------------------|-------------|
| `time` | `int` *required* | The timeout |

#### Examples:
```gherkin
Then a user identified by '1' exists in the table USERS in 10 seconds
```

<br />


[1]: https://commons.apache.org/proper/commons-csv/
[2]: en/wakamiti/architecture#comparador
[3]: #postexecutionmode
[4]: #asyncmode
