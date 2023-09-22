---
title: Informe HTML
date: 2022-09-20
slug: /plugins/html-reporter
---

Un generador de informes en formato `HTML` con los resultados de las pruebas.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:html-reporter-wakamiti-plugin:2.3.3
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>html-reporter-wakamiti-plugin</artifactId>
  <version>2.3.3</version>
</dependency>
```

---
## Tabla de contenido

---

---
## Configuración


###  `htmlReport.output`
La ruta (relativa) y el nombre del archivo generado.

El valor por defecto es `wakamiti.html`.

Ejemplo:

```yaml
htmlReport:
  output: my-cucumber-report.json
```

<br /><br />

###  `htmlReport.title`
Establece el título indicado en el informe.

Ejemplo:

```yaml
htmlReport:
  title: "Wakamiti v11"
```

<br /><br />

###  `htmlReport.css`
Indica la ruta de un fichero css que establecerá el aspecto del informe html.

Ejemplo:

```yaml
htmlReport:
  css: Wakamiti.css
```
