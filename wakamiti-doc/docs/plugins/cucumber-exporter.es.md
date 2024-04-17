---
title: Informe Cucumber 
date: 2022-09-20
slug: /plugins/cucumber-exporter
---


Un generador de informes que emite un fichero JSON con el formato utilizado por Cucumber. De esta forma, se reutilizan 
algunas herramientas diseñadas para funcionar con Cucumber, por ejemplo este
[Complemento de informes de Jenkins Cucumber](https://github.com/jenkinsci/cucumber-reports-plugin).

Hay que tener en cuenta que, aunque Cucumber y Wakamiti comparten similitudes estructurales en su formato de resultado, 
hay partes específicas de información que no son intercambiables. Por ejemplo, Wakamiti trabaja con planes de prueba de 
profundidad ilimitada, mientras que Cucumber espera una estructura fija de tres niveles. Por lo tanto, el fichero JSON 
de Cucumber exportado podría no ser una representación fiel del plan de prueba ejecutado.

---
## Tabla de contenido

---


## Instalación


Incluye el módulo en la sección correspondiente.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:cucumber-exporter-wakamiti-plugin:2.4.0
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>cucumber-exporter-wakamiti-plugin</artifactId>
  <version>2.4.0</version>
</dependency>
```


## Configuración


###  `cucumberExporter.outputFile`
- Tipo: `file`
- Por defecto: `cucumber-report.json`

La ruta (relativa) y el nombre del archivo generado.

Ejemplo:
```yaml
cucumberExporter:
  outputFile: my-cucumber-report.json
```


### `cucumberExporter.multiLevelStrategy`
- Tipo: `string`
- Por defecto: `innerSteps`

Establece la estrategia de mapeo utilizada cuando el plan de Wakamiti tiene más niveles del esperado por Cucumber. Los 
valores aceptados son:
- `innerSteps`: Solo se incluirán los pasos de implementación, descartando los de definición.
- `outerSteps`: Solo se incluirán los pasos de definición, descartando los de implementación.

Ejemplo:
```yaml
cucumberExporter:
  multiLevelStrategy: outerSteps
```
