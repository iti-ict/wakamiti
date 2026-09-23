---
title: Jacoco coverage
date: 2025-09-20
slug: /en/plugins/jacoco
---

This plugin integrates JaCoCo with Wakamiti, generating code coverage from test case execution.

It does the following:
- Connects to the JaCoCo agents at runtime and exports their execution data (.exec) at the end of each test case.
- Optionally generates reports for each scenario in XML and/or CSV formats if the output paths are configured.
- At the end of execution, it can generate an aggregated HTML coverage report if the output path is configured.

> **NOTE**
>
> Each JaCoCo agent must be started in `tcpserver` mode and listen on one of the configured endpoints. Otherwise, it
> will not be possible to generate a coverage report.

---
## Table of contents

---


## Install


Include the module in the corresponding section.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:jacoco-wakamiti-plugin:2.0.0
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>jacoco-wakamiti-plugin</artifactId>
  <version>2.0.0</version>
</dependency>
```


## Options

### `jacoco.dump.hosts`
- Type: `string[]`
- Default: `localhost:6300`

JaCoCo agent endpoints in `host:port` format. At least one endpoint is required. The host must be a DNS name or IPv4
address, and the port must be between 1 and 65535. Data from all agents is combined per scenario.

Example:
```yml
jacoco:
  dump:
    hosts:
      - 192.168.5.6:1234
      - jacoco-agent:6300
```


### `jacoco.dump.output`
- Type: `path`
- Default: `.`

Output directory for per-scenario execution data. It is created when needed. When `merge` is enabled, this path is
also used as the aggregate base name: `some/directory.exec`.

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

Output directory for per-scenario XML reports. It is created when needed. When `merge` is enabled, this path is also
used as the aggregate XML base name. XML reports are disabled when this parameter is absent.

Example:
```yml
jacoco:
  report:
    xml: some/directory/xml
```

### `jacoco.report.csv`
- Type: `path`

Output directory for per-scenario CSV reports. It is created when needed. When `merge` is enabled, this path is also
used as the aggregate CSV base name. CSV reports are disabled when this parameter is absent.

Example:
```yml
jacoco:
  report:
    csv: some/directory/csv
```


### `jacoco.report.html`
- Type: `path`

Output directory for the aggregate HTML report. It is created when needed.

Example:
```yml
jacoco:
  report:
    html: some/directory/html
```


### `jacoco.report.classes`
- Type: `path[]` *required*

Existing root directories containing Java class files. A single path is also accepted.

Example:
```yml
jacoco:
  report:
    classes:
      - target/classes
      - target/generated-classes
```


### `jacoco.report.sources`
- Type: `path[]`

Existing root directories containing Java source files. A single path is also accepted.

Example:
```yml
jacoco:
  report:
    sources: 
      - src/main/java
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

### `jacoco.report.merge`
- Type: `boolean`
- Default: `true`

Generates additional aggregate reports without removing individual scenario files. Lifecycle hook coverage is included
only in the aggregate reports. The aggregate files are the configured dump, XML and CSV paths with `.exec`, `.xml` and
`.csv` appended, respectively.

Example:
```yml
jacoco:
  report:
    merge: true
```
