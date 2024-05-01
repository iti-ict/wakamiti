---
title: Architecture
date: 2022-09-20
slug: /en/wakamiti/architecture
---


---
## Table of content

---


## Global options

Wakamiti's configuration is set up via `yaml` file located in the test directory. By default, Wakamiti searches for the 
file named `wakamiti.yaml`.


### `wakamiti.resourceTypes`
- Type: `string` *required*

Sets the resource type of the test scenarios. Currently only `gherkin` is available.

Example:
```yaml
wakamiti:
  resourceTypes: gherkin
```


### `wakamiti.language`
- Type: `locale` 
- Default: `en`

Sets the language of the test scenarios.


### `wakamiti.resourcePath`
- Type: `file`
- Default: `.` (path where the configuration file is located)

Sets the path where the test files are located.

Example:
```yaml
wakamiti:
  resourcePath: /other/path
```


### `wakamiti.outputFilePath`
- Type: `file`
- Default: `wakamiti.json` (in the current directory)

Sets the results file directory.

The output path may contain the following replacement placeholders:

| placeholder | replacement             |
|-------------|-------------------------|
| `%YYYY%`    | year (4 dígits)         |
| `%YY%`      | year (2 dígits)         |
| `%MM%`      | month                   |
| `%DD%`      | day of month            |
| `%hh%`      | hour (24H)              |
| `%mm%`      | minutes                 |
| `%ss%`      | seconds                 |
| `%sss%`     | milliseconds (3 dígits) |
| `%DATE%`    | `%YYYY%%MM%%DD%`        |
| `%TIME%`    | `%hh%%mm%%ss%%ssss%`    |
| `%execID%`  | unique execution        |

Example:
```yaml
wakamiti:
  outputFilePath: result/wakamiti.json
```


### `wakamiti.outputFilePerTestCase`
- Type: `boolean`
- Default: `false`

Specifies whether an output file should be created for each test case. If enabled, the value of `wakamiti.outputFilePath` 
is the destination directory and the filename is the test case ID itself.

Example:
```yaml
wakamiti:
  outputFilePerTestCase: true
```


### `wakamiti.tagFilter`
- Type: `string`

Filter out scenarios tagged with the specified [expression](https://cucumber.io/docs/cucumber/api/#tag-expressions).

Example:
```yaml
wakamiti:
  tagFilter: not Ignore
```


### `wakamiti.idTagPattern`
- Type: `regex`
- Default: `ID([\w-]+)`

Specifies the tag pattern of scenario identifiers. Must be a valid regular expression.

Example:
```yaml
wakamiti:
  idTagPattern: ([0-9]+)
```


### `wakamiti.strictTestCaseID`
- Type: `boolean`
- Default: `false`

Sets whether the plan executor should assert that each test case is properly annotated with a tag 
matching the pattern from  `idTagPattern`. If enabled and some test cases do not satisfy this requirement, the 
executor will stop resulting in an error.

Example:
```yaml
wakamiti:
  strictTestCaseID: true
```


### `wakamiti.launcher.modules`
- Type: `string[]`

Sets the modules to be used during tests. These modules are Maven artifacts located in a 
[repository indicated in the configuration](#mavenfetcher-remoterepositories). They must be specified using the pattern 
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


### `wakamiti.report.generation`
- Type: `boolean`
- Default: `true`

Specifies whether the test result report is generated when the test is finished running.

Example:
```yaml
wakamiti:
  report: 
    generation: "false"
```

### `wakamiti.redefinition.definitionTag`
- Type: `string`
- Default: `definition`

Sets the tag to indicated that the **feature** is a [definition]().

Example:
```yaml
wakamiti:
  redefinition:
    definitionTag: def
```


### `wakamiti.redefinition.implementationTag`
- Type: `string`
- Default: `implementation`

Sets the tag to indicated that the **feature** is a [implementation]().

Example:
```yaml
wakamiti:
  redefinition:
    implementationTag: impl
```


### `wakamiti.properties.hidden`
- Type: `string[]`

Specifies which [properties](#dynamic-properties) should be hidden in the test report.

Example:
```yaml
wakamiti:
  properties:
    hidden: 
      - token
      - credentials.password
```


### `wakamiti.log.path`
- Type: `file`

Specify the directory where a log file named `wakamiti-${yyyyMMddhhmmss}.log` will be created, where `${yyyyMMddhhmmss}` 
is the system date pattern. By default, the log is not created.

Example:
```yaml
wakamiti:
  log:
    path: results
```


### `wakamiti.log.level`
- Type: `string`
- Default: `info`

Sets the log level. Depending on the level indicated, more or less information will be displayed or omitted.
Possible values are: `info`, `error`, `fatal`, `warning`, `debug`, `trace`.
[See more](https://unpocodejava.com/2011/01/17/niveles-log4j/)

Example:
```yaml
wakamiti:
  log:
    level: debug
```


### `wakamiti.logs.ansi.enabled`
- Type: `boolean`
- Default: `true`

Sets whether the console logs should use [ANSI escape codes](https://en.wikipedia.org/wiki/ANSI_escape_code).

Example:
```yaml
wakamiti:
  logs:
    ansi:
      enabled: true
```


### `wakamiti.logs.showLogo`
- Type: `boolean`
- Default: `true`

Determines whether the Wakamiti logo is displayed in the console logs at the start of execution.

Example:
```yaml
wakamiti:
  logs:
    showLogo: true
```


### `wakamiti.logs.showElapsedTime`
- Type: `boolean`
- Default: `true`

Sets whether elapsed times are displayed in the console logs.

Example:
```yaml
wakamiti:
  logs:
    showElapsedTime: true
```


### `wakamiti.junit.treatStepsAsTests`
- Type: `boolean`
- Default: `false`

Specifies whether each step should be reported as a test when using Wakamiti's JUnit runner (to make JUnit derived 
reports and results more relevant).

Example:
```yaml
wakamiti:
  junit:
    treatStepsAsTests: true
```


### `wakamiti.nonRegisteredStepProviders`
- Type: `string[]`

It allows you to dynamically include step providers without them being part of a plugin. This allows ad hoc steps to be 
created for the needs of the specific project being tested.

Example:
```yaml
wakamiti:
  nonRegisteredStepProviders:
    - com.example.CustomSteps
```


### `mavenFetcher.remoteRepositories`
- Type: `URL[]`

Sets remote repositories.

Example:
```yaml
mavenFetcher:
  remoteRepositories: https://repo.maven.apache.org/maven2;file:///home/user/.m2/repository
```


### `mavenFetcher.localRepository`
- Type: `file`

Sets the local repository directory.

Example:
```yaml
mavenFetcher:
  localRepository: /usr/mvn-repo
```



## Feature options

In addition to the global configuration, specific properties can be included in each feature file.


### `language`
- Type: `locale`
- Default: `en`

Sets the language (identified by means of the standard 
[ISO 639-1](https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes)) which be used to write the test steps.

Example:
```gherkin
# language: es
Feature: ...
```


### `dataFormatLanguage`
- Type: `locale`
- Default: `en`

Specifies the language (identified by [ISO 639-1](https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes)) in which the 
step parameter values are to be written, in cases where localised formats are accepted, such as dates and numbers with 
separators.

Example:
```gherkin
# language: es
# dataFormatLanguage: en  
Feature: ...
```


### `modules`
- Type: `string[]`

Restricts the steps to be used in a file to avoid possible expression conflicts between multiple plugins that are not 
designed to work together.

Example:
```gherkin
#modules: database-steps, rest-steps
Feature: ...
```


### `redefinition.stepMap`
- Type: `integer[]`

When a test case is represented both at definition and implementation levels, this property
declares the correspondence between definition steps and implementation steps. Otherwise, a default 
one-to-one correspondence would be used.

The value of this property must be a list of numbers split by `-` , being each one the number 
of implementation steps corresponding to the definition step at that position. For instance, the 
value `1-2-1` implies the following correspondence:

| definition  | implementation | 
|-------------|----------------|
| 1           | 1              |
| 2           | 2,3            |
| 3           | 4              |

Note that this property must be defined in the implementation section, above the proper scenario. In addition to this, 
the scenario must have a unique identifier.

Example:
```gherkin
@implementation
Feature: ...

  
# redefinition.stepMap: 2-1-2  
@ID-43242   
Scenario: ...
```



## Data types


### `text`
Any text enclosed in quotes with `''`.

Example: `'texto de ejemplo'`.


### `word`
Any word (supports hyphens).

Example: `AB_C-1D`.


### `file`
A local file path (relative or absolute).

Example: `'dir/file.yaml'`.


### `url`
Example: `https://localhost/test`.


### `integer`
Example: `14`.


### `decimal`
Example: `14.5`.


### `date`
A date in format `yyyy-MM-dd`. 

Example: `'2022-02-22'`.


### `time`
A time in format `hh:mm`, `hh:mm:ss` or `hh:mm:ss.SSS`. 

Example: `'12:05:06.468'`.


### `datetime`
A datetime in format `yyyy-MM-ddThh:mm`, `yyyy-MM-ddThh:mm:ss` or `yyyy-MM-ddThh:mm:ss.SSS`. 

Example: 
`'2022-02-22T12:05:06.468'`.


### `duration`

A text fragment that translates to a duration in the format 
`~x~ (nanosecond|microsecond|millisecond|second|minute|hour|day)(s)`, where `~x~` is an integer.

Example: `3 seconds`.


### `text-assertion`
Text comparator. [See more](#comparators). 

Example: `is equal to 'something'`.


### `long-assertion`
Integer comparator. [See more](#comparators). 

Example: `is greater than or equal to 13`.


### `float-assertion`
Decimal comparator. [See more](#comparators). 

Example: `is less than or equal to 10.02`.


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


### `table`
Data table located on the next line of the step description.
[See more](https://cucumber.io/docs/gherkin/reference/#data-tables).

Example:
```gherkin
| USER  | STATE | BLOCKING_DATE |
| user1 | 2     | <null>        |
```



## Comparators

Text snippets that translate into matchers for different data types, reusable at any step. The available comparator 
types are:
- `text-assertion`
- `integer-assertion`
- `long-assertion`
- `decimal-assertion`


### `is (not) (equal to) ~x~` 
Type: numeric and text. It also supports the variants `is (equal to) ~x~ \(ignoring case\)` and
`is (equal to) ~x~ \(ignoring whitespace\)` for text type comparators and the negative version.

#### Examples
```
is 14
```
```
is equal to 22
```
```
is equal to 'algo'
```
```
is 'something'
```
```
is equal to 'somEthing' (ignoring case)
```
```
is equal to ' something ' (ignoring whitespace)
```
```
is not equal to 14
```


### `is (not) (greater|less) than ~x~`
Type: numeric. It also supports decimals and the negative version.

#### Examples
```
is greater than 14
```
```
is greater than 14.3
```
```
is less than 14
```
```
is less thane 14.3
```
```
is not greater than 14
```


### `is (not) (greater|less) or equal to ~x~`
Type: numeric. It also supports decimals and the negative version.

#### Examples
```
is greater or equal to 14
```
```
is greater or equal to 14.3
```
```
is less or equal to 14
```
```
is less or equal to 14.3
```
```
is not greater or equal to 14
```


### `(does not) start(s) with ~x~`
Type: text. It also supports the variants `starts with ~x~ \(ignoring case\)` and the negative version.

#### Examples
```
starts with 'something'
```
```
starts with 'somEthing' (ignoring case)
```
```
does not start with 'something'
```


### `(does not) end(s) with ~x~`
Type: text. It also supports the variants `ends with ~x~ \(ignoring case\)` and the negative version.

#### Examples
```
ends with 'something'
```
```
ends with 'somEthing' (ignoring case)
```
```
does not end with 'something'
```


### `(does not) contain(s) ~x~`
Type: text. It also supports the variants `contains ~x~ \(ignoring case\)` and the negative version.

#### Examples
```
contains 'something'
```
```
contains 'somEthing' (ignoring case)
```
```
does not contain 'something'
```


### `is (not) null`
Type: numeric and text. It also supports the negative version.

#### Examples
```
is null
```
```
is not null
```


### `is (not) empty`
Type: numeric and text. It also supports the negative version.

#### Examples
```
is empty
```
```
is not empty
```


### `is (not) null or empty`
Type: numeric and text. It also supports the negative version.

#### Examples
```
is null or empty
```
```
is not null or empty
```



## Dynamic properties

Wakamiti allows the use of dynamic properties to easily pass information to scenario execution using the syntax 
`${[property description]}`.

The `[property description]` depends on the type of property to be applied.


### Global property

Get the value of a global property using the syntax `${[name]}`, where `[name]` is the name of a property present in the 
Wakamiti configuration.

#### Examples
Having the following configuration in the `wakamiti.yaml` file:
```yml
wakamiti:
  resourceTypes:
    - gherkin
    
  credentials:
    username: user
    password: s3cr3t
```

And having the following step:
```gherkin
Given the service uses the oauth authentication credentials '${credentials.username}':'${credentials.password}'
```

When executed, it would resolve as:
```gherkin
Given the service uses the oauth authentication credentials 'user':'s3cr3t'
```


### Step Result property

Get the result of a previous step using the syntax `${[step number]#[xpath/jsonpath/gpath expression]}`, where 
`[step number]` is the position of the step from which you want to retrieve the result, and 
`[xpath/jsonpath/gpath expression]` is the expression to optionally retrieve specific data if the result is a complex 
object, such as a xml, json or even text.


See more about [JSONPath][jsonpath], [XPath][xpath] or [GPath][gpath].

#### Examples
Assume that step `1` returns the following:
```json
{
  "headers": {
    "Content-type": "json/application",
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
        "name": "Ana"
      }
    ]
  },
  "statusCode": 200,
  "statusLine": "HTTP/1.1 200 OK"
}
```

We have the following step:
```gherkin
Then a user identified by '${1#$.body.items[0].id}' exists in the database table USERS
```

When executed, it would resolve as:
```gherkin
Then a user identified by '4' exists in the database table USERS
```



[jsonpath]: https://goessner.net/articles/JsonPath/
[xpath]: https://en.wikipedia.org/wiki/XPath (XPath)
[gpath]: https://accenture.github.io/bdd-for-all/GPATH.html (GPath)
