---
title: Jacoco coverage
date: 2025-09-20
slug: /en/plugins/jacoco
---

This plugin integrates JaCoCo with Wakamiti, generating code coverage from test case execution.

It does the following:
- Connects to the JaCoCo agent at runtime and exports the execution data (.exec) at the end of each test case.
- Optionally generates reports for each test case in XML and/or CSV formats if the output paths are configured.
- At the end of execution, it can generate an aggregated HTML coverage report if the output path is configured.

> **NOTE**
>
> The JaCoCo agent must be started in 'tcpserver' mode and configured to listen on a specific host and port. 
> Otherwise, it will not be possible to generate a coverage report. 

---
## Tabla de contenido

---


## Install


Include the module in the corresponding section.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:jacoco-wakamiti-plugin:1.0.0
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>jacoco-wakamiti-plugin</artifactId>
  <version>1.0.0</version>
</dependency>
```


## Options

### `jacoco.dump.host`
- Type: `string`
- Default: `localhost`

Host name or ip address to connect to.

Example:
```yml
jacoco:
  dump:
    host: 192.168.5.6
```


### `jacoco.dump.port`
- Type: `string`
- Default: `6300`

The port to connect to.

Example:
```yml
jacoco:
  dump:
    port: 1234
```


### `jacoco.dump.output`
- Type: `path`
- Default: `.`

Path to write execution data to.

Example:
```yml
jacoco:
  dump:
    output: some/directory
```


### `jacoco.dump.retries`
- Type: `integer`
- Default: `10`

Number of retries.

Example:
```yml
jacoco:
  dump:
    retries: 3
```


### `jacoco.report.xml`
- Type: `path`

Output directory for XML reports. An XML report will be created for each test case and will not be created if this 
parameter is not specified.

Example:
```yml
jacoco:
  report:
    xml: some/directory/xml
```

### `jacoco.report.csv`
- Type: `path`

Output directory for CSV reports. An CSV report will be created for each test case and will not be created if this
parameter is not specified.

Example:
```yml
jacoco:
  report:
    csv: some/directory/csv
```


### `jacoco.report.html`
- Type: `path`

Output directory for the HTML report.

Example:
```yml
jacoco:
  report:
    html: some/directory/html
```


### `jacoco.report.classes`
- Type: `path` *required*

Location of Java class files.

Example:
```yml
jacoco:
  report:
    classes: target/classes
```


### `jacoco.report.sources`
- Type: `path`

Location of the source files.

Example:
```yml
jacoco:
  report:
    sources: src/main/java
```

### `jacoco.report.tabwith`
- Type: `integer`
- Default: `4`

Tab stop width for the source pages.

Example:
```yml
jacoco:
  report:
    tabwith: 5
```


### `jacoco.report.name`
- Type: `string`
- Default: `JaCoCo Coverage Report`

Name used for this report.

Example:
```yml
jacoco:
  report:    
    name: Wakamiti coverage report
```


