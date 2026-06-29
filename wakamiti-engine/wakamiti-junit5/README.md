# Wakamiti JUnit 5

`wakamiti-junit5` integrates Wakamiti plans with the JUnit Platform through a
dedicated `TestEngine`. A plan is declared with an annotation:

```java
import es.iti.wakamiti.api.imconfig.AnnotatedConfiguration;
import es.iti.wakamiti.junit5.WakamitiPlan;

@WakamitiPlan
@AnnotatedConfiguration(path = "classpath:wakamiti.yaml", pathPrefix = "wakamiti")
public class WakamitiTestPlan {
}
```

## Maven dependency

```xml
<dependency>
    <groupId>es.iti.wakamiti</groupId>
    <artifactId>wakamiti-junit5</artifactId>
    <version>{version}</version>
    <scope>test</scope>
</dependency>
```

> The engine is registered through the JUnit Platform `TestEngine` service
> (`org.junit.platform.engine.TestEngine`), so it is picked up automatically by
> the platform launcher, IDEs and a JUnit Platform aware Surefire/Failsafe
> (3.x).

## Execution model

- A custom `WakamitiTestEngine` discovers every concrete class annotated with
  `@WakamitiJUnit5TestPlan` and builds the plan during discovery.
- Each plan node (feature, scenario, step) is published as a platform
  descriptor carrying a real `TestSource` (the originating feature file and
  line), which enables IDE "jump to source" navigation to the failing scenario
  or step.
- Static `@BeforeAll` and `@AfterAll` methods declared on the plan class are
  invoked, respectively, before and after the plan execution.
- By default, test cases are visible as test nodes.
- `junit.treatStepsAsTests=true` switches visibility to step-level nodes.

## Profile-based execution

- `wakamiti.junit5.profile` preferred
- `wakamiti.profile` fallback

Strict mode:

- `wakamiti.junit5.profile.strict=true`
- `wakamiti.profile.strict=true`
