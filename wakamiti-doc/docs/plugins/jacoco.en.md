---
title: Jacoco coverage
date: 2025-09-20
slug: /en/plugins/jacoco
---

This plugin integrates JaCoCo with Wakamiti, generating code coverage from test case execution.

The `merge` mode determines whether reports are generated per test case, for all configured agents together, or per
host.

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
es.iti.wakamiti:jacoco-wakamiti-plugin:2.0.2
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>jacoco-wakamiti-plugin</artifactId>
  <version>2.0.2</version>
</dependency>
```


## Options

### `jacoco.dump.hosts`
- Type: `string[]`
- Default: `localhost:6300`

JaCoCo agent endpoints in `host:port` format. At least one endpoint is required. The host must be a DNS name or IPv4
address, and the port must be between 1 and 65535. Host names must be unique, ignoring case, when `merge` is `HOST`.

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

Output directory for per-scenario execution data with `merge: NONE`. It is created when needed. With `merge: ALL`, this
path is the aggregate base name and generates `some/directory.exec`; with `merge: HOST`, it generates
`some/directory.<host>.exec` for every configured host.

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

Output directory for per-scenario XML reports with `merge: NONE`. With `merge: ALL`, this path is the aggregate base
name and generates `some/directory.xml`; with `merge: HOST`, it generates `some/directory.<host>.xml` for every
configured host. XML reports are disabled when this parameter is absent.

Example:
```yml
jacoco:
  report:
    xml: some/directory/xml
```

### `jacoco.report.csv`
- Type: `path`

Output directory for per-scenario CSV reports with `merge: NONE`. With `merge: ALL`, this path is the aggregate base
name and generates `some/directory.csv`; with `merge: HOST`, it generates `some/directory.<host>.csv` for every
configured host. CSV reports are disabled when this parameter is absent.

Example:
```yml
jacoco:
  report:
    csv: some/directory/csv
```


### `jacoco.report.html`
- Type: `path`

Output directory for the final aggregate HTML report. With `merge: HOST`, a directory named `<path>.<host>` is
generated for every configured host. It is created when needed.

Example:
```yml
jacoco:
  report:
    html: some/directory/html
```


### `jacoco.report.classes`
- Type: `path[]` *required*

Existing root directories containing Java class files. A single path is also accepted.
With `merge: HOST`, each report includes only classes present in that host's execution data.

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
- Type: `NONE | ALL | HOST`
- Default: `NONE`

Controls report grouping. `NONE` generates `.exec`, XML and CSV artifacts per test case; its final HTML report combines
all test-case execution data. `ALL` combines data from every configured agent into one `.exec`, XML, CSV and HTML
report. `HOST` generates one set of aggregate artifacts per host, using the host as a suffix of each configured path.
Lifecycle hook coverage is included in `ALL` and `HOST`, but not `NONE`. Host names must be unique in `HOST`, even when
their ports differ. In `HOST`, classes without execution data from that host are excluded. Boolean values are no longer
supported.

Example:
```yml
jacoco:
  report:
    merge: NONE
```
