---
title: Informe HTML
date: 2022-09-20
slug: /plugins/html-reporter
---


Un generador de informes en formato `HTML` con los resultados de las pruebas.


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


## Configuración


###  `htmlReport.output`
- Tipo: `file`
- Por defecto: `wakamiti.html`

La ruta (relativa) y el nombre del archivo generado.

Ejemplo:
```yaml
htmlReport:
  output: my-cucumber-report.json
```


###  `htmlReport.title`
- Tipo: `string`

Establece el título indicado en el informe.

Ejemplo:
```yaml
htmlReport:
  title: "Wakamiti v11"
```


###  `htmlReport.css`
- Tipo: `file`

Indica la ruta de un fichero css que establecerá el aspecto del informe html.

Ejemplo:
```yaml
htmlReport:
  css: Wakamiti.css
```
