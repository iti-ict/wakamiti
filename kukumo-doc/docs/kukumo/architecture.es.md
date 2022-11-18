---
title: Arquitectura
date: 2022-09-20
slug: /kukumo/architecture
---


## Configuración global

La configuración de Wakamiti se establece mediante un fichero `yaml` ubicado en el directorio de pruebas. Por defecto, 
Wakamiti buscará el fichero con el nombre `kukumo.yaml`.

- [`kukumo.resourceTypes`](#kukumoresourcetypes)
- [`kukumo.resourcePath`](#kukumoresourcepath)
- [`kukumo.outputFilePath`](#kukumooutputfilepath)
- [`kukumo.tagFilter`](#kukumotagfilter)
- [`kukumo.idTagPattern`](#kukumoidtagpattern)
- [`kukumo.launcher.modules`](#kukumolaunchermodules)
- [`kukumo.report.generation`](#kukumoreportgeneration)
- [`kukumo.redefinition.definitionTag`](#kukumoredefinitiondefinitiontag)
- [`kukumo.redefinition.implementationTag`](#kukumoredefinitionimplementationtag)
- [`kukumo.log.path`](#kukumologpath)
- [`kukumo.log.level`](#kukumologlevel)
- [`mavenFetcher.remoteRepositories`](#mavenfetcherremoterepositories)
- [`mavenFetcher.localRepository`](#mavenfetcherlocalrepository)

---
### `kukumo.resourceTypes`

Establece el lenguaje de los escenarios de prueba. Actualmente solo está disponible `gherkin`.

Ejemplo:

```yaml
kukumo:
  resourceTypes: gherkin
```
&nbsp;


### `kukumo.resourcePath`

Establece la ruta donde se encuentran los ficheros de las pruebas.

El valor por defecto es `.` (ruta donde se encuentra el fichero de configuración).

Ejemplo:

```yaml
kukumo:
  resourcePath: /other/path
```

---
### `kukumo.outputFilePath`

Establece el directorio de salida del fichero con el resultado de las pruebas.

El valor por defecto es `kukumo.json`.

Ejemplo:

```yaml
kukumo:
  outputFilePath: result/kukumo.json
```

---
### `kukumo.tagFilter`

Filtra los escenarios etiquetados con la [expresión](https://cucumber.io/docs/cucumber/api/#tag-expressions) indicada. 

Ejemplo:

```yaml
kukumo:
  tagFilter: not Ignore
```

---
### `kukumo.idTagPattern`

Establece el patrón de identificadores de los escenarios. Debe contener una expresión regular válida.

El valor por defecto es `ID-(\w*)`.

Ejemplo:

```yaml
kukumo:
  idTagPattern: ([0-9]+)
```

---
### `kukumo.launcher.modules`

Establece los módulos que serán utilizados durante las pruebas. Estos módulos son artefactos maven ubicados en un 
[repositorio indicado en la configuración](#mavenfetcher-remoterepositories). Se debe indicar con el patrón 
`<groupId>:<artifactId>:<version>`.

Ejemplo:

```yaml
kukumo:
  launcher:
    modules:
      - iti.kukumo:kukumo-rest:1.0.0
      - iti.kukumo:kukumo-db:1.0.0
      - mysql:mysql-connector-java:8.0.28
```

---
### `kukumo.report.generation`

Indica si se generará el informe con el resultado de las pruebas al terminar la ejecución o no.

El valor por defecto es `true`.

Ejemplo:

```yaml
kukumo:
  report: 
    generation: "false"
```

---
### `kukumo.redefinition.definitionTag`

Establece la etiqueta para indicar que la **caracteristica** es una [definición]().

El valor por defecto es `definition`.

Ejemplo:

```yaml
kukumo:
  redefinition:
    definitionTag: def
```

---
### `kukumo.redefinition.implementationTag`

Establece la etiqueta para indicar que la **caracteristica** es una [implementación]().

El valor por defecto es `implementation`.

Ejemplo:

```yaml
kukumo:
  redefinition:
    implementationTag: impl
```

---
### `kukumo.log.path`

Establece el directorio donde se creará un fichero de log con nombre `kukumo-${yyyyMMddhhmmss}.log`, donde 
`${yyyyMMddhhmmss}` es el patrón de la fecha del sistema. Por defecto no se creará el log.

Ejemplo:

```yaml
kukumo:
  log:
    path: results
```

---
### `kukumo.log.level`

Establece el nivel de los logs de kukumo. Dependiendo del nivel indicado se mostrará u omitirá más o menos información.
Los posibles valores son: `info`, `error`, `fatal`, `warning`, `debug`, `trace`.
[Leer más](https://unpocodejava.com/2011/01/17/niveles-log4j/)

El valor por defecto es `info`.

Ejemplo:

```yaml
kukumo:
  log:
    level: debug
```

---
### `mavenFetcher.remoteRepositories`

Establece repositorios remotos.

Ejemplo:

```yaml
mavenFetcher:
  remoteRepositories: https://repo.maven.apache.org/maven2;file:///home/user/.m2/repository
```

---
### `mavenFetcher.localRepository`

Establece la ubicación del repositorio local.

Ejemplo:

```yaml
mavenFetcher:
  localRepository: /usr/mvn-repo
```


---
## Tipos de datos

---
### `text`
Cualquier texto entrecomillado con `''`. 

Ejemplo: `'texto de ejemplo'`.

---
### `word`
Cualquier palabra (admite guiones). 

Ejemplo: `AB_C-1D`.

---
### `file`
Una ruta local (relativa o absoluta). 

Ejemplo: `'dir/file.yaml'`.

---
### `url`
Una dirección URL. 

---
Ejemplo: `https://localhost/test`.

### `integer`
Un número entero. 

Ejemplo: `14`.

---
### `decimal`
Un número con decimales. 

Ejemplo: `14.5`.

---
### `date`
Una fecha con formato `yyyy-MM-dd`. 

Ejemplo: `'2022-02-22'`.

---
### `time`
Una hora con formato `hh:mm`, `hh:mm:ss` o `hh:mm:ss.SSS`. 

Ejemplo: `'12:05:06.468'`.

---
### `datetime`
Una fecha y hora con formato `yyyy-MM-ddThh:mm`, `yyyy-MM-ddThh:mm:ss` o `yyyy-MM-ddThh:mm:ss.SSS`. 

Ejemplo: `'2022-02-22T12:05:06.468'`.

---
### `text-assertion`
Comparador de textos. [Ver más](#comparadores). 

Ejemplo: `es igual a 'algo'`.

---
### `long-assertion`
Comparador de números enteros. [Ver más](#comparadores). 

Ejemplo: `es igual o mayor que 13`.

---
### `float-assertion`
Comparador de números decimales. [Ver más](#comparadores). 

Ejemplo: `es igual o menor que 10.02`.

---
### `document`
Bloque de texto ubicado en la siguiente línea de la descripción del paso. 
[Ver más](https://cucumber.io/docs/gherkin/reference/#doc-strings). 

Ejemplo:
```gherkin
"""
Un texto 
multilínea
"""
```

---
### `table`
Tabla de datos ubicada en la siguiente línea de la descipción del paso.
[Ver más](https://cucumber.io/docs/gherkin/reference/#data-tables).

Ejemplo:
```gherkin
| USER  | STATE | BLOCKING_DATE |
| user1 | 2     | <null>        |
```

---
## Comparadores

Fragmentos de texto que se traducen en comparadores para distintos tipos de datos, reutilizables en cualquier paso.
Los tipos de comparadores disponibles son:
- `text-assertion`
- `integer-assertion`
- `long-assertion`
- `decimal-assertion`

---
### `(no) es (igual a) ~x~` 
Tipo: numérico y texto. También admite las variantes `es (igual a) ~x~ \(sin distinguir mayúsculas\)` y 
`es (igual a) ~x~ \(ignorando espacios\)` para las comparaciones de tipo texto, y la versión en negativo.

Ejemplos:
```
es 14
```
```
es igual a 22
```
```
es igual a 'algo'
```
```
es 'algo'
```
```
es igual a 'aLgo' (sin distinguir mayúsculas)
```
```
es igual a ' algo ' (ignorando espacios)
```
```
no es igual a 14
```

---
### `(no) es (mayor|menor) que ~x~`
Tipo: numérico. También admite decimales y la versión en negativo.

Ejemplos:
```
es mayor que 14
```
```
es mayor que 14.3
```
```
es menor que 14
```
```
es menor que 14.3
```
```
no es mayor que 14
```

---
### `(no) es (mayor|menor) o igual que ~x~`
Tipo: numérico. También admite decimales y la versión en negativo.

Ejemplos:
```
es mayor o igual que 14
```
```
es mayor o igual que 14.3
```
```
es menor o igual que 14
```
```
es menor o igual que 14.3
```
```
no es mayor o igual que 14
```

---
### `(no) empieza por ~x~`
Tipo: texto. También admite la variante `empieza por ~x~ \(sin distinguir mayúsculas\)`, y la versión en negativo.

Ejemplos:
```
empieza por 'algo'
```
```
empieza por 'aLgo' (sin distinguir mayúsculas)
```
```
no empieza por 'algo'
```

---
### `(no) acaba en ~x~`
Tipo: texto. También admite la variante `acaba en ~x~ \(sin distinguir mayúsculas\)`, y la versión en negativo.

Ejemplos:
```
acaba en 'algo'
```
```
acaba en 'aLgo' (sin distinguir mayúsculas)
```
```
no acaba en 'algo'
```

---
### `(no) contiene ~x~`
Tipo: texto. También admite la variante `contiene ~x~ \(sin distinguir mayúsculas\)`, y la versión en negativo.

Ejemplos:
```
contiene 'algo'
```
```
contiene 'aLgo' (sin distinguir mayúsculas)
```
```
no contiene 'algo'
```

---
### `(no) es nulo`
Tipo: numérico y texto. También admite la versión en negativo.

Ejemplos:
```
es nulo
```
```
no es nulo
```

---
### `(no) está vacío`
Tipo: numérico y texto. También admite la versión en negativo.

Ejemplos:
```
está vacío
```
```
no está vacío
```

---
### `(no) es nulo o está vacío`
Tipo: numérico y texto. También admite la versión en negativo.

Ejemplos:
```
es nulo o está vacío
```
```
no es nulo o está vacío
```

