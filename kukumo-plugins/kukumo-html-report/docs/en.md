

A report generator in `HTML` format with the tests results.

---

**Configuration**:
- [`htmlReport.output`](#htmlreportoutput)
- [`htmlReport.title`](#htmlreporttitle)
- [`htmlReport.css`](#htmlreportcss)

---


## Configuration

---
####  `htmlReport.output`
The (relative) path and name of the generated file.

Default value is `kukumo.html`.

Example:

```yaml
htmlReport:
  output: my-cucumber-report.json
```

---
####  `htmlReport.title`
Sets the indicated title in the report.

Example:

```yaml
htmlReport:
  title: "Wakamiti v11"
```

---
####  `htmlReport.css`
Indicates the path of a css file that will establish the appearance of the html report.

Example:

```yaml
htmlReport:
  css: Wakamiti.css
```