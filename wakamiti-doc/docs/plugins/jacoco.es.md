---
title: Cobertura Jacoco
date: 2025-09-20
slug: /plugins/jacoco
---

Este plugin integra JaCoCo con Wakamiti para generar cobertura de código a partir de la ejecución de casos de prueba.

Qué hace:
- Se conecta al agente JaCoCo en tiempo de ejecución y vuelca (dump) los datos de ejecución (.exec) al finalizar 
  cada caso de prueba.
- Opcionalmente, genera reportes por caso de prueba en XML y/o CSV si se configuran las rutas de salida.
- Al finalizar la ejecución, puede generar un informe HTML agregado de cobertura si se configura su ruta de salida.

> **NOTA**
>
> El agente JaCoCo debe estar iniciado en modo `tcpserver` y escuchando en el `host` y `port` configurados. De lo 
> contrario no será posible volcar la cobertura. 

---
## Tabla de contenido

---


## Instalación


Incluye el módulo en la sección correspondiente.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:jacoco-wakamiti-plugin:1.0.0
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>jacoco-wakamiti-plugin</artifactId>
  <version>1.0.0</version>
</dependency>
```


## Configuración

### `jacoco.dump.host`
- Tipo: `string`
- Por defecto: `localhost`

Nombre de host o dirección IP del agente jacoco.

Ejemplo:
```yml
jacoco:
  dump:
    host: 192.168.5.6
```


### `jacoco.dump.port`
- Tipo: `string`
- Por defecto: `6300`

Puerto del agente jacoco.

Ejemplo:
```yml
jacoco:
  dump:
    port: 1234
```


### `jacoco.dump.output`
- Tipo: `path`
- Por defecto: `.`

Ruta donde se escribirán los datos de ejecución.

Ejemplo:
```yml
jacoco:
  dump:
    output: some/directory
```


### `jacoco.dump.retries`
- Tipo: `integer`
- Por defecto: `10`

Número de reintentos.

Ejemplo:
```yml
jacoco:
  dump:
    retries: 3
```


### `jacoco.report.xml`
- Tipo: `path`

Directorio de salida para informes XML. Se creará un informe XML para cada caso de prueba y no se creará si no se 
especifica este parámetro.

Ejemplo:
```yml
jacoco:
  report:
    xml: some/directory/xml
```

### `jacoco.report.csv`
- Tipo: `path`

Directorio de salida para informes CSV. Se creará un informe CSV para cada caso de prueba y no se creará si no se 
especifica este parámetro.

Ejemplo:
```yml
jacoco:
  report:
    csv: some/directory/csv
```


### `jacoco.report.html`
- Tipo: `path`

Directorio de salida para el informe HTML.

Ejemplo:
```yml
jacoco:
  report:
    html: some/directory/html
```


### `jacoco.report.classes`
- Tipo: `path` *required*

Ubicación de los archivos de clase Java.

Ejemplo:
```yml
jacoco:
  report:
    classes: target/classes
```


### `jacoco.report.sources`
- Tipo: `path`

Ubicación de los archivos fuente.

Ejemplo:
```yml
jacoco:
  report:
    sources: src/main/java
```

### `jacoco.report.tabwith`
- Tipo: `integer`
- Por defecto: `4`

Ancho de la tabulación para las páginas de origen.

Ejemplo:
```yml
jacoco:
  report:
    tabwith: 5
```


### `jacoco.report.name`
- Tipo: `string`
- Por defecto: `JaCoCo Coverage Report`

Nombre utilizado para este informe.

Ejemplo:
```yml
jacoco:
  report:    
    name: Wakamiti coverage report
```

