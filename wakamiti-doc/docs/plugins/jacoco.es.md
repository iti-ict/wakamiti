---
title: Cobertura Jacoco
date: 2025-09-20
slug: /plugins/jacoco
---

Este plugin integra JaCoCo con Wakamiti para generar cobertura de código a partir de la ejecución de casos de prueba.

El modo `merge` determina si los informes se generan por caso de prueba, para todos los agentes configurados juntos o
por host.

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
es.iti.wakamiti:jacoco-wakamiti-plugin:2.0.2
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>jacoco-wakamiti-plugin</artifactId>
  <version>2.0.2</version>
</dependency>
```


## Configuración

### `jacoco.dump.hosts`
- Tipo: `string[]`
- Por defecto: `localhost:6300`

Endpoints de los agentes JaCoCo en formato `host:port`. Debe indicarse al menos uno. El host debe ser un nombre DNS o
una dirección IPv4, y el puerto debe estar entre 1 y 65535. Los nombres de host deben ser únicos sin distinguir
mayúsculas de minúsculas cuando `merge` es `HOST`.

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

Directorio de salida para datos de ejecución por escenario con `merge: NONE`. Se crea cuando es necesario. Con
`merge: ALL`, esta ruta es la base del agregado y genera `some/directory.exec`; con `merge: HOST`, genera
`some/directory.<host>.exec` para cada host configurado.

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

Directorio de salida para informes XML por escenario con `merge: NONE`. Con `merge: ALL`, esta ruta es la base del
agregado y genera `some/directory.xml`; con `merge: HOST`, genera `some/directory.<host>.xml` para cada host
configurado. No se generarán informes XML si no se especifica este parámetro.

Ejemplo:
```yml
jacoco:
  report:
    xml: some/directory/xml
```

### `jacoco.report.csv`
- Tipo: `path`

Directorio de salida para informes CSV por escenario con `merge: NONE`. Con `merge: ALL`, esta ruta es la base del
agregado y genera `some/directory.csv`; con `merge: HOST`, genera `some/directory.<host>.csv` para cada host
configurado. No se generarán informes CSV si no se especifica este parámetro.

Ejemplo:
```yml
jacoco:
  report:
    csv: some/directory/csv
```


### `jacoco.report.html`
- Tipo: `path`

Directorio de salida para el informe HTML agregado final. Con `merge: HOST`, se genera un directorio llamado
`<ruta>.<host>` para cada host. Se crea cuando es necesario.

Ejemplo:
```yml
jacoco:
  report:
    html: some/directory/html
```


### `jacoco.report.classes`
- Tipo: `path[]` *required*

Directorios raíz existentes que contienen los archivos de clase Java. También se acepta una única ruta.
Con `merge: HOST`, cada informe incluye únicamente las clases presentes en los datos de ejecución de ese host.

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
- Tipo: `NONE | ALL | HOST`
- Por defecto: `NONE`

Controla la agrupación de informes. `NONE` genera artefactos `.exec`, XML y CSV por caso de prueba; su informe HTML
final combina los datos de ejecución de todos los casos. `ALL` combina los datos de todos los agentes configurados en
un único informe `.exec`, XML, CSV y HTML. `HOST` genera un conjunto de artefactos agregados por host, usando el host
como sufijo de cada ruta configurada. La cobertura de hooks de ciclo de vida se incluye en `ALL` y `HOST`, pero no en
`NONE`. Los nombres de host deben ser únicos en `HOST`, incluso si sus puertos son distintos. Los valores booleanos ya
no son compatibles. En `HOST`, se excluyen las clases sin datos de ejecución de ese host.

Ejemplo:
```yml
jacoco:
  report:
    merge: NONE
```
