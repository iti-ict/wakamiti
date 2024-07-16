---
title: Groovy steps
date: 2023-01-20
slug: /en/plugins/groovy
---


This plugin provides a `groovy` class compiler and steps for `groovy` code execution.


---
## Tabla de contenido

---


## Install


Include the module in the corresponding section.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:groovy-wakamiti-plugin:2.6.0
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>groovy-wakamiti-plugin</artifactId>
  <version>2.6.0</version>
</dependency>
```


## Compiler

The groovy compiler will attempt to compile any file with `.groovy` extension present in the working directory (or 
subdirectory). All libraries used in `.groovy` files must be included in the Wakamiti core, or in the configuration 
[`wakamiti.launcher.modules`](en/wakamiti/architecture#wakamitilaunchermodules).

These `groovy` files can be used as a provider of additional steps.

#### Examples:

We create a file called `custom-steps.properties` where we define a new step:
```properties copy=true
custom.step=the user's password {name:text} is retrieved
```

We create a file called `CustomSteps.goovy` where we develop the new step:
```groovy copy=true
package example

import es.iti.wakamiti.api.annotations.I18nResource
import es.iti.wakamiti.api.annotations.Step
import es.iti.wakamiti.api.util.WakamitiLogger
import es.iti.wakamiti.api.imconfig.Configurable
import es.iti.wakamiti.api.imconfig.Configuration
import org.slf4j.Logger

@I18nResource("custom-steps")
class CustomSteps implements StepContributor, Configurable {

  private static Logger log = WakamitiLogger.forName("es.iti.wakamiti.example");
  private String username
  private String password
  
  @Override
  void configure(Configuration configuration) {
    username = configuration.get("credentials.username", String.class).orElse(null)
    password = configuration.get("credentials.password", String.class).orElse(null)
  }

  @Step(value = "custom.step", args = ["name:text"])
  def customStep(String name) {
    if (name == username) {
      log.info("Hello, {}! Your password is {}", name, password)
      return password
    }
  }
}
```

We include this new class in the configuration 
[`wakamiti.nonRegisteredStepProviders`](en/wakamiti/architecture#wakamitinonRegisteredStepProviders) of Wakamiti, and add the 
properties with the sample credentials:
```yml
  nonRegisteredStepProviders:
    - example.CustomSteps
  credentials:
    username: user
    password: s3cr3t
```

We create an `example.feature` file with the custom step:
```gherkin
Feature: Custom steps example
  Scenario: Test
    When the user's password 'user' is retrieved
```

When executed, it would be shown in the log:
```
[e.i.w.c.r.PlanNodeLogger.logStepResult]   INFO -  [ PASSED ]  When the user's password 'user' is retrieved (0.011) 
[e.i.w.example.CustomSteps.customStep]   INFO - Hello, user! Your password is s3cr3t
```


## Steps


### Execute code
```text copy=true
(that) the following groovy code is executed:
    {data}
```
Runs the given groovy script with the available variables:
- `ctx`: Context of the scenario. This is a container with the `id` of the scenario, the results of the different steps, 
  and any other variables you add.
- `log`: Wakamiti logger to debug the script.

#### Parameters:
| Name   | Wakamiti type         | Description    |
|--------|-----------------------|----------------|
| `data` | `document` *required* | Script content |

#### Examples:
```gherkin
@ID-01
Scenario: Example
  When the following groovy code is executed:
    """groovy
    ctx['a'] = 'something'
    1+1
    """
  And the following groovy code is executed:
    """groovy
    log.debug("Context: {}", ctx)
    assert ctx.results[0] == 2
    assert ctx.a == 'something'
    assert ctx.id == 'ID-01'
    """
```


## Dynamic properties

### Groovy property
Get the result of executing a line of groovy code, using the syntax `${=[expression]}`, where `[expression]` is the 
groovy code to be executed. This expression also includes the variable `ctx`.

#### Examples:
We have the following scenario:
```gherkin
@ID-01
Scenario: Example
  When the SQL script file 'data/${=ctx.id}/script-${=new Date().format("yyyyMMdd")}.sql' is executed
```

Assuming today is `2023-09-20`, when executed, it would resolve as:
```gherkin
@ID-01
Scenario: Example
  When the SQL script file 'data/ID-01/script-20230920.sql' is executed
```
