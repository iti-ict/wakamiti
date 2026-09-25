---
title: Cobertura Jacoco
date: 2025-09-20
slug: /plugins/jacoco
---

Este plugin integra JaCoCo con Wakamiti para generar cobertura de código a partir de la ejecución de casos de prueba.

Qué hace:
- Con `merge` desactivado, vuelca un `.exec` y genera informes XML y/o CSV por cada caso de prueba.
- Con `merge` activado, acumula la cobertura y genera únicamente los artefactos agregados al finalizar la ejecución.

> **NOTA**
>
> Cada agente JaCoCo debe estar iniciado en modo `tcpserver` y escuchando en uno de los endpoints configurados. De lo
> contrario no será posible volcar la cobertura.

---
## Tabla de contenido

---


## Instalación


Incluye el módulo en la sección correspondiente.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:jacoco-wakamiti-plugin:2.0.0
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>jacoco-wakamiti-plugin</artifactId>
  <version>2.0.0</version>
</dependency>
```


## Configuración

### `jacoco.dump.hosts`
- Tipo: `string[]`
- Por defecto: `localhost:6300`

Endpoints de los agentes JaCoCo en formato `host:port`. Debe indicarse al menos uno. El host debe ser un nombre DNS o
una dirección IPv4, y el puerto debe estar entre 1 y 65535. Los datos de todos los agentes se combinan por escenario.

Ejemplo:
```yml
jacoco:
  dump:
    hosts:
      - 192.168.5.6:1234
      - jacoco-agent:6300
```


### `jacoco.dump.output`
- Tipo: `path`
- Por defecto: `.`

Directorio de salida donde se escribirán los datos de ejecución por escenario cuando `merge` esté desactivado. Se crea
cuando es necesario. Con `merge` activo, esta ruta se usa como base del agregado: `some/directory.exec`.

Los archivos `.exec` por escenario incluyen únicamente clases con al menos una probe ejecutada. Con `merge` activo no
se generan archivos por escenario; solo se genera el agregado sin aplicar este filtrado adicional.

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

Directorio de salida para informes XML por escenario cuando `merge` esté desactivado. Se crea cuando es necesario. Con
`merge` activo, esta ruta se usa como base del agregado XML. No se generarán informes XML si no se especifica este
parámetro.

Ejemplo:
```yml
jacoco:
  report:
    xml: some/directory/xml
```

### `jacoco.report.csv`
- Tipo: `path`

Directorio de salida para informes CSV por escenario cuando `merge` esté desactivado. Se crea cuando es necesario. Con
`merge` activo, esta ruta se usa como base del agregado CSV. No se generarán informes CSV si no se especifica este
parámetro.

Ejemplo:
```yml
jacoco:
  report:
    csv: some/directory/csv
```


### `jacoco.report.html`
- Tipo: `path`

Directorio de salida para el informe HTML agregado. Se crea cuando es necesario.

Ejemplo:
```yml
jacoco:
  report:
    html: some/directory/html
```


### `jacoco.report.classes`
- Tipo: `path[]` *required*

Directorios raíz existentes que contienen los archivos de clase Java. También se acepta una única ruta.

Ejemplo:
```yml
jacoco:
  report:
    classes:
      - target/classes
      - target/generated-classes
```


### `jacoco.report.sources`
- Tipo: `path[]`

Directorios raíz existentes que contienen los archivos fuente. También se acepta una única ruta.

Ejemplo:
```yml
jacoco:
  report:
    sources: 
      - src/main/java
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

### `jacoco.report.merge`
- Tipo: `boolean`
- Por defecto: `true`

Genera únicamente los artefactos agregados. La cobertura de los hooks de lifecycle se incluye en ellos. Los agregados
se guardan añadiendo `.exec`, `.xml` y `.csv` a las rutas configuradas para dump, XML y CSV, respectivamente.

Con `merge` desactivado, los informes XML y CSV por escenario incluyen únicamente clases con cobertura. Con `merge`
activado, los informes agregados, incluido el HTML, muestran todas las clases configuradas, también las que no tienen
cobertura.

Ejemplo:
```yml
jacoco:
  report:
    merge: true
```
