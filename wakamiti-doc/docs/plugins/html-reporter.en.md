---
title: HTML reporter
date: 2022-09-20
slug: /en/plugins/html-reporter
---


A report generator in `HTML` format with the tests results.


---
## Tabla de contenido

---


## Install


Include the module in the corresponding section.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:html-reporter-wakamiti-plugin:2.4.0
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>html-reporter-wakamiti-plugin</artifactId>
  <version>2.4.0</version>
</dependency>
```


## Options


###  `htmlReport.output`
- Type: `file` 
- Default: `wakamiti.html`

The (relative) path and name of the generated file.

Example:
```yaml
htmlReport:
  output: my-cucumber-report.html
```


###  `htmlReport.title`
- Type: `string`

Sets the title shown in the report.

Example:
```yaml
htmlReport:
  title: "Wakamiti v11"
```


###  `htmlReport.css`
- Type: `file`

Specifies the path to a css file that defines the appearance of the html report.

Example:
```yaml
htmlReport:
  css: Wakamiti.css
```
