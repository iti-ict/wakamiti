---
title: DB steps
date: 2022-09-20
slug: /en/plugins/database
---



This plugin provides a set of steps that interact with a database via JDBC, easing the effort required to load and
assert data.

> **KEEP IN MIND** 
> 
> Regarding the multiple database implementations existing, this plugin does not include any specific driver. In order
> to work properly, do not forget to include the module with the JDBC driver(s) that your database connection would
> require.

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

---
## Table of content

---

---
## Configuración


### `database.connection.url`
Sets the JDBC database connection URL. The driver used to access the database will be determined by the URL format.

Example:
```yaml
database:
  connection:
    url: jdbc:h2:tcp://localhost:9092/~/test
```

<br /><br />

### `database.connection.username`
Sets the connection username, required to connect.

Example:
```yaml
database:
  connection:
    username: test
```

<br /><br />

### `database.connection.password`
Sets the connection password, required to connect.

Example:
```yaml
database:
  connection:
    password: test
```

<br /><br />

### `database.metadata.schema`
Sets the schema that should be used in order to retrieve metadata as primary keys and nullability. If not specified,
the default schema returned by the connection will be used.

Example:
```yaml
database:
  metadata:
    schema: TESTDB
```

<br /><br />

### `database.metadata.catalog`
Sets the catalog that should be used in order to retrieve meta-data as primary keys and nullability. If not specified,
the default catalog returned by the connection will be used (in case the database system uses one).

Example:
```yaml
database:
  metadata:
    catalog: TESTCAT
```

<br /><br />

### `database.metadata.caseSensititvy`
Sets whether any upper/lower case transformation should be done when trying to access meta-data. Some engines are
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

<br /><br />

### `database.csv.format`
Sets the format used to parse CSV files. The accepted values are directly imported from the [Commons CSV][1] project,
check it for detailed explanation of each format. Possible values are:
- `DEFAULT`
- `INFORMIX_UNLOAD`
- `INFORMIX_UNLOAD_CSV`
- `MYSQL`
- `ORACLE`
- `POSTGRESQL_CSV`
- `POSTGRESQL_TEXT`
- `RFC4180`

Default value is `DEFAULT`.

Example:
```yaml
database:
  csv:
    format: ORACLE
```

<br /><br />

### `database.xls.ignoreSheetPattern`
Sets regex pattern used to determine what sheets to be ignored when loading XLSX files.

Default value is `#.*`.

Example:
```yaml
database:
  xls:
    ignoreSheetPattern: //.*
```

<br /><br />

### `database.nullSymbol`
Sets literal used to handle a specific cell value as the SQL `NULL` element. It is used in any data source like CSV, XLS,
and in-line tables.

Default value is `<null>`.

Example:
```yaml
database:
  nullSymbol: (null)
```

<br /><br />

### `database.enableCleanupUponCompletion`
The default behaviour does not perform any clean-up operation following the test plan execution so that you can check
the resulting data afterward. Switching this parameter to `true` will force clean-up operations so no test data will
remain in the database after the execution of the tests.

Default value is `false`.

Example:
```yaml
database:
  enableCleanupUponCompletion: "true"
```


---
## Steps


### Define connection
``` copy=true
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

<br /><br />

### Define script post execution
``` copy=true
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

<br /><br />

### Define script post execution (file)
``` copy=true
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

<br /><br />

### Execute script
``` copy=true
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

<br /><br />

### Execute script (file)
``` copy=true
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

<br /><br />

### Clear table
``` copy=true
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

<br /><br />

### Delete data
``` copy=true
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

<br /><br />

### Delete data (table)
``` copy=true
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

<br /><br />

### Delete data (XLS)
``` copy=true
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

<br /><br />

### Delete data (CSV)
``` copy=true
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

<br /><br />

### Insert data (table)
``` copy=true
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

<br /><br />

### Insert data (XLS)
``` copy=true
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

<br /><br />

### Insert data (CSV)
``` copy=true
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

<br /><br />

### Check data exists
``` copy=true
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

<br /><br />

### Check data not exists
``` copy=true
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

<br /><br />

### Check data exists (id)
``` copy=true
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

<br /><br />

### Check data not exists (id)
``` copy=true
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

<br /><br />

### Check data exists (table)
``` copy=true
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

<br /><br />

### Check data not exists (table)
``` copy=true
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

<br /><br />

### Check data exists (XLS)
``` copy=true
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

<br /><br />

### Check data not exists (XLS)
``` copy=true
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

<br /><br />

### Check data exists (CSV)
``` copy=true
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

<br /><br />

### Check data not exists (CSV)
``` copy=true
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

<br /><br />

### Check data exists (document)
``` copy=true
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

<br /><br />

### Check data not exists (document)
``` copy=true
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

<br /><br />

### Check row count
``` copy=true
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

<br /><br />

### Check row count (table)
``` copy=true
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

<br /><br />

### Check row count (document)
``` copy=true
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

<br /><br />

### Check table is empty
``` copy=true
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

<br /><br />

### Check table is not empty
``` copy=true
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
[2]: en/plugins/wakamiti/architecture#comparator