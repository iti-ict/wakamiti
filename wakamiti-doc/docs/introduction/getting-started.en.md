---
title: Getting started
date: 2022-09-20
slug: /en/introduction/getting-started
---

In this quick tutorial you will learn how to:
- Write the basic configuration.
- Write a scenario.
- Run wakamiti.
- Learn the basic workflow.

Please be aware that this tutorial assumes that you have a:
- Some experience using a terminal.
- Some experience using a text editor.
- Basic understanding of `gherkin` syntax.

Before we begin, you will need the following:
- Install and run [Docker](https://www.docker.com/get-started/).
- Install an IDE, like [IntelliJ IDEA](https://www.jetbrains.com/idea/) or [VS Code](https://code.visualstudio.com/).
- The source code of [this tutorial](https://minhaskamal.github.io/DownGit/#/home?url=https://github.com/iti-ict/wakamiti/tree/main/examples/tutorial).

### 1. Wakamiti configuration
Wakamiti configuration is done by means of a `yaml` file that will be placed in the same directory where the tests are 
located, for example:
```
.
├── features
│   └── example.feature
└── wakamiti.yaml
```

This is the basic configuration to be able to run the tests:
```yml
wakamiti:
  resourceTypes:
    - gherkin
  launcher:
    modules:
      - mysql:mysql-connector-java:8.0.28
      - es.iti.wakamiti:rest-wakamiti-plugin:2.2.1
      - es.iti.wakamiti:db-wakamiti-plugin:2.2.2
      - es.iti.wakamiti:html-report-wakamiti-plugin:2.2.1
  htmlReport:
    title: Test
  rest:
    baseURL: https://localhost
  database:
    connection:
      url: jdbc:mysql://localhost:3306
      username: user
      password: p4ssw0rd
```
> **NOTE** <br />
> Note that each plugin has its own configuration, which you can check in [their respective sections](en/plugins).
> You can also check other options in [global configuration](en/wakamiti/architecture#global-configuration).


### 2. Scenario definition
When we do *Behaviour-Driven Development* we use concrete examples to specify what we want the software to do. 
Scenarios are written before production code. They start their life as an executable specification. As the 
production code emerges, scenarios take on a role as living documentation and automated tests.

A scenario belongs to a specific software feature. Each feature can contain many scenarios, and are defined in `.feature` 
files that must be in our working directory (or subdirectory).

A concrete example would be to get a pet owner.

Create an empty file named `example.feature` with the following content:
```gherkin
Feature: Get the pets owners

  Scenario: An existing owner is consulted
    Given el servicio REST '/owners/{id}'
    And the path parameter 'id' with value '20'
    And the following user is inserted into the database table owners:
      | ID  | FIRST_NAME | LAST_NAME      |
      | 20  | Pepe       | Perez Martínez |
    When the user is requested
    Then the response HTTP code is equals to 200
    And the response is:
      """json
      {
        "id": 20,
        "firstName": "Pepe",
        "lastName": "Perez Martínez"
      }
      """
```
The first line of this file starts with the keyword `Feature:` followed by a name. It is a good idea to use a name 
similar to the file name.

The third line, `Scenario: An existing owner is queried`, is a scenario, which is a concrete example that illustrates 
how the software should behave.

The rest of the lines starting with `Given`, `When`, `Then`, `And` are the steps of our scenario, and are what Wakamiti 
will execute.

[See more](https://cucumber.io/docs/gherkin/) in detail the `gherkin` syntax.

### 3. Run Wakamiti
The tests are run from the directory containing the Wakamiti features with the following command:

* Windows:
```Shell
docker run --rm -v "%cd%:/wakamiti" wakamiti/wakamiti
```
* Linux:
```Shell
docker run --rm -v "$(pwd):/wakamiti" wakamiti/wakamiti
```
With this command, you will download the latest version of Wakamiti. To work with a specific version, 
you should specify it in the Docker command as follows: `wakamiti/wakamiti:version`, You can view the available 
versions in the [Wakamiti dockerhub](https://hub.docker.com/r/wakamiti/wakamiti/tags) repository.


### 4.Reports
Once the tests are executed, the results are generated in two formats: `wakamiti.json` and `wakamiti.html`.

The current states available in Wakamiti are:

- <span style="color:#5fc95f">**PASSED**</span>: test case is correct, the same result as expected is received from the 
  system.
- <span style="color:#4fc3f7">**NOT IMPLEMENTED**</span>: test case exists, but its steps are not defined.
- <span style="color:#9e9e9e">**SKIPPED**</span>: test case has not been executed.
- <span style="color:#ffc107">**UNDEFINED**</span>: there is no such step in Wakamiti.
- <span style="color:#ff7b7e">**FAILED**</span>: there is a check error, it does not match what is expected from what the 
  system returns.
- <span style="color:#ff0000">**ERROR**</span>: there is an unexpected error in the system (connection error, database error, 
  time out error...).
