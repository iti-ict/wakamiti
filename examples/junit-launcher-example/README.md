# JUnit launcher example (without Spring)

Este ejemplo muestra como ejecutar Wakamiti con el launcher `wakamiti-junit` en un proyecto Maven sin Spring.

## Que incluye

- Clase de test vacia con `@RunWith(WakamitiJUnitRunner.class)`.
- Configuracion en `src/test/resources/wakamiti-test.yaml`.
- Un `.feature` con pasos `groovy-steps` para validar el launcher.
- Un ejemplo de pasos custom Java (`CustomJavaSteps`) registrados con `wakamiti.nonRegisteredStepProviders`.
- Un paso custom de post-ejecucion con formato `Al finalizar, ...` que encola acciones y las ejecuta en `@TearDown`.

## Estructura del ejemplo custom Java

- Step contributor:
  `src/test/java/es/iti/wakamiti/examples/junit/launcher/CustomJavaSteps.java`
- Textos de pasos:
  `src/test/resources/steps/custom-java-steps_en.properties`
- Feature:
  `src/test/resources/features/custom-java.feature`

## Ejecutar

Desde este directorio:

```bash
mvn test
```

Al finalizar, el resultado de la ejecucion se genera en:

`target/wakamiti/wakamiti.json`
