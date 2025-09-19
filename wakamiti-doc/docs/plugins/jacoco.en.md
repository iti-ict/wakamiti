---
title: Cucumber exporter
date: 2022-09-20
slug: /en/plugins/cucumber-exporter
---


A report generator that outputs a JSON file in the format used by Cucumber. This reuses some of the tools designed to 
work with Cucumber, such as this [Jenkins Cucumber report plugin](https://github.com/jenkinsci/cucumber-reports-plugin).

Note that while Cucumber and Wakamiti have structural similarities in their output format, there is specific information 
that is not interchangeable. For example, Wakamiti works with unlimited depth test plans, whereas Cucumber expects a 
fixed three-level structure. Therefore, the exported Cucumber JSON file may not be a faithful representation of the 
executed test plan.

---
## Tabla de contenido

---


## Install


Include the module in the corresponding section.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:cucumber-exporter-wakamiti-plugin:2.6.0
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>cucumber-exporter-wakamiti-plugin</artifactId>
  <version>2.6.0</version>
</dependency>
```


## Options


###  `cucumberExporter.outputFile`
- Type: `file` 
- Default: `cucumber-report.json`

The (relative) path and name of the generated file.

Example:
```yaml
cucumberExporter:
  outputFile: my-cucumber-report.json
```


### `cucumberExporter.multiLevelStrategy`
- Type: `string`
- Default: `innerSteps`

Defines the mapping strategy used when the Wakamiti plan has more levels than expected by Cucumber. Accepted values are
- `innerSteps`: Only implementation steps are included, definition steps are discarded.
- `outerSteps`: Only the definition steps are included, the implementation steps are discarded.

Example:
```yaml
cucumberExporter:
  multiLevelStrategy: outerSteps
```
