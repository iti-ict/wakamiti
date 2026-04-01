# JUnit launcher example

This example shows how to run Wakamiti with the `wakamiti-junit` launcher in a Maven project.

## What it includes

- Empty test class with `@RunWith(WakamitiJUnitRunner.class)`.
- Configuration in `src/test/resources/wakamiti-test.yaml`.
- An example of custom Java steps (`CustomJavaSteps`) registered with `wakamiti.nonRegisteredStepProviders`.

## Custom Java example structure

- Step contributor:
  `src/test/java/es/iti/wakamiti/examples/junit/launcher/CustomJavaSteps.java`
- Step texts:
  `src/test/resources/steps/custom-java-steps.properties`
  `src/test/resources/steps/custom-java-steps_es.properties`
- Feature:
  `src/test/resources/features/custom-java.feature`

## Run

From this directory:

```bash
mvn test
```

After execution, the result is generated at:

`target/wakamiti/wakamiti.json`
