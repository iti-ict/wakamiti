---
title: Getting started
date: 2022-09-20
slug: /en/introduction/getting-started
---

### 1. Configure Wakamiti in the project
In order to configure Wakamiti, a configuration file is needed, which will be placed in the 
root directory of the folder where the tests are located.
To keep the tests organized, you'll need a folder structure, for example:
```
> src
> wakamiti
    > data
    > features
    > steps
    > results
    wakamiti.yaml
```
This is the basic configuration of the yaml file to be able to run the tests:
```
wakamiti:
  resourceTypes:
    - gherkin
  tagFilter:  
  log:
    level: debug
  launcher:
    modules:
      - mysql:mysql-connector-java:8.0.28
      - es.iti.wakamiti:wakamiti-rest:2.0.0
      - es.iti.wakamiti:wakamiti-db:2.0.0
      - es.iti.wakamiti:wakamiti-html-report:2.0.0
  outputFilePath: result/wakamiti.json
  htmlReport:
    title: Test
    output: result/ResultTest.html
  rest:
    baseURL: https://localhost
  database:
    connection:
      url: jdbc:mysql://localhost:3306
      username: user
      password: p4ssw0rd
```
[Consult other configuration options](setup/configuration.en)

### 2. Develop a test case
Once the configuration is done and the folder structure is clear, you can start developing the tests.
Within each feature, several scenarios can be defined to cover the functionality to be tested.
There must be at least one scenario per feature, if there are no scenarios defined, Wakamiti will interpret the feature as "not implemented" and it will be shown as such.
feature as "not implemented", and it will be shown in the final report.
The feature is organised as the Gherkin structure, for more information, you can consult the [Gherkin](https://cucumber.io/docs/gherkin/)
website. 
````
Feature: Get the pets owners
-------------------------------------------------
    Background:
        Given the REST service '/owners'

   @ID-ownerExist
    Scenario: An existing owner is consulted
        Given a user identified by '20'
        And the following users are inserted into the database table owners:
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
````
### 3. Ejecutar los tests
The tests are run from the directory containing the Wakamiti features with the following command:
* Windows:
```Shell
docker run --rm -it -v "%cd%:/wakamiti" wakamiti/wakamiti
```
* Linux:
```Shell
docker run --rm -v "$(pwd):/wakamiti" wakamiti/wakamiti
```
With this command, you will download the latest version of Wakamiti. To work with a specific version, 
you should specify it in the Docker command as follows: ```wakamiti/wakamiti:version```, You can view the available 
versions in the [Wakamiti dockerhub](https://hub.docker.com/r/wakamiti/wakamiti/tags) repository.


### 4.Reports
Once the tests are executed, the results are generated in two formats: .json and .html. These files will be located in the directory 
specified in the  ```outputFilePath``` y ``` htmlReport```  sections of the configuration file.

The current states available in Wakamiti are:

- <span style="color:#32cd32">PASSED</span> : The test case is correct; the system returns the expected result.
- <span style="color:#f08080">FAILED</span> : There is a validation error; what the system returns doesn't match the expected result.
- <span style="color:#808080">SKIPPED</span> : The test case was not executed.
- <span style="color:#ff0000">ERROR</span> : There is a system error, such as a database connection error, duplicate key error, or a timeout error.
- <span style="color:#ffd700">UNDEFINED</span> : The step doesn't exist in Wakamiti.
- <span style="color:#87cefa"> NOT IMPLEMENTED</span>: The feature exists, but there is no defined test case for it.