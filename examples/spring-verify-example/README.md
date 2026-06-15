# Spring verify example

This example shows how to run Wakamiti from the Maven lifecycle while a Spring Boot application is started for the integration-test phase.

## What the project demonstrates

- `spring-boot-maven-plugin` starts and stops the sample application
- `wakamiti-maven-plugin:verify` runs during `integration-test`
- Wakamiti plugins are attached as dependencies of the Maven plugin
- the HTML report is generated from the same Maven execution

## Relevant files

- `pom.xml`: Maven lifecycle wiring
- `src/test/resources/wakamiti.yaml`: Wakamiti configuration
- `src/test/resources/UserService.feature`: sample feature file

## Key Maven configuration

The current project uses these artifact names:

- `es.iti.wakamiti:wakamiti-maven-plugin`
- `es.iti.wakamiti:rest-wakamiti-plugin`
- `es.iti.wakamiti:db-wakamiti-plugin`
- `es.iti.wakamiti:html-report-wakamiti-plugin`

The example binds `wakamiti-maven-plugin:verify` to `integration-test` and points it to `src/test/resources/wakamiti.yaml` through `configurationFiles`.

## Run

From this directory:

```bash
mvn verify
```

To skip the example tests:

```bash
mvn verify -DskipExampleTests=true
```

## Output

The example writes its Wakamiti outputs under `target/wakamiti/`, including:

- `wakamiti.json`
- `wakamiti.html`
