# Wakamiti::JUNIT

## Overview

Wakamiti provides, as a part of the core component, a JUnit 4.x runner that can be used to execute a 
test plan built according to the received configuration. In order to do this, create a regular 
Java class annotated with `@RunWith` in order to use the Wakamiti runner instead of the by-default 
JUnit runner.

Also, you should provide a configuration using the annotation `@Configuration` (see the Wakamiti tool 
`Configurer` for further explanation of this mechanism).   
  
```java
@Configurator(properties = {
   @Property(key="resourceTypes",value="gherkin"),
   @Property(key="resourcePath", value="src/test/resources/features"),
   @Property(key="outputFilePath", value="target/wakamiti.json"),
   @Property(key="junit.treatStepsAsTests",value="false")
})
@RunWith(WakamitiJUnitRunner.class)
public class WakamitiTestPlan {
    
}
``` 
An alternative (or complementary) way to define the configuration is use an external file. The following
example would be equivalent to the one shown above.

### WakamitiTestPlan.java
```java
@Configurator(path = "classpath:wakamiti.yaml", pathPrefix = "wakamiti")
@RunWith(WakamitiJUnitRunner.class)
public class WakamitiTestPlan {
    
}
```
### wakamiti.yaml
```yaml
wakamiti:
  resourceTypes:
    - gherkin
  resourcePath: src/test/resources/features 
  outputFilePath: target/wakamiti.json
  junit:
    treatStepsAsTests: false
```

Notice that, in both cases, the test class is empty; since the test cases are discovered via the 
configuration, no code is required. In actuality, if you write methods annotated with `@Test`, `@Before` 
and `@After`, the runner will complain at execution time and abort the run in order to avoid misleading 
dead test code.

You _can_, however, annotate static methods with `@BeforeClass` and `@AfterClass`, if you require 
some code to be executed prior to or following the Wakamiti execution.


## Test case notification _vs_ step notification

The regular behaviour of the Wakamiti JUnit runner is notified to JUnit each test case result but not 
individual step results. It means that, for example, if your testing inside an IDE with JUnit integration, 
the graphical view will not mark a specific step has failed but the whole test case.

In order to provide more descriptive info, you can force Wakamiti to notify each step as a test case, 
so they will be shown in the graphical tools and reports. One drawback is that the number of tests 
would be no longer accurate since it will represent the total number of steps instead of the real 
number of test cases. Thus, you may use this option as a debugging feature when a test case has failed, 
but keep it disabled for normal executions.  



## Configuration
| Key | Accepted values | Default value | Comments
|---|---|---|---
|`junit.treatStepsAsTests`|`true`,`false`|`false`| When enabled, any step will be notified to JUnit as an individual test case 

---

## Maven dependency

```xml
<dependency>
    <groupId>es.iti.wakamiti</groupId>
    <artifactId>wakamiti-core</artifactId>
    <version>2.4.0</version>
</dependency>
```

  
### Launching Wakamiti programmatically

## Plugin development