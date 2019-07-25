# Kukumo


Kukumo is Cucumber-inspired automatic testing tool written in Java. 

Just like Cucumber, you can define your tests using 
natural, human-readable language by adopting (for example) the Gherkin _grammar_. However, with Kukumo you
 _do not_ bind each step to your test code; instead, steps are bound to reusable, 
common-purpose code provided by external plugins. Thus, Kukumo turns out to be a convenient tool if your aim is 
test your system via standarized accesses such as 
REST web services or JDBC connection, which tend to be a great deal of percentage of the tests written for most 
of applications. **No test code is required to be written**, so even non-programmers can define and execute 
their own tests.

Other features provided by Kukumo are:

- **Alternative launchers**: execute Kukumo as a JUnit test suite, as a Maven verify goal, or directly as 
a console command.
- **Fully internationalizable**: you can use your own language as long as you provided Kukumo with a translation file  
- **Easily extensible**: write your own plugins for any extension point (steps, reporters, language parsers, etc.) 
and share them with the community
- **Not only Gherkin**: Gherkin feature files are the initial test definition source implemented, 
but it is not internally bound to Kukumo; any plugin can provide other ways to collect the test definitions to be used


> **REMEMBER**  
> Kukumo is a _tool_, not a _testing framework_. Use it when fits the circumstances. It is not either 
a replacement for Cucumber: according your necessities, you can use _both_!    

## Usage
 
There are several aspects to consider in order to use Kukumo: 
 
### Runners

First and foremost, be aware that Kukumo can be launched using three different mechanisms, 
regarding the nature of the project.

#### JUnit runner

For Java projects, you can create an empty testing class to be run as a normal JUnit test suite, 
setting the custom JUnit runner ```KukumoJUnitRunner```. This way, the Kukumo test iti.kukumo.test.gherkin.plan is treated 
as a normal JUnit test and will be executed along any other JUnit test existing.

> **NOTE**  
> Check the **Kukumo::CORE** module for further details

#### Maven plugin

If your project uses Maven, and you prefer to execute the Kukumo test iti.kukumo.test.gherkin.plan in a separate stage,
you can execute Kukumo attaching the ```kukumo-maven-plugin``` plugin to the 
Maven lifecycle.  Simply configure the Maven plugin and the desired additional Kukumo plugins 
in your POM `build` section.


> **NOTE**  
> Check the **Kukumo::Maven Plugin** module for further details


#### Stand-alone launcher

If your project does not use neither JUnit nor Maven, Kukumo provides a stand-alone command-line
based launcher. Once installed, you can launch the test iti.kukumo.test.gherkin.plan execution simply by typing
the command `kukumo` from the folder where your test specifications are placed.

> **NOTE**  
> Check the **Kukumo::Launcher** module for further details   

### Configuration

Normally you would require to apply custom configurations to the Kukumo core process and/or some of the 
plugins used. There are alternative ways to define such configurations:

> **NOTE**  
> Check the **Configurer** module for further details 


#### Create an specific Kukumo configuration file
A very convenient way to configure your test execution is create a new file in the top folder of your application.
Different formats are accepted, including Java `.properties` files, JSON files, YAML files, etc. The default 
file name woud be `kukumo`.

For example:

###### kukumo.yaml
```yaml
kukumo:
    report: 
        generation: true
    htmlReport:
        output: target/reports/kukumo/html/kukumo-report.html
```

#### Include Kukumo configuration in an existing configuration file

If your application already has a configuration file and you prefer to keep it all together,
you may include the configuration in that file. For instance,for  a Spring Boot application that have 
an `application.yml` file, simply put the Kukumo properties there.

For example:

###### application.yml
```yaml
spring:
   application:
      name: MyApplication
   server.port: 9090
kukumo:
    report: 
        generation: true
    htmlReport:
        output: target/reports/kukumo/html/kukumo-report.html
  
``` 



> **CAUTION**   
> When readed from a file, Kukumo properties root must be the segment ```kukumo```.


## Example

Let's assume a RESTful Java project that exposes a service that allows to retrieve 
data about users. Internally, data is stored in a database table named `USER`. Given 
that, we want to test that the name of the users is retrieved correctly when using the 
REST service.

### 1. Writing the test
We could write a Gherkin specification like this:

###### users.feature
```
Feature: Testing the user data retrieval service
    Background:
        Given the database connection URL 'jdbc:h2:tcp://localhost:9092/~/test' using the user 'sa' and the password '' Ⓐ  
        And the REST service '/user'; Ⓑ 
    Escenario: Retrieve the name of a user
        Given a user identified by '3' Ⓑ 
        And the following user is inserted in the database table 'USER': Ⓐ   
        | ID | FIRST_NAME | LAST_NAME |
        | 3  | John       | Doe       |
        When the user is requested Ⓑ 
        Then the response HTTP code is 200 Ⓑ 
        And the response contains: Ⓑ 
        """
         { "firstName": "John" }
        """
```
> **Legend:**
> - Ⓐ database steps
> - Ⓑ RESTful steps

### 2. Including the dependencies
Assuming our project is built using Maven, we need to add the proper dependencies
in the POM file:

```xml
    <dependencies>
       <!-- Kukumo core functionality -->
       <dependency>
          <artifactId>iti.kukumo</artifactId>
          <groupId>kukumo-core</groupId> 
          <version>1.0.0</version>
       </dependency>
       iti.kukumo.test.gherkin.plan         
   iti.kukumo.test.gherkin.plan<dependency>
          <artifactId>iti.kukumo</artifactId>
          <groupId>kukumo-gherkin</groupId>
          <version>1.0.0</version>
       </dependency>
       <!-- Database-related steps -->
       <dependency>
          <artifactId>iti.kukumo</artifactId>
          <groupId>kukumo-db</groupId>
          <version>1.0.0</version>
       </dependency>
       <!-- REST-related steps -->
       <dependency>
          <artifactId>iti.kukumo</artifactId>
          <groupId>kukumo-rest</groupId>
          <version>1.0.0</version>
       </dependency>
    </dependencies>
``` 

### 3. Define the runner

Several alternatives at this point. The one that requires less writing would be 
create a JUnit test run by the Kukumo JUnit runner using the default configuration:

.KukumoTestPlan.java
```java
@RunWith(iti.kukumo.junit.KukumoJUnitRunner.class)
public class KukumoTestPlan {
  // no code required
}
```

## Contributing

Currently the project is closed to external contributions but this may change in the future.

## License

```
MIT License
           
Copyright (c) 2019 - Instituto Tecnológico de Informática www.iti.es

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

```

## Authors

- Luis Iñesta Gelabert  | :email: <linesta@iti.es> | :email: <luiinge@gmail.com>