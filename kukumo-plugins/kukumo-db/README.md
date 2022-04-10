
# Database Steps

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


> **KEEP IN MIND**  
> Regarding the multiple database implementations existing, this plugin **does not** include
any specific driver. In order to work properly, do not forget to include the module with the JDBC driver(s)
that your database connection would require.




## Usage

### Configuration properties

> **KEEP IN MIND**  
> These properties must have the `kukumo.` prefix when defined in an external configuration file.

#### `database.connection.url`
The JDBC database connection URL. The driver used to access the database will be determined by the URL format.

#### `database.connection.username`
The connection username, required to connect.

#### `database.connection.password`
The connection password, required to connect.

#### `database.metadata.schema`
The schema that should be used in order to retrieve meta-data as primary keys and nullability. If not specified, 
the default schema returned by the connection will be used.

#### `database.metadata.catalog`
The catalog that should be used in order to retrieve meta-data as primary keys and nullability. If not specified, the default catalog returned by the connection will be used (in case the database system 
uses one).

#### `database.metadata.caseSensititvy` = [`INSENSITIVE`] | `LOWER_CASED` | `UPPER_CASED`
Configure whether any upper/lower case transformation should be done when trying to access meta-data. 
Some engines are very strict regarding this issue, and can cause unexpected errors if this property is not properly configured.

#### `database.csv.format` = [`DEFAULT`], `INFORMIX_UNLOAD`, `INFORMIX_UNLOAD_CSV`, `MYSQL`, `ORACLE`, `POSTGRESQL_CSV`, `POSTGRESQL_TEXT`, `RFC4180`
The format used to parse CSV files. The accepted values are directly imported from the [Commons CSV][1]
project, check it for detailed explanation of each format.

#### `database.xls.ignoreSheetPattern` = `#.*` 
Regex pattern used to determine what sheets to be ignored when loading XLSX files.

#### `database.nullSymbol` = `<null>`
Literal used to handle a specific cell value as the SQL `NULL` element. It is used in any data source like CSV, XLS, and in-line tables.

#### `database.enableCleanupUponCompletion` = `true | [false]` 
The default behaviour does not perform any clean-up operation following the test plan execution so that
you can check the resulting data afterwards. Switching this parameter to `true` will force
clean-up operations so no test data will remain in the database after the execution of the tests.



### Steps

> **NOTE**   
> The keywords appearing within brackets refer to Gherkin keyword examples, but have no actual meaning

#### Configuration steps

##### *[Given]* the database connection URL ```{url:text}``` using the user ```{username:text}``` and the password ```{password:text}```
Configure the database connection URL, username and password for following connections.  

This step is the declarative equivalent to set the configuration properties `database.connection.url`,
`database.connection.username`, `database.connection.password`.
>```gherkin
> Given the database connection URL 'jdbc:h2:tcp://localhost:9092/~/test' using the user 'sa' and the password ''
>```

#### Script execution steps

##### *[Given that/When]* the following SQL script is executed:
Execute a in-line SQL script. 
>```gherkin
> Given that the following SQL script is executed:
> """
>  UPDATE USER SET STATE = 2 WHERE BLOCKING_DATE IS NULL;
>  DELETE FROM USER WHERE STATE = 3;
> """
>```

##### *[Given that/When]* the SQL script file {file} is executed
Execute a SQL script file.
>```gherkin
> When the SQL script file 'data/insert-users.sql' is executed
>


#### Delete row execution steps

##### *[Given that/When]* the database table {word} is cleared
Clear the given table, first attempting to execute `TRUNCATE`, and then using `DELETE FROM` as fallback.
>```gherkin
> Given that the database table USER is cleared
>```

##### *[Given that/When]* * having {column:word} = {value:text} is|are deleted from the database table {table:word}
Delete the rows in the given table that match the specified value.
>```gherkin
> When users having STATE = '2' are deleted from the database table USER 
>```


##### *[Given that/When]* * having {column1:word} = {value1:text} and {column2:word} = {value2:text} is|are deleted from the database table {table:word}
Delete the rows in the given table that match both specified values.
>```gherkin
> Given that users having STATE = '2' and BLOCKING_DATE = '<null>' are deleted from the database table USER 
>```

##### *[Given that/When]* the following * (is|are) deleted from the database table {word}:
Delete the following in-line rows from the given table.
>```gherkin
> Given that the following is deleted from the database table USER:
>  | USER  | STATE | BLOCKING_DATE |   
>  | user1 | 2     | <null>        |
>  | user2 | 3     | 2020-02-13    |
>```

##### *[Given that/When]* the content of the XLS file {file} (is) deleted from the database
Delete the rows contained in the specified XLS file, one sheet per table.
>```gherkin
> When the content of the XLS file 'data/users.xls' is deleted from the database
>``` 

##### *[Given that/When]* the content of the CSV file {csv:file} (is) deleted from the database table {table:word}
Delete the rows contained in the specified CSV file from the given table.
>```gherkin
> When the content of the CSV file 'data/users.csv' is deleted from the database table USER
>```


#### Insert row execution steps
##### *[Given that/When]* the following * (is|are) inserted into the database table {word}:
Insert the following in-line rows into the given table. Non-specified but required columns will be populated with random values.
>```gherkin
> When the following is inserted into the database table USER:
>  | USER  | STATE | BLOCKING_DATE |   
>  | user1 | 2     | <null>        |
>  | user2 | 3     | 2020-02-13    |
>```

##### *[Given that/When]* the content of the XLS file {file} (is) inserted into the database
Insert the rows contained in the specified XLS file, one sheet per table. Non-specified but required columns will be populated with random values.
>```gherkin
> Given that the content of the XLS file 'data/users.xls' is inserted into the database
>``` 

##### *[Given that/When]* the content of the CSV file {csv:file} (is) inserted into the database table {table:word}
Insert the rows contained in the specified CSV file into the given table. Non-specified but required columns will be populated with random values.
>```gherkin
> When the content of the CSV file 'data/users.csv' is inserted into the database table USER
>```

#### Data presence assertion steps

##### *[Given that/Then]* * identified by {id:text} exist(s) in the database table {table:word}
Assert that a row in the given table has a primary key matching the specified value. The table must have a single-column 
primary key accessible from the database metadata. 
>```gherkin
> Then a user identified by 'user1' exists in the database table USER
>```

##### *[Given that/Then]* * identified by {id:text} do(es) not exist in the database table {table:word}
Assert that no row in the given table has a primary key matching the specified value. The table must have a single-column 
primary key accessible from the database metadata. 
>```gherkin
>  Given that a user identified by 'user1' does not exist in the database table USER
>```

##### *[Given that/Then]* * having {column:word} = {value:text} exist(s) in the database table {table:word}
Assert that at least one row in the given table matches the specified value for a column
>```gherkin
> Then several users having STATE = '1' exist in the database table USER
>```

##### *[Given that/Then]* * having {column:word} = {value:text} do(es) not exist in the database table {table:word}
Assert that no row in the given table matches the specified value for a column
>```gherkin
> Given that users having STATE = '1' do no exist in the database table USER
>```

##### *[Given that/Then]* the number of * having {column:word} = {value:text} in the database table {table:word} {matcher:long-assertion}
Assert that the number of rows in the given table matching the specified value for one column satisfies a numeric assertion
>```gherkin
> Then the number of users having STATE = '1' in the database table USER is greater than 5
>```


##### *[Given that/Then]* * having {column1:word} = {value1:text} and {column2:word} = {value2:text} exist(s) in the database table {table:word}
Assert that at least one row in the given table matches the specified values for two columns
>```gherkin
> Then several users having STATE = '1' and BLOCKING_DATE = '<null>' exist in the database table USER
>```

##### *[Given that/Then]* * having {column1:word} = {value1:text} and {column2:word} = {value2:text} do(es) not exists in the database table {table:word}
Assert that no row in the given table matches the specified values for two columns
>```gherkin
> Given that users having STATE = '1' and BLOCKING_DATE = '<null>' do not exist in the database table USER
>```

##### *[Given that/Then]* the number of * having {column1:word} = {value1:text} and {column2:word} = {value2:text} in the database table {table:word} {matcher:long-assertion}
Assert that the number of rows in the given table matching the specified values for two columns satisfies a numeric assertion
>```gherkin
> Given that the number of users having STATE = '1' and BLOCKING_DATE = '<null>' in the database table USER is not 7
>```

##### *[Given that/Then]* * satisfying the SQL clause {sql:text} exist(s) in the database table {table:word}
Assert that at least one row in the given table satisfies the specified SQL clause exist
>```gherkin
> Then a user satisfying the SQL clause 'STATE IN (2,3) OR BLOCKING_DATE IS NULL' exists in the database table USER
>```

##### *[Given that/Then]* * satisfying the SQL clause {sql:text} do(es) not exist in the database table {table:word}
Assert that no row in the given table satisfies the specified SQL clause exist
>```gherkin
> Given that a user satisfying the SQL clause 'STATE IN (2,3) OR BLOCKING_DATE IS NULL' does not exist in the database table USER
>```

##### *[Given that/Then]* the number of * satisfying the SQL clause {sql:text} in the database table {table:word} {matcher:long-assertion}
Assert that the number of rows in the given table matching the SQL clause satisfies a numeric assertion
>```gherkin
> Then the number of users satisfying the SQL clause 'STATE IN (2,3) OR BLOCKING_DATE IS NULL' in the database table USER is less than 10
>```

##### *[Given that/Then]* the following * exist(s) in the database table {word}:
Assert that all the subsequent data rows exist in the given table
>```gherkin
> Then the following users exist in the database table USER:
>  | USER  | STATE | BLOCKING_DATE |   
>  | user1 | 2     | <null>        |
>  | user2 | 3     | 2020-02-13    |
>```

##### *[Given that/Then]* the following * do(es) not exist in the database table {word}:
Assert that none of the subsequent data rows exist in the given table
>```gherkin
> Then the following users do not exist in the database table USER:
>  | USER  | STATE | BLOCKING_DATE |   
>  | user1 | 2     | <null>        |
>  | user2 | 3     | 2020-02-13    |
>```

##### *[Given that/Then]* the number of * satisfying the following * in the database table {table:word} {matcher:long-assertion}:
Assert that the number of rows in the given table matching the specified values for every column satisfies a numeric expression.
Only the first row of the given data is considered.
>```gherkin
> Then the number of users satisfying the following data in the database table USER is 0
>  | USER  | STATE | BLOCKING_DATE |   
>  | user1 | 2     | <null>        |
>```


##### *[Given that/Then]* the content of the XLS file {file} exists in the database
Assert that all of the data rows included in the specified XLS file exist in the database, using one sheet per table
>```gherkin
> Given that the content of the XLS file 'data/users.xls' exists in the database
>```

##### *[Given that/Then]* the content of the XLS file {file} does not exist in the database
Assert that none of the data rows included in the specified XLS file exist in the database, using one sheet per table
>```gherkin
> Given that the content of the XLS file 'data/users.xls' does not exist in the database
>```

##### *[Given that/Then]* the content of the CSV file {csv:file} exists in the database table {table:word}
Assert that all the data rows included in the specified CSV file exists in the given table
>```gherkin
> Then the content of the CSV file 'data/users.csv' exists in the database table USER
>```

##### *[Given that/Then]* the content of the CSV file {csv:file} does not exist in the database table {table:word}
Assert that all the data rows included in the specified CSV file exists in the given table
>```gherkin
>  Given that the content of the CSV file 'data/users.csv' does not exists in the database table USER
>```

##### *[Given that/Then]* the database table {word} is empty
Assert that the given table has no data
>```gherkin
>  Then the database table USER is empty
>```

##### *[Given that/Then]* the database table {word} is not empty
Assert that the given tabla has some data
>```gherkin
>  Then the database table USER is not empty
>```






  
## References  

- [**1**] *Common CSV* -  https://commons.apache.org/proper/commons-csv/  
  
[1]:  https://commons.apache.org/proper/commons-csv/
