---
title: Informe Cucumber 
date: 2022-09-20
slug: plugins/cucumber-exporter
---

Un generador de informes que emite un fichero JSON con el formato utilizado por Cucumber. De esta forma, se reutilizan 
algunas herramientas diseñadas para funcionar con Cucumber, por ejemplo este
[Complemento de informes de Jenkins Cucumber](https://github.com/jenkinsci/cucumber-reports-plugin).

Hay que tener en cuenta que, aunque Cucumber y Kukumo comparten similitudes estructurales en su formato de resultado, 
hay partes específicas de información que no son intercambiables. Por ejemplo, Kukumo trabaja con planes de prueba de 
profundidad ilimitada, mientras que Cucumber espera una estructura fija de tres niveles. Por lo tanto, el fichero JSON 
de Cucumber exportado podría no ser una representación fiel del plan de prueba ejecutado.

## Configuración

---
####  `cucumberExporter.outputFile`
La ruta (relativa) y el nombre del archivo generado.

El valor por defecto es `cucumber-report.json`.

Ejemplo:

```yaml
cucumberExporter:
  outputFile: my-cucumber-report.json
```

---
#### `cucumberExporter.multiLevelStrategy`
Establece la estrategia de mapeo utilizada cuando el plan de Kukumo tiene más niveles del esperado por Cucumber. Los 
valores aceptados son:
- `innerSteps`: Sólo se incluirán los pasos de implementación, descartando los de definición.
- `outerSteps`: Sólo se incluirán los pasos de definición, descartando los de implementación.

El valor por defecto es `innerSteps`.

Ejemplo:

```yaml
cucumberExporter:
  multiLevelStrategy: outerSteps
```