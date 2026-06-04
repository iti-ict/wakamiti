---
title: Allure report
date: 2026-06-03
slug: /en/plugins/allure
---


An exporter that writes Wakamiti execution results in the format expected by Allure.

The generated output can be consumed directly by Allure CLI tools, for example with `allure serve`.

---
## Table of contents

---


## Install


Include the module in the corresponding section.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:allure-wakamiti-plugin:1.0.0
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>allure-wakamiti-plugin</artifactId>
  <version>1.0.0</version>
</dependency>
```


## Options


### `allureReport.output`
- Type: `file`
- Default: `allure-results`

The output directory where the Allure `*-result.json` files are generated.

Example:
```yaml
allureReport:
  output: target/allure-results
```
