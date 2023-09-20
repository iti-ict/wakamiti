---
title: Arquitectura
date: 2022-09-20
slug: /wakamiti/architecture
---



---
## Tabla de contenido

---

---
## Configuración global

La configuración de Wakamiti se establece mediante un fichero `yaml` ubicado en el directorio de pruebas. Por defecto, 
Wakamiti buscará el fichero con el nombre `wakamiti.yaml`.

<br /><br />

### `wakamiti.resourceTypes`

Establece el lenguaje de los escenarios de prueba. Actualmente solo está disponible `gherkin`.

Ejemplo:

```yaml
wakamiti:
  resourceTypes: gherkin
```

<br /><br />

### `wakamiti.resourcePath`

Establece la ruta donde se encuentran los ficheros de las pruebas.

El valor por defecto es `.` (ruta donde se encuentra el fichero de configuración).

Ejemplo:

```yaml
wakamiti:
  resourcePath: /other/path
```

<br /><br />

### `wakamiti.outputFilePath`

Establece el directorio de salida del fichero con el resultado de las pruebas.

La ruta puede contener las siguientes variables de sustitución (**desde** 1.7.0):

| variable   | sustitución               |
|------------|---------------------------|
| `%YYYY%`   | año (4 dígitos)           |
| `%YY%`     | año (2 dígitos)           |
| `%MM%`     | mes                       |
| `%DD%`     | día de mes                |
| `%hh%`     | hora (formato 24H)        |
| `%mm%`     | minutos                   |
| `%ss%`     | segundos                  |
| `%sss%`    | milisegundos (3 dígitos)  |
| `%DATE%`   | `%YYYY%%MM%%DD%`          |
| `%TIME%`   | `%hh%%mm%%ss%%ssss%`      |
| `%execID%` | ID único de ejecucicón    |

El valor por defecto es `wakamiti.json`.

Ejemplo:

```yaml
wakamiti:
  outputFilePath: result/wakamiti.json
```

<br /><br />

### `wakamiti.outputFilePerTestCase`

Establece si se debe generar un fichero de salida por cada caso de test. En caso de activarse, el 
valor de `wakamiti.outputFilePath` será el directorio destini, y el nombre del fichero será el 
propio ID del caso de test.

El valor por defecto es `false`

Ejemplo:
```yaml
wakamiti:
  outputFilePerTestCase: true

```

**Desde** 1.7.0

<br /><br />

### `wakamiti.tagFilter`

Filtra los escenarios etiquetados con la [expresión](https://cucumber.io/docs/cucumber/api/#tag-expressions) indicada. 

Ejemplo:

```yaml
wakamiti:
  tagFilter: not Ignore
```

<br /><br />

### `wakamiti.idTagPattern`

Establece el patrón de identificadores de los escenarios. Debe contener una expresión regular válida.

El valor por defecto es `ID-(\w*)`.

Ejemplo:

```yaml
wakamiti:
  idTagPattern: ([0-9]+)
```

<br /><br />

### `wakamiti.strictTestCaseID`

Establece si se debe asegurar que cada caso de test esté debidamente etiquetado con un valor
que cumpla con el patrón de `idTagPattern`. En caso de activarse y que no se cumpla dicha 
condición, la ejecución se detendrá con un error informando de ello.

El valor por defecto es `false`

Ejemplo:

```yaml
wakamiti:
  strictTestCaseID: true
```

**Desde** 1.7.0

<br /><br />

### `wakamiti.launcher.modules`

Establece los módulos que serán utilizados durante las pruebas. Estos módulos son artefactos maven ubicados en un 
[repositorio indicado en la configuración](#mavenfetcher-remoterepositories). Se debe indicar con el patrón 
`<groupId>:<artifactId>:<version>`.

Ejemplo:

```yaml
wakamiti:
  launcher:
    modules:
      - es.iti.wakamiti:wakamiti-rest:1.0.0
      - es.iti.wakamiti:wakamiti-db:1.0.0
      - mysql:mysql-connector-java:8.0.28
```

<br /><br />

### `wakamiti.report.generation`

Indica si se generará el informe con el resultado de las pruebas al terminar la ejecución o no.

El valor por defecto es `true`.

Ejemplo:

```yaml
wakamiti:
  report: 
    generation: "false"
```

<br /><br />

### `wakamiti.redefinition.definitionTag`

Establece la etiqueta para indicar que la **caracteristica** es una [definición]().

El valor por defecto es `definition`.

Ejemplo:

```yaml
wakamiti:
  redefinition:
    definitionTag: def
```

<br /><br />

### `wakamiti.redefinition.implementationTag`

Establece la etiqueta para indicar que la **caracteristica** es una [implementación]().

El valor por defecto es `implementation`.

Ejemplo:

```yaml
wakamiti:
  redefinition:
    implementationTag: impl
```

<br /><br />

### `wakamiti.properties.hidden`

Establece las [propiedades](#propiedades-din%C3%A1micas) que permanecerán ocultas en el informe de pruebas.

Ejemplo:

```yaml
wakamiti:
  properties:
    hidden: 
      - token
      - credentials.password
```

<br /><br />

### `wakamiti.log.path`

Establece el directorio donde se creará un fichero de log con nombre `wakamiti-${yyyyMMddhhmmss}.log`, donde 
`${yyyyMMddhhmmss}` es el patrón de la fecha del sistema. Por defecto no se creará el log.

Ejemplo:

```yaml
wakamiti:
  log:
    path: results
```

<br /><br />

### `wakamiti.log.level`

Establece el nivel de los logs de wakamiti. Dependiendo del nivel indicado se mostrará u omitirá más o menos información.
Los posibles valores son: `info`, `error`, `fatal`, `warning`, `debug`, `trace`.
[Leer más](https://unpocodejava.com/2011/01/17/niveles-log4j/)

El valor por defecto es `info`.

Ejemplo:

```yaml
wakamiti:
  log:
    level: debug
```

<br /><br />

### `wakamiti.logs.ansi.enabled` 

Establece si los logs por consola deben usar [códigos de control ANSI](https://en.wikipedia.org/wiki/ANSI_escape_code).

El valor por defecto es `true`.

Ejemplo:

```yaml
wakamiti:
  logs:
    ansi.enabled: true
```

<br /><br />

### `wakamiti.logs.showLogo`

Establece si se debe mostrar el logo de Wakamiti en los logs por consola al inicio de la ejecución.

El valor por defecto es `true`.

Ejemplo:

```yaml
wakamiti:
  logs:
    showLogo: true
```

<br /><br />

### `wakamiti.logs.showElapsedTime` 

Establece si se debe mostrar los tiempos transcurridos en los logs por consola.

El valor por defecto es `true`.

Ejemplo:

```yaml
wakamiti:
  logs:
    showElapsedTime: true
```

<br /><br />

### `wakamiti.junit.treatStepsAsTests`

Establece si, al usar el runner JUnit de Wakamiti, se debe notificar cada paso como test (para que 
los informes y resultados derivados de JUnit sean más relevantes). 

El valor por defecto es `false`.

Ejemplo:

```yaml
wakamiti:
  junit:
    treatStepsAsTests: true
```

<br /><br />

### `wakamiti.nonRegisteredStepProviders`

Permite incluir proveedores de pasos de forma dinámica, sin de que formen parte de ningún 
plugin. De esta manera, se pueden crear pasos ad hoc para necesidades del proyecto específico 
a testear.


Ejemplo:

```yaml
wakamiti:
  nonRegisteredStepProviders:
    - com.example.CustomSteps
```

<br /><br />

### `mavenFetcher.remoteRepositories`

Establece repositorios remotos.

Ejemplo:

```yaml
mavenFetcher:
  remoteRepositories: https://repo.maven.apache.org/maven2;file:///home/user/.m2/repository
```

<br /><br />

### `mavenFetcher.localRepository`

Establece la ubicación del repositorio local.

Ejemplo:

```yaml
mavenFetcher:
  localRepository: /usr/mvn-repo
```



---
## Configuración de features

Adicionalmente, a la configuración global, se pueden incluir propiedades específicas en cada fichero feature

- [`language`](#language)
- [`dataFormatLanguage`](#language)
- [`modules`](#modules)
- [`redefinition.stepMap`](#redefinitionstepmap)

<br /><br />

### `language`

Establece el idioma (identificado mediante [ISO 639-1](https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes))
en el que se va a escribir los escenarios del fichero.

El valor por defecto es `en`.

Ejemplo:

```gherkin
# language: es
Feature: ...
```

<br /><br />

### `dataFormatLanguage`

Establece el idioma (identificado mediante [ISO 639-1](https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes))
en el que se va a escribir los valores de los parámetros de los pasos, en los casos en los que se acepten
formatos localizados, como el caso de fechas y números que usen separadores.

El valor por defecto es el mismo valor que `language`.

Ejemplo:

```gherkin
# language: es
# dataFormatLanguage: en  
Feature: ...
```

<br /><br />

### `modules`

Restringe los pasos que se van a usar en un fichero, para evitar posibles conflictos de expresiones 
entre varios plugins que no se hayan diseñado para trabajar juntos.


Ejemplo:

```gherkin
#modules: database-steps, rest-steps
Feature: ...
```

<br /><br />

### `redefinition.stepMap`

Para los casos en los que un escenario esté representado a nivel de definición y de implementación, 
esta propiedad permite realizar la correspondencia entre los pasos de definición y de la implementación, 
en el caso que no sea una traslacion 1-a-1.

El valor debe ser una lista de números separada por `-`, en la que cada número indica la cantidad de pasos de 
implementación que corresponden al paso de definición de la misma posición. Por ejemplo, el valor `1-2-1`
implica la siguiente correspondencia:

| definición | implementación | 
|------------|----------------|
| 1          | 1              |
| 2          | 2,3            |
| 3          | 4              |

Esta propiedad se debe definicion en la parte de la implementación, encima del escenario en cuestión. 
Además, el escenario debe tener un identificador unívoco.

Ejemplo:

```gherkin
@implementation
Feature: ...

  
# redefinition.stepMap: 2-1-2  
@ID-43242   
Scenario: ...
```




---
## Tipos de datos


### `text`
Cualquier texto entrecomillado con `''`. 

Ejemplo: `'texto de ejemplo'`.

<br /><br />

### `word`
Cualquier palabra (admite guiones). 

Ejemplo: `AB_C-1D`.

<br /><br />

### `file`
Una ruta local (relativa o absoluta). 

Ejemplo: `'dir/file.yaml'`.

<br /><br />

### `url`
Una dirección URL. 

<br /><br />

Ejemplo: `https://localhost/test`.

### `integer`
Un número entero. 

Ejemplo: `14`.

<br /><br />

### `decimal`
Un número con decimales. 

Ejemplo: `14.5`.

<br /><br />

### `date`
Una fecha con formato `yyyy-MM-dd`. 

Ejemplo: `'2022-02-22'`.

<br /><br />

### `time`
Una hora con formato `hh:mm`, `hh:mm:ss` o `hh:mm:ss.SSS`. 

Ejemplo: `'12:05:06.468'`.

<br /><br />

### `datetime`
Una fecha y hora con formato `yyyy-MM-ddThh:mm`, `yyyy-MM-ddThh:mm:ss` o `yyyy-MM-ddThh:mm:ss.SSS`. 

Ejemplo: `'2022-02-22T12:05:06.468'`.

<br /><br />

### `text-assertion`
Comparador de textos. [Ver más](#comparadores). 

Ejemplo: `es igual a 'algo'`.

<br /><br />

### `long-assertion`
Comparador de números enteros. [Ver más](#comparadores). 

Ejemplo: `es igual o mayor que 13`.

<br /><br />

### `float-assertion`
Comparador de números decimales. [Ver más](#comparadores). 

Ejemplo: `es igual o menor que 10.02`.

<br /><br />

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

<br /><br />

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

<br /><br />

### `(no) es (igual a) ~x~` 
Tipo: numérico y texto. También admite las variantes `es (igual a) ~x~ \(sin distinguir mayúsculas\)` y 
`es (igual a) ~x~ \(ignorando espacios\)` para las comparaciones de tipo texto, y la versión en negativo.

#### Ejemplos
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

<br /><br />

### `(no) es (mayor|menor) que ~x~`
Tipo: numérico. También admite decimales y la versión en negativo.

#### Ejemplos
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

<br /><br />

### `(no) es (mayor|menor) o igual que ~x~`
Tipo: numérico. También admite decimales y la versión en negativo.

#### Ejemplos
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

<br /><br />

### `(no) empieza por ~x~`
Tipo: texto. También admite la variante `empieza por ~x~ \(sin distinguir mayúsculas\)`, y la versión en negativo.

#### Ejemplos
```
empieza por 'algo'
```
```
empieza por 'aLgo' (sin distinguir mayúsculas)
```
```
no empieza por 'algo'
```

<br /><br />

### `(no) acaba en ~x~`
Tipo: texto. También admite la variante `acaba en ~x~ \(sin distinguir mayúsculas\)`, y la versión en negativo.

#### Ejemplos
```
acaba en 'algo'
```
```
acaba en 'aLgo' (sin distinguir mayúsculas)
```
```
no acaba en 'algo'
```

<br /><br />

### `(no) contiene ~x~`
Tipo: texto. También admite la variante `contiene ~x~ \(sin distinguir mayúsculas\)`, y la versión en negativo.

#### Ejemplos
```
contiene 'algo'
```
```
contiene 'aLgo' (sin distinguir mayúsculas)
```
```
no contiene 'algo'
```

<br /><br />

### `(no) es nulo`
Tipo: numérico y texto. También admite la versión en negativo.

#### Ejemplos
```
es nulo
```
```
no es nulo
```

<br /><br />

### `(no) está vacío`
Tipo: numérico y texto. También admite la versión en negativo.

#### Ejemplos
```
está vacío
```
```
no está vacío
```

<br /><br />

### `(no) es nulo o está vacío`
Tipo: numérico y texto. También admite la versión en negativo.

#### Ejemplos
```
es nulo o está vacío
```
```
no es nulo o está vacío
```

---
## Propiedades dinámicas

Wakamiti permite el uso de propiedades dinámicas con las que facilitar el paso de información a la ejecución de los 
escenarios mediante la sintaxis `${[descripción de la propiedad]}`.

La `[descripción de la propiedad]` dependerá del tipo de propiedad que se desea aplicar. 

<br /><br />

### Propiedad global

Obtener el valor de una propiedad global, mediante la sintaxis `${[name]}`, donde `[name]` es el nombre de una propiedad 
que esté presente en la configuración de Wakamiti. 


#### Ejemplos
Tenemos la siguiente configuración en el fichero `wakamiti.yaml`:

```yml
wakamiti:
  resourceTypes:
    - gherkin
    
  credentials:
    username: user
    password: s3cr3t
```

Tenemos el siguiente paso:
```gherkin
Dado que el servicio usa autenticación oauth con las credenciales '${credentials.username}':'${credentials.password}'
```

Al ejecutarse, se resolvería como:
```gherkin
Dado que el servicio usa autenticación oauth con las credenciales 'user':'s3cr3t'
```

<br /><br />

### Propiedad de resultado
Obtener el resultado de un paso anterior, mediante la sintaxis `${[número del paso]#[expresión xpath/jsonpath/gpath]}`,
donde `[número de paso]` es la posición del paso del cuál se quiere recuperar el resultado, y 
`[expresión xpath/jsonpath/gpath]` es la expresión para recuperar, de forma opcional, un dato concreto cuando el 
resultado es un objeto complejo, como un xml, un json o incluso un texto. 

Ver más sobre [JSONPath][jsonpath], [XPath][xpath] o [GPath][gpath].

#### Ejemplos
Supongamos que el paso `1` devuelve lo siguiente:
```json
{
  "headers": {
    "Content-type": "json/application",
    "Connection": "Keep-alive"
  },
  "body": {
    "items": [
      {
        "id": 4,
        "name": "Pepe"
      },
      {
        "id": 7,
        "name": "Ana"
      }
    ]
  },
  "statusCode": 200,
  "statusLine": "HTTP/1.1 200 OK"
}
```

Tenemos el siguiente paso:
```gherkin
Entonces un usuario identificado por '${1#$.body.items[0].id}' existe en la tabla de BBDD USERS
```

Al ejecutarse, se resolvería como:
```gherkin
Entonces un usuario identificado por '4' existe en la tabla de BBDD USERS
```



[jsonpath]: https://goessner.net/articles/JsonPath/
[xpath]: https://en.wikipedia.org/wiki/XPath (XPath)
[gpath]: https://accenture.github.io/bdd-for-all/GPATH.html (GPath)