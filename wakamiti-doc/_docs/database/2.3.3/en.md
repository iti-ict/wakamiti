---
title: DB steps
date: 2022-09-20
slug: /en/plugins/database/2.3.3
---



This plugin provides a set of steps that interact with a database via JDBC, easing the effort required to load and
assert data.

> **KEEP IN MIND**
>
> Regarding the multiple database implementations existing, this plugin does not include any specific driver. In order
> to work properly, remember to include the module with the JDBC driver(s) that your database connection would
> require.

<br />

---
## Tabla de contenido

---
<br />


## Install

---

Include the module and the necessary JDBC driver(s) in the corresponding section.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:db-wakamiti-plugin:2.3.3
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>db-wakamiti-plugin</artifactId>
  <version>2.3.3</version>
</dependency>
```

<br />


## Options

---

### `database.connection.url`
- Type: `string` *required*

Set the JDBC database connection URL. The URL format will determine the driver used to access the database.

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

Set the JDBC connection username.

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

Set the connection password.

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


### `database.metadata.caseSensititvy`
- Type: `string`
- Default `INSENSITIVE`

Set whether any upper/lower case transformation should be done when trying to access meta-data. Some engines are
very strict regarding this issue, and can cause unexpected errors if this property is not properly configured.
Possible values are:
- `INSENSITIVE`: Identifiers will be kept as they are written.
- `LOWER_CASED`: Identifiers will be converted to lowercase.
- `UPPER_CASED`: Identifiers will be converted to uppercase.

Example:
```yaml
database:
  metadata:
    caseSensitivity: UPPER_CASED
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

Sets regex pattern used to determine what sheets to be ignored when loading XLS files.

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

The default behaviour does not perform any clean-up operation following the test plan execution so that you can check
the resulting data afterward. Switching this parameter to `true` will force clean-up operations so no test data will
remain in the database after the execution of the tests.

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
the database connection URL {url} using the user {username} and the password {password}
```
Configure the database connection URL, username and password for following connections. This step is the declarative
equivalent to set the configuration properties [`database.connection.url`](#databaseconnectionurl),
[`database.connection.username`](#databaseconnectionusername),
[`database.connection.password`](#databaseconnectionpassword).

#### Parameters:
| Name       | Wakamiti type | Description        |
|------------|---------------|--------------------|
| `url`      | `text`        | The URL connection |
| `username` | `text`        | User name          |
| `password` | `text`        | User password      |

#### Examples:
```gherkin
Given the database connection URL 'jdbc:h2:tcp://localhost:9092/~/test' using the user 'test' and the password 'test'
```

<br />

---


### Define script post execution
```text copy=true
At finish, the following SQL script is executed:
```
Sets the SQL statements that will be executed one the scenario is finished, regardless execution status.

#### Parameters:
| Name   | Wakamiti type | Description    |
|--------|---------------|----------------|
|        | `document`    | Script content |

#### Examples:
```gherkin
* At finish, the following SQL script is executed:
    """
    UPDATE AAAA SET STATE = 2 WHERE ID = 1;
    DELETE FROM BBBB WHERE ID = 2;
    """
```

<br />

---


### Define script post execution (file)
```text copy=true
At finish, the SQL script file {file} is executed
```
Sets the SQL statements from file that will be executed one the scenario is finished, regardless execution status.

#### Parameters:
| Name   | Wakamiti type | Description |
|--------|---------------|-------------|
| `file` | `file`        | SQL file    |

#### Examples:
```gherkin
* At finish, the SQL script file 'data/insert-users.sql' is executed
```

<br />

---


### Execute script
```text copy=true
the following SQL script is executed:
```
Execute a in-line SQL script.

#### Parameters:
| Name | Wakamiti type | Description    |
|------|---------------|----------------|
|      | `document`    | Script content |

#### Examples:
```gherkin
Given that the following SQL script is executed:
    """sql
    UPDATE USER SET STATE = 2 WHERE BLOCKING_DATE IS NULL;
    DELETE FROM USER WHERE STATE = 3;
    """
```

<br />

---


### Execute script (file)
```text copy=true
the SQL script file {file} is executed
```
Execute a SQL script file.

#### Parameters:
| Name   | Wakamiti type | Description |
|--------|---------------|-------------|
| `file` | `file`        | SQL file    |

#### Examples:
```gherkin
When the SQL script file 'data/insert-users.sql' is executed
```

<br />

---


### Clear table
```text copy=true
the database table {word} is cleared
```
Clear the given table, first attempting to execute `TRUNCATE`, and then using `DELETE FROM` as fallback.

#### Parameters:
| Name   | Wakamiti type | Description    |
|--------|---------------|----------------|
| `word` | `word`        | The table name |

#### Examples:
```gherkin
Given that the database table USER is cleared
```

<br />

---


### Delete data
```text copy=true
* having {column} = {value} (is|are) deleted from the database table {table}
```
Delete the rows in the given table that match the specified value.

#### Parameters:
| Name     | Wakamiti type | Description    |
|----------|---------------|----------------|
| `column` | `word`        | Column name    |
| `value`  | `text`        | Column value   |
| `table`  | `word`        | The table name |

#### Examples:
```gherkin
When user having NAME = 'user1' is deleted from the database table USER 
```
```gherkin
When users having STATE = '2' are deleted from the database table USER 
```

<br />

---


### Delete data (table)
```text copy=true
the following * (is|are) deleted from the database table {word}:
```
Delete the following in-line rows from the given table.

#### Parameters:
| Name   | Wakamiti type | Description    |
|--------|---------------|----------------|
| `word` | `word`        | The table name |
|        | `table`       | Data table     |

#### Examples:
```gherkin
When the following users are deleted from the database table USER:
    | USER  | STATE | BLOCKING_DATE |   
    | user1 | 2     | <null>        |
    | user2 | 3     | 2020-02-13    |
```
```gherkin
When the following user is deleted from the database table USER:
    | USER  | STATE | BLOCKING_DATE |
    | user2 | 3     | 2020-02-13    |
```

<br />

---


### Delete data (XLS)
```text copy=true
the content of the XLS file {file} is deleted from the database
```
Delete the rows contained in the specified XLS file, one sheet per table.

#### Parameters:
| Name   | Wakamiti type | Description |
|--------|---------------|-------------|
| `file` | `file`        | XLS file    |

#### Examples:
```gherkin
When the content of the XLS file 'data/users.xls' is deleted from the database
```

<br />

---


### Delete data (CSV)
```text copy=true
the content of the CSV file {csv} is deleted from the database table {table}
```
Delete the rows contained in the specified CSV file from the given table.

#### Parameters:
| Name    | Wakamiti type | Description    |
|---------|---------------|----------------|
| `csv`   | `file`        | CSV file       |
| `table` | `word`        | The table name |

#### Examples:
```gherkin
When the content of the CSV file 'data/users.csv' is deleted from the database table USER
```

<br />

---


### Insert data (table)
```text copy=true
the following * (is|are) inserted into the database table {word}:
```
Insert the following in-line rows into the given table. Non-specified but required columns will be populated with random
values.

#### Parameters:
| Name   | Wakamiti type | Description    |
|--------|---------------|----------------|
| `word` | `word`        | The table name |
|        | `table`       | Data table     |

#### Examples:
```gherkin
When the following users are inserted into the database table USER:
    | USER  | STATE | BLOCKING_DATE |   
    | user1 | 2     | <null>        |
    | user2 | 3     | 2020-02-13    |
```
```gherkin
When the following user is inserted into the database table USER:
    | USER  | STATE | BLOCKING_DATE |   
    | user2 | 3     | 2020-02-13    |
```

<br />

---


### Insert data (XLS)
```text copy=true
the content of the XLS file {file} is inserted into the database
```
Insert the rows contained in the specified XLS file, one sheet per table. Non-specified but required columns will be
populated with random values.

#### Parameters:
| Name   | Wakamiti type | Description |
|--------|---------------|-------------|
| `file` | `file`        | XLS file    |

#### Examples:
```gherkin
Given that the content of the XLS file 'data/users.xls' is inserted into the database
``` 

<br />

---


### Insert data (CSV)
```text copy=true
the content of the CSV file {csv} is inserted into the database table {table}
```
Insert the rows contained in the specified CSV file into the given table. Non-specified but required columns will be
populated with random values.

#### Parameters:
| Name    | Wakamiti type | Description    |
|---------|---------------|----------------|
| `csv`   | `file`        | CSV file       |
| `table` | `word`        | The table name |

#### Examples:
```gherkin
When the content of the CSV file 'data/users.csv' is inserted into the database table USER
```

<br />

---


### Check data exists
```text copy=true
* having {column} = {value} exist(s) in the database table {table}
```
Assert that at least one row in the given table matches the specified value for a column.

#### Parameters:
| Name     | Wakamiti type | Description    |
|----------|---------------|----------------|
| `column` | `word`        | Column name    |
| `value`  | `text`        | Column value   |
| `table`  | `word`        | The table name |

#### Examples:
```gherkin
Then several users having STATE = '1' exist in the database table USER
```

<br />

---


### Check data not exists
```text copy=true
* having {column} = {value} do(es) not exist in the database table {table}
```
Assert that no row in the given table matches the specified value for a column.

#### Parameters:
| Name     | Wakamiti type | Description    |
|----------|---------------|----------------|
| `column` | `word`        | Column name    |
| `value`  | `text`        | Column value   |
| `table`  | `word`        | The table name |

#### Examples:
```gherkin
Given that users having STATE = '1' do no exist in the database table USER
```

<br />

---


### Check data exists (id)
```text copy=true
* identified by {id} exist(s) in the database table {table}
```
Assert that a row in the given table has a primary key matching the specified value. The table must have a single-column
primary key accessible from the database metadata.

#### Parameters:
| Name    | Wakamiti type | Description       |
|---------|---------------|-------------------|
| `id`    | `text`        | Primary key value |
| `table` | `word`        | The table name    |

#### Examples:
```gherkin
Then a user identified by 'user1' exists in the database table USER
```

<br />

---


### Check data not exists (id)
```text copy=true
* identified by {id} do(es) not exist in the database table {table}
```
Assert that no row in the given table has a primary key matching the specified value. The table must have a single-column
primary key accessible from the database metadata.

#### Parameters:
| Name    | Wakamiti type | Description       |
|---------|---------------|-------------------|
| `id`    | `text`        | Primary key value |
| `table` | `word`        | The table name    |

#### Examples:
```gherkin
Given that a user identified by 'user1' does not exist in the database table USER
```

<br />

---


### Check data exists (table)
```text copy=true
the following record(s) exist(s) in the database table {table}:
```
Assert that all the subsequent data rows exist in the given table.

#### Parameters:
| Name    | Wakamiti type | Description    |
|---------|---------------|----------------|
| `table` | `word`        | The table name |
|         | `table`       | Data table     |

#### Examples:
```gherkin
Then the following users exist in the database table USER:
    | USER  | STATE | BLOCKING_DATE |   
    | user1 | 2     | <null>        |
    | user2 | 3     | 2020-02-13    |
```

<br />

---


### Check data not exists (table)
```text copy=true
the following record(s) do(es) not exist in the database table {table}:
```
Assert that none of the subsequent data rows exist in the given table.

#### Parameters:
| Name    | Wakamiti type | Description    |
|---------|---------------|----------------|
| `table` | `word`        | The table name |
|         | `table`       | Data table     |

#### Examples:
```gherkin
Then the following users do not exist in the database table USER:
    | USER  | STATE | BLOCKING_DATE |   
    | user1 | 2     | <null>        |
    | user2 | 3     | 2020-02-13    |
```

<br />

---


### Check data exists (XLS)
```text copy=true
the content of the XLS file {file} exists in the database
```
Assert that all the data rows included in the specified XLS file exist in the database, using one sheet per table.

#### Parameters:
| Name   | Wakamiti type | Description |
|--------|---------------|-------------|
| `file` | `file`        | XLS file    |

#### Examples:
```gherkin
Given that the content of the XLS file 'data/users.xls' exists in the database
```

<br />

---


### Check data not exists (XLS)
```text copy=true
the content of the XLS file {file} does not exist in the database
```
Assert that none of the data rows included in the specified XLS file exist in the database, using one sheet per table.

#### Parameters:
| Name   | Wakamiti type | Description |
|--------|---------------|-------------|
| `file` | `file`        | XLS file    |

#### Examples:
```gherkin
Given that the content of the XLS file 'data/users.xls' does not exist in the database
```

<br />

---


### Check data exists (CSV)
```text copy=true
the content of the CSV file {csv} exists in the database table {table}
```
Assert that all the data rows included in the specified CSV file exists in the given table.

#### Parameters:
| Name    | Wakamiti type | Description    |
|---------|---------------|----------------|
| `csv`   | `file`        | CSV file       |
| `table` | `word`        | The table name |

#### Examples:
```gherkin
Then the content of the CSV file 'data/users.csv' exists in the database table USER
```

<br />

---


### Check data not exists (CSV)
```text copy=true
the content of the CSV file {csv} does not exist in the database table {table}
```
Assert that all the data rows included in the specified CSV file does not exist in the given table.

#### Parameters:
| Name    | Wakamiti type | Description    |
|---------|---------------|----------------|
| `csv`   | `file`        | CSV file       |
| `table` | `word`        | The table name |

#### Examples:
```gherkin
Given that the content of the CSV file 'data/users.csv' does not exists in the database table USER
```

<br />

---


### Check data exists (document)
```text copy=true
* satisfying the following SQL clause exist(s) in the database table {table}:
```
Assert that at least one row in the given table satisfies the specified SQL clause exist.

#### Parameters:
| Name    | Wakamiti type | Description    |
|---------|---------------|----------------|
| `table` | `word`        | The table name |
|         | `document`    | Where clause   |

#### Examples:
```gherkin
Then a user satisfying the following SQL clause exists in the database table USER:
    """
    STATE IN (2,3) OR BLOCKING_DATE IS NULL
    """
```

<br />

---


### Check data not exists (document)
```text copy=true
* satisfying the following SQL clause do(es) not exist in the database table {table}:
```
Assert that no row in the given table satisfies the specified SQL clause exist.

#### Parameters:
| Name    | Wakamiti type | Description    |
|---------|---------------|----------------|
| `table` | `word`        | The table name |
|         | `document`    | Where clause   |

#### Examples:
```gherkin
Given that a user satisfying the following SQL clause does not exist in the database table USER:
    """
    STATE IN (2,3) OR BLOCKING_DATE IS NULL
    """
```

<br />

---


### Check row count
```text copy=true
the number of * having {column} = {value} in the database table {table} {matcher}
```
Assert that the number of rows in the given table matching the specified values for two columns satisfies a numeric
assertion.

#### Parameters:
| Name      | Wakamiti type     | Description             |
|-----------|-------------------|-------------------------|
| `column`  | `word`            | Column name             |
| `value`   | `text`            | Column value            |
| `table`   | `word`            | The table name          |
| `matcher` | `long-assertion`  | Numeric [comparator][2] |

#### Examples:
```gherkin
Given that the number of users having STATE = '1' in the database table USER is greater than 5
```

<br />

---


### Check row count (table)
```text copy=true
the number of * satisfying the following (info) in the database table {table} {matcher}:
```
Assert that the number of rows in the given table matching the specified values for every column satisfies a numeric
expression. Only the first row of the given data is considered.

#### Parameters:
| Name      | Wakamiti type     | Description             |
|-----------|-------------------|-------------------------|
| `table`   | `word`            | The table name          |
| `matcher` | `long-assertion`  | Numeric [comparator][2] |
|           | `table`           | Data table              |

#### Examples:
```gherkin
Then the number of users satisfying the following in the database table USER is 0:
    | USER  | STATE | BLOCKING_DATE |   
    | user1 | 2     | <null>        |
```

```gherkin
Then the number of records satisfying the following info in the database table USER is more than 0:
    | USER  | STATE | BLOCKING_DATE |
    | user1 | 2     | <null>        |
```

<br />

---


### Check row count (document)
```text copy=true
the number of * satisfying the following SQL clause in the database table {table} {matcher}:
```
Assert that the number of rows in the given table matching the SQL clause satisfies a numeric assertion.

#### Parameters:
| Name      | Wakamiti type     | Description             |
|-----------|-------------------|-------------------------|
| `table`   | `word`            | The table name          |
| `matcher` | `long-assertion`  | Numeric [comparator][2] |
|           | `document`        | Where clause            |

#### Examples:
```gherkin
Then the number of users satisfying the following SQL clause in the database table USER is less than 10:
    """
    STATE IN (2,3) OR BLOCKING_DATE IS NULL
    """
```

<br />

---


### Check table is empty
```text copy=true
the database table {word} is empty
```
Assert that the given table has no data.

#### Parameters:
| Name   | Wakamiti type | Description    |
|--------|---------------|----------------|
| `word` | `word`        | The table name |

#### Examples:
```gherkin
Then the database table USER is empty
```

<br />

---


### Check table is not empty
```text copy=true
the database table {word} is not empty
```
Assert that the given tabla has some data.

#### Parameters:
| Name   | Wakamiti type | Description    |
|--------|---------------|----------------|
| `word` | `word`        | The table name |

#### Examples:
```gherkin
Then the database table USER is not empty
```





[1]: https://commons.apache.org/proper/commons-csv/
[2]: en/wakamiti/architecture#comparator