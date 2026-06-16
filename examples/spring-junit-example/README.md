# Spring JUnit example

This module is a Spring Boot sample application exercised through `wakamiti-junit`.

## What it includes

- REST API over `User`
- embedded H2 database
- custom runner `WakamitiSpringJUnitRunner` that bootstraps Spring before Wakamiti
- Wakamiti configuration in `src/test/resources/application-test.yaml`

## Default endpoints used by the example

- HTTP base URL: `http://localhost:9191/`
- H2 TCP URL: `jdbc:h2:tcp://localhost:9092/mem:test`

The ports come from `application-test.yaml`, which is the configuration actually used by the tests.

## Run

From this directory:

```bash
mvn test
```

## Relevant files

- `src/test/java/.../WakamitiTest.java`
- `src/test/java/.../WakamitiSpringJUnitRunner.java`
- `src/test/resources/application-test.yaml`
- `src/test/resources/UserService.feature`
