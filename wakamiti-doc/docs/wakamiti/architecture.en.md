---
title: Architecture
date: 2022-09-20
slug: /en/wakamiti/architecture
---


## Global configuration

Wakamiti's configuration is defined via `yaml` file located in the test directory. By default, Wakamiti will look for the 
file named `wakamiti.yaml`.

- [`wakamiti.resourceTypes`](#wakamitiresourcetypes)
- [`wakamiti.resourcePath`](#wakamitiresourcepath)
- [`wakamiti.outputFilePath`](#wakamitioutputfilepath)
- [`wakamiti.tagFilter`](#wakamititagfilter)
- [`wakamiti.idTagPattern`](#wakamitiidtagpattern)
- [`wakamiti.launcher.modules`](#wakamitilaunchermodules)
- [`wakamiti.report.generation`](#wakamitireportgeneration)
- [`wakamiti.redefinition.definitionTag`](#wakamitiredefinitiondefinitiontag)
- [`wakamiti.redefinition.implementationTag`](#wakamitiredefinitionimplementationtag)
- [`wakamiti.log.path`](#wakamitilogpath)
- [`wakamiti.log.level`](#wakamitiloglevel)
- [`mavenFetcher.remoteRepositories`](#mavenfetcherremoterepositories)
- [`mavenFetcher.localRepository`](#mavenfetcherlocalrepository)

---
### `wakamiti.resourceTypes`

Sets the language for test scenarios. Only `gherkin` is available for now.

Example:

```yaml
wakamiti:
  resourceTypes: gherkin
```

---
### `wakamiti.resourcePath`

Sets the path where the test files are located.

Default value is `.` (path where the configuration file is located).

Example:

```yaml
wakamiti:
  resourcePath: /other/path
```

---
### `wakamiti.outputFilePath`

Sets the results file directory.

Default value is `wakamiti.json` (in the current directory).

Example:

```yaml
wakamiti:
  outputFilePath: result/wakamiti.json
```

---
### `wakamiti.tagFilter`

Filter scenarios tagged with the indicated [expression](https://cucumber.io/docs/cucumber/api/#tag-expressions).

Example:

```yaml
wakamiti:
  tagFilter: not Ignore
```

---
### `wakamiti.idTagPattern`

Sets the indicated scenario id tag pattern. It must contains a valid regular expression.

Default value is `ID-(\w*)`.

Example:

```yaml
wakamiti:
  idTagPattern: ([0-9]+)
```

---
### `wakamiti.launcher.modules`

Sets the modules that will be used during the tests. These modules are maven artifacts located in a 
[repository indicated in the configuration](#mavenfetcher-remoterepositories). It must be specified with the pattern 
`<groupId>:<artifactId>:<version>`.

Example:

```yaml
wakamiti:
  launcher:
    modules:
      - es.iti.wakamiti:wakamiti-rest:1.0.0
      - es.iti.wakamiti:wakamiti-db:1.0.0
      - mysql:mysql-connector-java:8.0.28
```

---
### `wakamiti.report.generation`

Sets if the test result report will be generated at the end of execution or not.

Default value is `true`.

Example:

```yaml
wakamiti:
  report: 
    generation: "false"
```

---
### `wakamiti.redefinition.definitionTag`

Sets the tag to indicated that the **feature** is a [definition]().

Default value is `definition`.

Example:

```yaml
wakamiti:
  redefinition:
    definitionTag: def
```

---
### `wakamiti.redefinition.implementationTag`

Sets the tag to indicated that the **feature** is a [implementation]().

Default value is `implementation`.

Example:

```yaml
wakamiti:
  redefinition:
    implementationTag: impl
```

---
### `wakamiti.log.path`

Sets the directory where a log file named `wakamiti-${yyyyMMddhhmmss}.log` will be created, where
`${yyyyMMddhhmmss}` is the system date pattern. By default the log will not be created.

Example:

```yaml
wakamiti:
  log:
    path: results
```

---
### `wakamiti.log.level`

Sets the log level. Depending on the level indicated, more or less information will be displayed or omitted.
Possible values are: `info`, `error`, `fatal`, `warning`, `debug`, `trace`.
[See more](https://unpocodejava.com/2011/01/17/niveles-log4j/)

Default value is `info`.

Example:

```yaml
wakamiti:
  log:
    level: debug
```

---
### `mavenFetcher.remoteRepositories`

Sets remote reporitories.

Example:

```yaml
mavenFetcher:
  remoteRepositories: https://repo.maven.apache.org/maven2;file:///home/user/.m2/repository
```


---
### `mavenFetcher.localRepository`

Sets the local repository directory.

Example:

```yaml
mavenFetcher:
  localRepository: /usr/mvn-repo
```


---
## Data types

---
### `text`
Any text enclosed in quotes with `''`. 

Example: `'texto de ejemplo'`.

---
### `word`
Any word (supports hyphens). 

Example: `AB_C-1D`.

---
### `file`
A local file path (relative or absolute).

Example: `'dir/file.yaml'`.

---
### `url`
Example: `https://localhost/test`.

---
### `integer`
Example: `14`.

---
### `decimal`
Example: `14.5`.

---
### `date`
A date in format `yyyy-MM-dd`. 

Example: `'2022-02-22'`.

---
### `time`
A time in format `hh:mm`, `hh:mm:ss` o `hh:mm:ss.SSS`. 

Example: `'12:05:06.468'`.

---
### `datetime`
A datetime in format `yyyy-MM-ddThh:mm`, `yyyy-MM-ddThh:mm:ss` o `yyyy-MM-ddThh:mm:ss.SSS`. 

Example: 
`'2022-02-22T12:05:06.468'`.

---
### `text-assertion`
Text comparator. [See more](#comparators). 

Example: `is equals to 'something'`.

---
### `long-assertion`
Integer comparator. [See more](#comparators). 

Example: `is greater than or equals to 13`.

---
### `float-assertion`
Decimal comparator. [See more](#comparators). 

Example: `is less than or equals to 10.02`.

---
### `document`
Text block located on the next line of the step description.
[See more](https://cucumber.io/docs/gherkin/reference/#doc-strings). 

Example:
```gherkin
"""
multiline
text
"""
```

---
### `table`
Data table located on the next line of the step description.
[See more](https://cucumber.io/docs/gherkin/reference/#data-tables).

Example:
```gherkin
| USER  | STATE | BLOCKING_DATE |
| user1 | 2     | <null>        |
```


---
## Comparators

Text snippets that translate into matchers for different data types, reusable at any step. The available comparator 
types are:
- `text-assertion`
- `integer-assertion`
- `long-assertion`
- `decimal-assertion`


---
### `is (not) (equals to) ~x~` 
Type: numeric and text. It also supports the variants `is (equals to) ~x~ \(ignoring case\)` and
`is (equals to) ~x~ \(ignoring whitespace\)` for text type comparators and the negative version.

Example:
```
is 14
```
```
is equals to 22
```
```
is equals to 'algo'
```
```
is 'something'
```
```
is equals to 'somEthing' (ignoring case)
```
```
is equals to ' something ' (ignoring whitespace)
```
```
is not equals to 14
```

---
### `is (not) (greather|less) than ~x~`
Type: numeric. It also supports decimals and the negative version.

Example:
```
is greather than 14
```
```
is greather than 14.3
```
```
is less than 14
```
```
is less thane 14.3
```
```
is not greather than 14
```

---
### `is (not) (greather|less) or equals to ~x~`
Type: numeric. It also supports decimals and the negative version.

Example:
```
is greather or equals to 14
```
```
is greather or equals to 14.3
```
```
is less or equals to 14
```
```
is less or equals to 14.3
```
```
is not greather or equals to 14
```

---
### `(does not) starts with ~x~`
Type: text. It also supports the variants `is (equals to) ~x~ \(ignoring case\)` and the negative version.

Example:
```
starts with 'something'
```
```
starts with 'somEthing' (ignoring case)
```
```
does not starts with 'something'
```

---
### `(does not) end with ~x~`
Type: text. It also supports the variants `is (equals to) ~x~ \(ignoring case\)` and the negative version.

Example:
```
end with 'something'
```
```
end with 'somEthing' (ignoring case)
```
```
does not end with 'something'
```

---
### `(does not) contains ~x~`
Type: text. It also supports the variants `is (equals to) ~x~ \(ignoring case\)` and the negative version.

Example:
```
contains 'something'
```
```
contains 'somEthing' (ignoring case)
```
```
does not contains 'something'
```

---
### `is (not) null`
Type: numeric and text. It also supports the negative version.

Example:
```
is null
```
```
is not null
```

---
### `is (not) empty`
Type: numeric and text. It also supports the negative version.

Example:
```
is empty
```
```
is not empty
```

---
### `is (not) null or empty`
Type: numeric and text. It also supports the negative version.

Example:
```
is null or empty
```
```
is not null or empty
```


-------
## Dynamic properties

Wakamiti allows using dynamic properties to make easier the passing of information to the execution 
though `${[property description]}` syntax.

The `property description` will depend on the type of property you want to apply. By default, there are following:
- Get the value of a global property, using the syntax `${[name]}`. For example: `${credential.password}`.
- Get the result of a previous step, using the syntax `${[step number]#[xpath/jsonpath expression]`.
  For example: `${2#$.body.items[0].id}`, this example will retrieve the result of step 2 (which is expected to be
  a json) and the value of the given jsonpath expression will be retrieved. **Note**: The xpath/jsonpath expression is 
  optional.

Let's see a practical example:
- We have the following yaml configuration:
```yml
wakamiti:
   credentials:
     username: pepe
     password: 1234asdf
```
- We have the following scenario:
```cucumber
Scenario: Test scenario
   Given the service use the oauth authentication credentials '${credentials.username}':'${credentials.password}'
   When users are queried
   Then a user identified by '${2#$.body.items[0].id}' exist in the database table USERS
```

On launch, the scenario would resolve as follows:
```cucumber
Scenario: Test scenario
   Given the service use the oauth authentication credentials 'pepe':'1234asdf'
   When users are queried
   Then a user identified by '4' exist in the database table USERS
```

**Note**: We'll assume that the step `When users are queried` returns the following result:
```json
{
   "headers": {
     "content-type": "json/application",
     "Connection": "Keep-alive"
   },
   "body": {
     "items": [
       {
         "id": 4,
         "name": "Pepe"
       },
       {
         "id": 7,
         "name": "Anna"
       }
     ]
   },
   "statusCode": 200,
   "statusLine": "HTTP/1.1 200 OK"
}
```