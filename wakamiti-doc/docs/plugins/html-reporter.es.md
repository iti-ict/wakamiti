---
title: Informe HTML
date: 2022-09-20
slug: /plugins/html-reporter
---

Un generador de informes en formato `HTML` con los resultados de las pruebas.

---
## Tabla de contenido

---
## Configuración


####  `htmlReport.output`
La ruta (relativa) y el nombre del archivo generado.

El valor por defecto es `wakamiti.html`.

Ejemplo:

```yaml
htmlReport:
  output: my-cucumber-report.json
```

<br /><br />

####  `htmlReport.title`
Establece el título indicado en el informe.

Ejemplo:

```yaml
htmlReport:
  title: "Wakamiti v11"
```

<br /><br />

####  `htmlReport.css`
Indica la ruta de un fichero css que establecerá el aspecto del informe html.

Ejemplo:

```yaml
htmlReport:
  css: Wakamiti.css
```
