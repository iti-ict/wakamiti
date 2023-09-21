---
title: HTML reporter
date: 2022-09-20
slug: /en/plugins/html-reporter
---

A report generator in `HTML` format with the tests results.

```text tabs=coord name=yaml
es.iti.wakamiti:html-reporter-wakamiti-plugin:2.3.3
```

```xml tabs=coord name=maven
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>html-reporter-wakamiti-plugin</artifactId>
  <version>2.3.3</version>
</dependency>
```

---
## Table of content

---

---
## Configuration


###  `htmlReport.output`
The (relative) path and name of the generated file.

Default value is `wakamiti.html`.

Example:

```yaml
htmlReport:
  output: my-cucumber-report.json
```

<br /><br />

###  `htmlReport.title`
Sets the indicated title in the report.

Example:

```yaml
htmlReport:
  title: "Wakamiti v11"
```

<br /><br />

###  `htmlReport.css`
Indicates the path of a css file that will establish the appearance of the html report.

Example:

```yaml
htmlReport:
  css: Wakamiti.css
```
