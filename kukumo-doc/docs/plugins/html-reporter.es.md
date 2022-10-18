---
title: Informe HTML
date: 2022-09-20
slug: /plugins/html-reporter
---

Un generador de informes en formato `HTML` con los resultados de las pruebas.


## Configuración

---
####  `htmlReport.output`
La ruta (relativa) y el nombre del archivo generado.

El valor por defecto es `kukumo.html`.

Ejemplo:

```yaml
htmlReport:
  output: my-cucumber-report.json
```

---
####  `htmlReport.title`
Establece el título indicado en el informe.

Ejemplo:

```yaml
htmlReport:
  title: "Wakamiti v11"
```

---
####  `htmlReport.css`
Indica la ruta de un fichero css que establecerá el aspecto del informe html.

Ejemplo:

```yaml
htmlReport:
  css: Wakamiti.css
```