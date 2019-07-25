# Kukumo::Database Steps

This plugin provides a set of steps that interact with a database via JDBC, easing the effort
required to load and assert data.

Data set can be defined in different ways:

- From CSV files, for a single table
- From XLSX files, for multiple tables
- From data tables included directly in the document, using the format:

```gherkin
 | columnA | columnB | columnC |
 | row1A   | row1B   | row1C   |
 | row2A   | row2B   | row2C   |
```



The plugin source code provides a convenient way to implement new data sources extending the abstract
class `DataSet`, which can be used by third-party contributors.


> **CAUTION:**  
> Regarding the multiple database implementations existing, this plugin **does not** include
any specific driver. In order to work properly, remember to include the module with the JDBC driver(s)
that your database connection would require.

---

## Usage

### Configuration properties

> **CAUTION:**  
> These properties must have the `kukumo.` prefix when defined in an external configuration file.

`database.connection.url`::

The JDBC database connection URL. The driver used to access the database will be determined by the URL format.

`database.connection.username`::
The connection username.

`database.connection.password`::
The connection password.

`database.csv.format` = `DEFAULT`::
The format used to parse CSV files. Accepted values are:
`DEFAULT`, `INFORMIX_UNLOAD`, `INFORMIX_UNLOAD_CSV`, `MYSQL`, `ORACLE`, `POSTGRESQL_CSV`,
`POSTGRESQL_TEXT`, `RFC4180`. Check the https://commons.apache.org/proper/commons-csv/[Commons CSV]
project for detailed explanation of each format.

`database.xls.ignoreSheetPattern` = `#.*` ::
Regex pattern used to determine what sheets to be ignored when loading XLSX files.

`database.xls.nullSymbol` = `<null>` ::
Literal used to handle a specific cell value as the SQL `NULL` element when loading XSLX files.

`database.enableCleanupUponCompletion` = `true | [false]` ::
The default behaviour does not perform any clean-up operation following the test iti.kukumo.test.gherkin.plan execution so that
you can check the resulting data afterwards. Switching this parameter to `true` will force
clean-up operations so no test data will remain in the database after the execution of the tests.


### Steps

- *the database connection URL ```{url:text}``` using the user ```{username:text}``` and the password ```{password:text}```*

  > Set the database connection URL, username and password.

  > **NOTE:**  
  > This step is equivalent to set the configuration properties `database.connection.url`,
`database.connection.username`, `database.connection.password`.


- the database schema {word} ::
- (that) the following SQL script is executed: ::
- (that) the SQL script file {file} is executed ::
- (that) the database table {word} is cleared ::
- (that) * in the database table {table:word} having column {column:word} = {value:text} is|are cleared ::
- (that) * in the database table {table:word} having column {column1:word} = {value1:text} and column {column2:word} = {value2:text} is|are cleared ::
- (that) the following * is|are inserted in the database table {word}: ::
- (that) the content of the XLS file {file} is inserted in the database ::
- (that) the content of the CSV file {csv:file} is inserted in the database table {table:word} ::
- (that) the following * is|are deleted from the database table {word}: ::
- (that) the content of the XLS file {file} is deleted from the database ::
- (that) the content of the CSV file {csv:file} is deleted from the database table {table:word} ::
- exists * identified by {id:text} in the database table {table:word} ::
- exists * in the database table {table:word} having column {column:word} = {value:text} ::
- exists * in the database table {table:word} having column {column1:word} = {value1:text} and column {column2:word} = {value2:text} ::
- exists * in the database table {table:word} satisfying the SQL clause {sql:text} ::
- the following * exist(s) in the database table {word}: ::
- the content of the XLS file {file} exists in the database ::
- the content of the CSV file {csv:file} exists in the database table {table:word} ::
- does not exist * identified by {id:text} in the database table {table:word} ::
- does not exist * in the database table {table:word} having column {column:word} = {value:text} ::
- does not exist * in the database table {table:word} having column {column1:word} = {value1:text} and column {column2:word} = {value2:text} ::
- does not exist * in the database table {table:word} satisfying the SQL clause {sql:text} ::
- the following * do(es) not exist in the database table {word}: ::
- the content of the XLS file {file} does not exist in the database ::
- the content of the CSV file {csv:file} does not exist in the database table {table:word} ::
- the number of * identified by {id:text} in the database table {table:word} {matcher:long-assertion} ::
- the number of * in the database table {table:word} having column {column:word} = {value:text} {matcher:long-assertion} ::
- the number of * in the database table {table:word} having column {column1:word} = {value1:text} and column {column2:word} = {value2:text} {matcher:long-assertion} ::
- the number of * in the database table {table:word} satisfying the SQL clause {sql:text} {matcher:long-assertion} ::
- the number of * satisfying the following * in the database table {table:word} {matcher:long-assertion}: ::
- the database table {word} is empty ::
- the database table {word} is not empty ::

