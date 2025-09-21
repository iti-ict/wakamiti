

![](images/logo_wakamiti_bright.svg)

---

[![Sonar Quality Gate](https://img.shields.io/sonar/quality_gate/iti-ict_kukumo?server=https%3A%2F%2Fsonarcloud.io)](https://sonarcloud.io/project/overview?id=iti-ict_kukumo) 
[![Sonar Tests](https://img.shields.io/sonar/tests/iti-ict_kukumo?server=https%3A%2F%2Fsonarcloud.io)](https://sonarcloud.io/component_measures?metric=tests&view=list&id=iti-ict_kukumo) 
[![Sonar Coverage](https://img.shields.io/sonar/coverage/iti-ict_kukumo?server=https%3A%2F%2Fsonarcloud.io)](https://sonarcloud.io/component_measures?metric=Coverage&view=list&id=iti-ict_kukumo) 
[![Sonar Technical Debt](https://img.shields.io/sonar/tech_debt/iti-ict_kukumo?server=https%3A%2F%2Fsonarcloud.io)](https://sonarcloud.io/component_measures?metric=sqale_index&view=list&id=iti-ict_kukumo) 
[![Docker Image Version](https://img.shields.io/docker/v/wakamiti/wakamiti?label=docker&logo=docker)](https://hub.docker.com/r/wakamiti/wakamiti) 
[![Maven Central Version](https://img.shields.io/maven-central/v/es.iti.wakamiti/wakamiti-engine?logo=circle&logoColor=red)](https://mvnrepository.com/search?q=wakamiti)

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/iti-ict/wakamiti)



> Please check the [site version](https://iti-ict.github.io/wakamiti/) in case you are reading this document directly 
> from the repository.



### Latest plugin versions

| artifact                                            | version                                                                                                                                                                                                                         |
|-----------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `es.iti.wakamiti:rest-wakamiti-plugin`              | [![Maven Central](https://img.shields.io/maven-central/v/es.iti.wakamiti/rest-wakamiti-plugin?label=%20&color=white)](https://mvnrepository.com/artifact/es.iti.wakamiti/rest-wakamiti-plugin/latest)                           |
| `es.iti.wakamiti:db-wakamiti-plugin`                | [![Maven Central](https://img.shields.io/maven-central/v/es.iti.wakamiti/db-wakamiti-plugin?label=%20&color=white)](https://mvnrepository.com/artifact/es.iti.wakamiti/db-wakamiti-plugin/latest)                               |
| `es.iti.wakamiti:html-report-wakamiti-plugin`       | [![Maven Central](https://img.shields.io/maven-central/v/es.iti.wakamiti/html-report-wakamiti-plugin?label=%20&color=white)](https://mvnrepository.com/artifact/es.iti.wakamiti/html-report-wakamiti-plugin/latest)             |
| `es.iti.wakamiti:cucumber-exporter-wakamiti-plugin` | [![Maven Central](https://img.shields.io/maven-central/v/es.iti.wakamiti/cucumber-exporter-wakamiti-plugin?label=%20&color=white)](https://mvnrepository.com/artifact/es.iti.wakamiti/cucumber-exporter-wakamiti-plugin/latest) |
| `es.iti.wakamiti:file-uploader-wakamiti-plugin`     | [![Maven Central](https://img.shields.io/maven-central/v/es.iti.wakamiti/file-uploader-wakamiti-plugin?label=%20&color=white)](https://mvnrepository.com/artifact/es.iti.wakamiti/file-uploader-wakamiti-plugin/latest)         |
| `es.iti.wakamiti:amqp-wakamiti-plugin`              | [![Maven Central](https://img.shields.io/maven-central/v/es.iti.wakamiti/amqp-wakamiti-plugin?label=%20&color=white)](https://mvnrepository.com/artifact/es.iti.wakamiti/amqp-wakamiti-plugin/latest)                           |
| `es.iti.wakamiti:groovy-wakamiti-plugin`            | [![Maven Central](https://img.shields.io/maven-central/v/es.iti.wakamiti/groovy-wakamiti-plugin?label=%20&color=white)](https://mvnrepository.com/artifact/es.iti.wakamiti/groovy-wakamiti-plugin/latest)                       |
| `es.iti.wakamiti:azure-wakamiti-plugin`             | [![Maven Central](https://img.shields.io/maven-central/v/es.iti.wakamiti/azure-wakamiti-plugin?label=%20&color=white)](https://mvnrepository.com/artifact/es.iti.wakamiti/azure-wakamiti-plugin/latest)                         |
| `es.iti.wakamiti:appium-wakamiti-plugin`            | [![Maven Central](https://img.shields.io/maven-central/v/es.iti.wakamiti/appium-wakamiti-plugin?label=%20&color=white)](https://mvnrepository.com/artifact/es.iti.wakamiti/appium-wakamiti-plugin/latest)                       |
| `es.iti.wakamiti:email-wakamiti-plugin`             | [![Maven Central](https://img.shields.io/maven-central/v/es.iti.wakamiti/email-wakamiti-plugin?label=%20&color=white)](https://mvnrepository.com/artifact/es.iti.wakamiti/email-wakamiti-plugin/latest)                         |
| `es.iti.wakamiti:io-wakamiti-plugin`                | [![Maven Central](https://img.shields.io/maven-central/v/es.iti.wakamiti/io-wakamiti-plugin?label=%20&color=white)](https://mvnrepository.com/artifact/es.iti.wakamiti/io-wakamiti-plugin/latest)                               |
| `es.iti.wakamiti:jmeter-wakamiti-plugin`            | [![Maven Central](https://img.shields.io/maven-central/v/es.iti.wakamiti/jmeter-wakamiti-plugin?label=%20&color=white)](https://mvnrepository.com/artifact/es.iti.wakamiti/jmeter-wakamiti-plugin/latest)                       |
| `es.iti.wakamiti:jacoco-wakamiti-plugin`            | [![Maven Central](https://img.shields.io/maven-central/v/es.iti.wakamiti/jacoco-wakamiti-plugin?label=%20&color=white)](https://mvnrepository.com/artifact/es.iti.wakamiti/jacoco-wakamiti-plugin/latest)                       |

---


Usage
----------------------------------------------------------------------------------------------------
 
Running tests with Wakamiti is straightforward. Write your test specification, define a basic configuration with the 
step providers (and any other plugins) that you require, and choose the launcher that best suits your project.

Let's assume a RESTful application that exposes a service that allows to retrieve data about users. Internally, data is 
stored in a database table named `USER`. Given that, we want to test that the name of the users is retrieved correctly 
when using the REST service.

The following Gherkin specification defines a test that uses steps from two different plugins, `REST steps` and 
`Database steps` (see the documentation of each plugin for further explanation).

###### users.feature
```gherkin
Feature: Testing the user data retrieval service

    Background:
        Given the database connection URL 'jdbc:h2:tcp://localhost:9092/~/test' using the user 'sa' and the password ''
        And the base URL http://localhost:9191
        And the REST service '/users'
    
    Scenario: Retrieve the name of a user
        Given a user identified by '3' 
        And the following user is inserted in the database table USER:
        | ID | FIRST_NAME | LAST_NAME |
        | 3  | John       | Doe       |
        When the user is requested 
        Then the response HTTP code is 200 
        And the response contains:
        """
         { "firstName": "John" }
        """
```

Now we define the execution configuration, which is simply a set of properties that Wakamiti would take into account for 
various aspects. There are several ways to define them; in this example we will just write them into the default 
configuration file `wakamiti.yaml`:

```yaml
# wakamiti.yaml
wakamiti:
    resourceTypes: 
      - gherkin
    modules:
      - es.iti.wakamiti:wakamiti-rest:2.3.3
      - es.iti.wakamiti:wakamiti-db:2.3.3
      - es.iti.wakamiti:wakamiti-html-report:2.3.3
      - com.h2database:h2:1.4.199
    htmlReport:
        output: target/wakamiti-report.html
``` 

> In addition to the core functionality properties, each plugin can make use of its own set of properties. These are all
> described in their own documentation.

Once you have configured the test execution, all that remains is to execute the test plan. There are several ways of 
launching it (see `Runners` below); in this example, we will use the console command launcher. Assuming it is installed 
correctly, you need to type:

```shell
wakamiti
```

and the test will be executed.

<br/>

<img src="images/wakamiti-run.png" width="600">

<br/>


The results of the test execution are stored in an output file, named `wakamiti.json` by default, in addition to the 
console output. Additional plugins can read this file and generate reports or perform other post-execution tasks.

## Runners

There are currently three methods of initiating a test plan, depending on the type of project and test approach:

- **Using JUnit**: For Java projects, you can create an empty test class to be run as a normal JUnit test suite, 
setting the custom JUnit runner `WakamitiJUnitRunner`. This way, the plan will be treated as a normal JUnit test and 
will be executed along with any other existing JUnit test.

    _Suitable scenarios_   
   Java applications where you need to integrate reports with other JUnit tests, or use an IDE to run tests.


- **Using Maven**: If your project uses Maven, and you prefer to run the Wakamiti test plan in a separate phase, you can 
run Wakamiti by attaching the `wakamiti-maven-plugin` plugin to the Maven lifecycle. Configure the Maven plugin and any 
additional Wakamiti plugins you want in your POM `build` section.

   _Suitable scenarios_   
   Maven-built applications where you want to include Wakamiti as part of the verify phase


- **Stand-alone launcher**: If your project does not use neither JUnit nor Maven, Wakamiti provides a standalone 
command-line launcher that manages the plugin dependencies internally.

   _Suitable scenarios_   
   Non-Java applications and/or your test plan needs to be executed outside the build process.
   
     


Plugins
----------------------------------------------------------------------------------------------------

Wakamiti itself does not have defined steps, but they are included through various plugins. Currently, 
[the following](https://iti-ict.github.io/wakamiti/en/plugins) are available, but you can also develop your own plugins!




Wakamiti Editor
----------------------------------------------------------------------------------------------------

Wakamiti implements the [Language Server Protocol](https://microsoft.github.io/language-server-protocol/)
in order to allow editors to provide auto-completion and validation features. As an example, a 
[Visual Studio Code](https://code.visualstudio.com/) extension is provided 
[here](https://github.com/iti-ict/wakamiti/raw/main/wakamiti-vscode-extension/wakamiti-vscode-extension-latest.vsix).
See [this section](xxx) to learn more about it.



Contributing
----------------------------------------------------------------------------------------------------

There are several ways in which you can contribute to this project:

### Reporting a defect / requesting a new feature

If you have found a potential bug, or there is a missing feature that you really would need, feel free to open a new 
issue on the [Github page](https://github.com/iti-ict/wakamiti/issues). Please check before that a similar
issue has not already been reported.


### Fixing a bug / implementing a feature

If there is an [open issue](https://github.com/iti-ict/wakamiti/issues) that you want to implement,
follow these steps:
1. Fork the [source code](https://github.com/iti-ict/wakamiti).
2. Create a new branch in the forked repository.
3. Commit the changes.
4. Open a pull request that clearly states the issue that you are attempting to solve.
5. Your pull request will be reviewed and either accepted or rejected. In the latter case, you will be given enough 
feedback to make changes and resubmit the request.

> Feel free to engage in a **friendly** discussion if you disagree with the feedback given. 
> Any rude or inappropriate form of communication will be automatically rejected.


Acknowledgements
----------------------------------------------------------------------------------------------------

This software has been developed as a part of the Plan of Non-Economical Activities of 
**Instituto Tecnológico de Informática (ITI)** for the year 2021, funded by 
**Institut Valencià de Competitivitat Empresarial (IVACE)** and **Generalitat Valenciana**,
by means of the colaboration agreement between IVACE and ITI aimed to enhance their activity 
and capabilities of developing excellence in the matter of independant R&D, spreading 
the results of conducted researches, and driving knowledge transfer among companies from the 
*Comunitat Valenciana*. 


<img src="images/iti.png" align="right">
<img src="images/gva-ivace.png">


License
----------------------------------------------------------------------------------------------------

```
Mozilla Public License 2.0

This Source Code Form is subject to the terms of the Mozilla Public
License, v. 2.0. If a copy of the MPL was not distributed with this
file, You can obtain one at https://mozilla.org/MPL/2.0/.
```



Authors
----------------------------------------------------------------------------------------------------

- **María Galbis Calomarde** - mgalbis@iti.es 
- **Carlos Oliva Miñana** - coliva@iti.es
    


