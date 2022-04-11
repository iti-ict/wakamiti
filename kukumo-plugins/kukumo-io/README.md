Kukumo :: I/O plugin
====================================================================================================

This plugin provides a set of steps to interact with I/O operations


Configuration
----------------------------------------------------------------------------------------------------

###  `files.timeout`
Set the timeout time (in seconds) for reading access to files.

Default value is `60`.

Example:

```yaml
files:
  timeout: 180
```

###  `files.link`
Set symbolic links to files or directories in order to ease access during the tests
Example:

```yaml
files:
  links: path1=/tmp/file1;path2=/tmp/path
```

###  `files.enableCleanupUponCompletion`
Set whether any file changes should be undone after the test is executed.

Default value is `false`.

Example:
```yaml
files:
  enableCleanupUponCompletion: true
```


Steps
--------------------------------------------------------------------------------------------

### Setup steps
Set the timeout time (in seconds) for reading access to files. This step is equivalent
to set the `files.timeout` configuration property.

---
#### `a file timeout of {value:long} seconds`
Set the 
##### Parameters:
| name | Kukumo type | description        |
|------|-------------|--------------------|
|      | `long`      | Timeout in seconds |
##### Examples:
```gherkin
Given a file timeout of 10 seconds
```
##### Localizations:
- :es: `un tiempo de espera de {long} segundos`


### Action steps


---
#### `the (file|directory) {src:file} is moved to directory {dest:file}`
Move a file or directory to the target directory.
##### Parameters:
| name    | Kukumo type | description                  |
|---------|-------------|------------------------------|
| `src`   | `file`      | The source file or directory |
| `dest`  | `file`      | The target directory         |
##### Examples:
```gherkin
When the file 'tmp/file.txt' is moved to the directory '/tmp/dir'
And the directory 'tmp/dir' is moved to the directory '/tmp/dir2'
```
##### Localizations:
- :es: `el (fichero|directory) {src:file} se mueve al directorio {dest:file}`




---
#### `the (file|directory) {src:file} is moved to file {dest:file}`
Rename a file or directory (or move it to a new path)
##### Parameters:
| name    | Kukumo type | description                  |
|---------|-------------|------------------------------|
| `src`   | `file`      | The source file or directory |
| `dest`  | `file`      | The new path                 |
##### Examples:
```gherkin
```
##### Localizations:
- :es: ``



---
#### `the (file|directory) {src:file} is copied to directory {dest:file}`
Copy a file or directory to the target directory.
##### Parameters:
| name    | Kukumo type | description                  |
|---------|-------------|------------------------------|
| `src`   | `file`      | The source file or directory |
| `dest`  | `file`      | The target directory         |
##### Examples:
```gherkin
When the file 'tmp/file.txt' is copied to the directory '/tmp/dir'
And the directory 'tmp/dir' is copied to the directory '/tmp/dir2'
```
##### Localizations:
- :es: `el (fichero|directorio) {src:file} se copia al directorio {dest:file}`




---
#### `the file {src:file} is copied to file {dest:file}`
Copy a file
##### Parameters:
| name    | Kukumo type | description     |
|---------|-------------|-----------------|
| `src`   | `file`      | The source file |
| `dest`  | `file`      | The target file |
##### Examples:
```gherkin
When the file 'tmp/file.txt' is copied to the file '/tmp/file2.txt'
```
##### Localizations:
- :es: `el fichero {src:file} se copia al fichero {dest:file}`





---
#### `the (file|directory) {dest:file} is deleted`
Delete the given file or directory
##### Parameters:
| name | Kukumo type | description                     |
|------|-------------|---------------------------------|
|      | `file`      | The file or directory to delete |
##### Examples:
```gherkin
Then the file '/tmp/file.txt' is deleted
```
##### Localizations:
- :es: `el fichero '/tmp/file.txt' se elimina`





---
#### `the (file|directory) {file} deletion is awaited`
Await until the given file or directory is deleted
##### Parameters:
| name | Kukumo type | description                    |
|------|-------------|--------------------------------|
|      | `file`      | The file or directory to watch |
##### Examples:
```gherkin
Then the file '/tmp/file.txt' deletion is awaited
```
##### Localizations:
- :es: `se espera a que el (fichero|directorio) {file} se elimine`





---
#### `the (file|directory) {file} modification is awaited`
Await until the given file or directory is modified
##### Parameters:
| name | Kukumo type | description                    |
|------|-------------|--------------------------------|
|      | `file`      | The file or directory to watch |
##### Examples:
```gherkin
And the file '/tmp/file.txt' is modification is awaited
```
##### Localizations:
- :es: `se espera a que el (fichero|directorio) {file} se modifique`





---
#### `the (file|directory) {file} creation is awaited`
Await until the given file or directory is created
##### Parameters:
| name | Kukumo type | description                    |
|------|-------------|--------------------------------|
|      | `file`      | The file or directory to watch |
##### Examples:
```gherkin
Then the file '/tmp/file.txt' creation is awaited
```
##### Localizations:
- :es: `se espera a que el (fichero|directory) {file} se cree`





### Validation steps


---
#### `the file {file} exists`
Validate that the given file exists
##### Parameters:
| name | Kukumo type | description       |
|------|-------------|-------------------|
|      | `file`      | The file to check |
##### Examples:
```gherkin
Then the file '/tmp/file.txt' exists
```
##### Localizations:
- :es: `el fichero {file} existe`




---
#### `the file {file} not exists`
Validate that the given file does not exist
##### Parameters:
| name | Kukumo type | description       |
|------|-------------|-------------------|
|      | `file`      | The file to check |
##### Examples:
```gherkin
Then the file '/tmp/file.txt' not exists
```
##### Localizations:
- :es: `el fichero {file} no existe`



---
#### `the file {file} contains the following text:`
Validate that a file contains the given text
##### Parameters:
| name | Kukumo type  | description                         |
|------|--------------|-------------------------------------|
|      | `file`       | The file to check                   |
|      | `document`   | The expected text to be in the file |
##### Examples:
```gherkin
Then the file '/tmp/file.txt' contains the following data:
"""
file contents
"""
```
##### Localizations:
- :es: `el fichero {file} contiene el siguiente texto:`




---
#### `the file {file:file} has length of {chars:int}`
Validate the file has a certain length (in bytes) 
##### Parameters:
| name    | Kukumo type | description                  |
|---------|-------------|------------------------------|
| `file`  | `file`      | The file to check            |
| `chars` | `int`       | The expected length in bytes |
##### Examples:
```gherkin
Then the file '/tmp/file.txt' has length of 112
```
##### Localizations:
- :es: `el fichero {file:file} tiene una longitud de {chars:int}`








