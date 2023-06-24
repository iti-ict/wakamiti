---
title: Arquitectura
date: 2022-09-20
slug: /wakamiti/architecture
---




- [Configuración global](#configuracin-global)
- [Configuración de features](#configuracin-de-features)
- [Tipos de datos](#tipos-de-datos)
- [Comparadores](#comparadores)
- [Propiedades dinámicas](#propiedades-dinmicas)



---


## Configuración global

La configuración de Wakamiti se establece mediante un fichero `yaml` ubicado en el directorio de pruebas. Por defecto, 
Wakamiti buscará el fichero con el nombre `wakamiti.yaml`.


- [`wakamiti.resourceTypes`](#wakamitiresourcetypes)
- [`wakamiti.resourcePath`](#wakamitiresourcepath)
- [`wakamiti.outputFilePath`](#wakamitioutputfilepath)
- [`wakamiti.outputFilePerTestCase`](#wakamitioutputfilepertestcase)
- [`wakamiti.tagFilter`](#wakamititagfilter)
- [`wakamiti.idTagPattern`](#wakamitiidtagpattern)
- [`wakamiti.strictTestCaseID`](#wakamitistricttestcaseid)
- [`wakamiti.launcher.modules`](#wakamitilaunchermodules)
- [`wakamiti.report.generation`](#wakamitireportgeneration)
- [`wakamiti.redefinition.definitionTag`](#wakamitiredefinitiondefinitiontag)
- [`wakamiti.redefinition.implementationTag`](#wakamitiredefinitionimplementationtag)
- [`wakamiti.log.path`](#wakamitilogpath)
- [`wakamiti.log.level`](#wakamitiloglevel)
- [`wakamiti.logs.ansi.enabled`](#wakamitilogsansienabled)
- [`wakamiti.logs.showLogo`](#wakamitilogsshowlogo)
- [`wakamiti.logs.showElapsedTime`](#wakamitilogsshowelapsedtime)
- [`wakamiti.junit.treatStepsAsTests`](#wakamitijunittreatstepsastests)
- [`wakamiti.nonRegisteredStepProviders`](#wakamitinonregisteredstepproviders)
- [`mavenFetcher.remoteRepositories`](#mavenfetcherremoterepositories)
- [`mavenFetcher.localRepository`](#mavenfetcherlocalrepository)



---
### `wakamiti.resourceTypes`

Establece el lenguaje de los escenarios de prueba. Actualmente solo está disponible `gherkin`.

Ejemplo:

```yaml
wakamiti:
  resourceTypes: gherkin
```
&nbsp;


### `wakamiti.resourcePath`

Establece la ruta donde se encuentran los ficheros de las pruebas.

El valor por defecto es `.` (ruta donde se encuentra el fichero de configuración).

Ejemplo:

```yaml
wakamiti:
  resourcePath: /other/path
```

---
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

---
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

---
### `wakamiti.tagFilter`

Filtra los escenarios etiquetados con la [expresión](https://cucumber.io/docs/cucumber/api/#tag-expressions) indicada. 

Ejemplo:

```yaml
wakamiti:
  tagFilter: not Ignore
```

---
### `wakamiti.idTagPattern`

Establece el patrón de identificadores de los escenarios. Debe contener una expresión regular válida.

El valor por defecto es `ID-(\w*)`.

Ejemplo:

```yaml
wakamiti:
  idTagPattern: ([0-9]+)
```


---
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







---
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

---
### `wakamiti.report.generation`

Indica si se generará el informe con el resultado de las pruebas al terminar la ejecución o no.

El valor por defecto es `true`.

Ejemplo:

```yaml
wakamiti:
  report: 
    generation: "false"
```

---
### `wakamiti.redefinition.definitionTag`

Establece la etiqueta para indicar que la **caracteristica** es una [definición]().

El valor por defecto es `definition`.

Ejemplo:

```yaml
wakamiti:
  redefinition:
    definitionTag: def
```

---
### `wakamiti.redefinition.implementationTag`

Establece la etiqueta para indicar que la **caracteristica** es una [implementación]().

El valor por defecto es `implementation`.

Ejemplo:

```yaml
wakamiti:
  redefinition:
    implementationTag: impl
```

---
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

---
### `wakamiti.log.path`

Establece el directorio donde se creará un fichero de log con nombre `wakamiti-${yyyyMMddhhmmss}.log`, donde 
`${yyyyMMddhhmmss}` es el patrón de la fecha del sistema. Por defecto no se creará el log.

Ejemplo:

```yaml
wakamiti:
  log:
    path: results
```


---
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




---
### `wakamiti.logs.ansi.enabled` 

Establece si los logs por consola deben usar [códigos de control ANSI](https://en.wikipedia.org/wiki/ANSI_escape_code).

El valor por defecto es `true`.

Ejemplo:

```yaml
wakamiti:
  logs:
    ansi.enabled: true
```



---
### `wakamiti.logs.showLogo`

Establece si se debe mostrar el logo de Wakamiti en los logs por consola al inicio de la ejecución.

El valor por defecto es `true`.

Ejemplo:

```yaml
wakamiti:
  logs:
    showLogo: true
```




---
### `wakamiti.logs.showElapsedTime` 

Establece si se debe mostrar los tiempos transcurridos en los logs por consola.

El valor por defecto es `true`.

Ejemplo:

```yaml
wakamiti:
  logs:
    showElapsedTime: true
```




---
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






---
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







## Configuración de features

Adicionalmente a la configuración global, se pueden incluir propiedades específicas en cada
fichero feature

- [`language`](#language)
- [`dataFormatLanguage`](#language)
- [`modules`](#modules)
- [`redefinition.stepMap`](#redefinitionstepmap)


---
### `language`

Establece el idioma (identificado mediante [ISO 639-1](https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes))
en el que se va a escribir los escenarios del fichero.

El valor por defecto es `en`.

Ejemplo:

```gherkin
# language: es
Feature: ...
```


---
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


---
### `modules`

Restringe los pasos que se van a usar en un fichero, para evitar posibles conflictos de expresiones 
entre varios plugins que no se hayan diseñado para trabajar juntos.


Ejemplo:

```gherkin
#modules: database-steps, rest-steps
Feature: ...
```


---
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

-------
## Propiedades dinámicas

Wakamiti permite el uso de propiedades dinámicas con las que facilitar el paso de información a la ejecución de los 
escenarios mediante la sintaxis `${[descripción de la propiedad]}`.

La `descripción de la propiedad` dependerá del tipo de propiedad que se desea aplicar. Por defecto, exsisten las 
siguientes:
- Obtener el valor de una propiedad global, mediante la sintaxis `${[name]}`. Por ejemplo: `${credential.password}`.
- Obtener el resultado de un paso anterior, mediante la sintaxis `${[número del paso]#[expresión xpath/jsonpath]`. 
Por ejemplo: `${2#$.body.items[0].id}`, en este ejemplo se recuperará el resultado del paso 2 (el cual se espera que sea 
un json) y se recuperará el valor de la expresión jsonpath indicada. **Nota**: la expresión xpath/jsonpath es opcional.

Veamos un ejemplo práctico:
- Tenemos la siguiente configuración yaml:
```yml
wakamiti:
  credentials:
    username: pepe
    password: 1234asdf
```
- Tenemos el siguiente escenario:
```cucumber
Escenario: Escenario de prueba
  Dado que el servicio usa autenticación oauth con las credenciales '${credentials.username}':'${credentials.password}'
  Cuando se realiza la búsqueda de los usuarios
  Entonces un usuario identificado por '${2#$.body.items[0].id}' existe en la tabla de BBDD USERS
```

Al lanzar, el escenario se resolvería de la siguiente manera:
```cucumber
Escenario: Escenario de prueba
  Dado que el servicio usa autenticación oauth con las credenciales 'pepe':'1234asdf'
  Cuando se realiza la búsqueda de los usuarios
  Entonces un usuario identificado por '4' existe en la tabla de BBDD USERS
```

**Nota**: Supondremos que el paso `Cuando se realiza la búsqueda de los usuarios` nos devuelve el siguiente resultado:
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