---
title: Informe Allure
date: 2026-06-03
slug: /plugins/allure
---


Un exportador que escribe los resultados de ejecución de Wakamiti en el formato esperado por Allure.

La salida generada se puede consumir directamente con las herramientas CLI de Allure, por ejemplo con `allure serve`.

---
## Tabla de contenido

---


## Instalación


Incluye el módulo en la sección correspondiente.

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


## Configuración


### `allureReport.output`
- Tipo: `file`
- Por defecto: `allure-results`

El directorio de salida donde se generan los ficheros Allure `*-result.json`.

Ejemplo:
```yaml
allureReport:
  output: target/allure-results
```
