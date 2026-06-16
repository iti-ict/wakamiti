# Wakamiti JUnit

`wakamiti-junit` integrates Wakamiti plans with JUnit 4 by exposing `es.iti.wakamiti.junit.WakamitiJUnitRunner`.

## Maven dependency

```xml
<dependency>
    <groupId>es.iti.wakamiti</groupId>
    <artifactId>wakamiti-junit</artifactId>
    <version>{version}</version>
    <scope>test</scope>
</dependency>
```

## Basic runner

```java
import es.iti.wakamiti.api.imconfig.AnnotatedConfiguration;
import es.iti.wakamiti.junit.WakamitiJUnitRunner;
import org.junit.runner.RunWith;

@RunWith(WakamitiJUnitRunner.class)
@AnnotatedConfiguration(path = "classpath:wakamiti.yaml", pathPrefix = "wakamiti")
public class WakamitiTestPlan {
}
```

The runner class must stay empty. Wakamiti discovers the plan from the supplied configuration rather than from `@Test` methods.

## Inline configuration

You can also define the configuration directly on the class:

```java
import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.imconfig.AnnotatedConfiguration;
import es.iti.wakamiti.api.imconfig.Property;
import es.iti.wakamiti.core.gherkin.parser.GherkinResourceType;

@RunWith(WakamitiJUnitRunner.class)
@AnnotatedConfiguration({
    @Property(key = WakamitiConfiguration.RESOURCE_TYPES, value = GherkinResourceType.NAME),
    @Property(key = WakamitiConfiguration.RESOURCE_PATH, value = "src/test/resources/features"),
    @Property(key = WakamitiConfiguration.OUTPUT_FILE_PATH, value = "target/wakamiti/wakamiti.json"),
    @Property(key = "junit.treatStepsAsTests", value = "false")
})
public class WakamitiInlineConfigurationTestPlan {
}
```

## Example `wakamiti.yaml`

```yaml
wakamiti:
  resourceTypes:
    - gherkin
  resourcePath: src/test/resources/features
  outputFilePath: target/wakamiti/wakamiti.json
  junit:
    treatStepsAsTests: false
```

## JUnit-specific behaviour

- instance methods annotated with `@Test`, `@Before` or `@After` are not supported on the runner class
- static `@BeforeClass` and `@AfterClass` hooks are allowed
- `junit.treatStepsAsTests=true` makes each step visible to JUnit as an individual test node, which is useful for debugging but changes the reported test count

## Profile-based execution

`@Profile` can be used to activate or skip runner classes depending on system properties:

- `wakamiti.junit.profile` preferred
- `wakamiti.profile` fallback

Strict mode is controlled by:

- `wakamiti.junit.profile.strict=true`
- `wakamiti.profile.strict=true`

## Example module

See [examples/junit-launcher-example/README.md](../../examples/junit-launcher-example/README.md) and [examples/spring-junit-example/README.md](../../examples/spring-junit-example/README.md).
