---
title: Primeros pasos
date: 2022-09-20
slug: /introduction/getting-started
---

En este tutorial rápido aprenderás a:
- Definir la configuración básica.
- Definir un escenario.
- Lanzar wakamiti.
- Conocer el flujo de trabajo básico.

Ten en cuenta que este tutorial asume que tienes:
- Algo de experiencia usando un terminal.
- Algo de experiencia usando un editor de texto.
- Comprensión básica de la sintaxis de `gherkin`.

Antes de empezar, será necesario tener instaladas las siguientes aplicaciones:
- [Docker](https://www.docker.com/get-started/)
- Un IDE, como por ejemplo:
  - [IntelliJ IDEA](https://www.jetbrains.com/idea/)
  - [VS Code](https://code.visualstudio.com/)


### 1. Configurar Wakamiti
La configuración de Wakamiti se realiza mediante un fichero `yaml` que se situará en el directorio raíz de la carpeta 
donde se ubiquen los tests, por ejemplo:
```
.
├── features
│   └── example.feature
└── wakamiti.yaml
```

Esta es la configuración básica para poder ejecutar los tests:
```yml
wakamiti:
  resourceTypes:
    - gherkin
  launcher:
    modules:
      - mysql:mysql-connector-java:8.0.28
      - es.iti.wakamiti:wakamiti-rest:2.3.0
      - es.iti.wakamiti:wakamiti-db:2.3.0
      - es.iti.wakamiti:wakamiti-html-report:2.3.0
  htmlReport:
    title: Test
  rest:
    baseURL: https://localhost
  database:
    connection:
      url: jdbc:mysql://localhost:3306
      username: user
      password: p4ssw0rd
```
Ten en cuenta que cada plugin tiene su propia configuración, la cual puedes consultar en [sus respectivos apartados](plugins).
También puedes consultar otras opciones de [configuración global](architecture#configuración-global).


### 2. Definir escenario
Cuando hacemos *Behaviour-Driven Development* utilizamos ejemplos concretos para especificar lo que queremos que haga el 
software. Los escenarios se escriben antes que el código de producción. Comienzan su vida como una especificación 
ejecutable. Cuando llega el código a producción, los escenarios adquieren un papel como documentación viva y pruebas 
automatizadas.

Un escenario pertenece a una característica concreta del software. Cada característica puede contener muchos escenarios, 
y se definen en archivos `.feature` que deberán estar en nuestro directorio de trabajo (o subdirectorio).

Un ejemplo concreto sería consultar un propietario de una mascota.

Crea un archivo vacío llamado `example.feature` con el siguiente contenido:
```gherkin
# language: es
Característica: Consultar propietarios
  
  Escenario: Se consulta un dueño existente
    Dado el servicio REST '/owners/{id}'
    Y el parámetro de ruta 'id' con el valor '20'
    Y que se ha insertado el siguiente usuario en la tabla de BBDD owners:
      | ID  | FIRST_NAME | LAST_NAME      |
      | 20  | Pepe       | Perez Martínez |
    Cuando se consulta el usuario
    Entonces el código de respuesta HTTP es 200
    Y la respuesta es parcialmente:
      """json
      {
        "id": 20,
        "firstName": "Pepe",
        "lastName": "Perez Martínez"
      }
      """
```
La segunda línea de este archivo comienza con la palabra clave `Característica:` seguida de un nombre. Es una buena idea
usar un nombre similar al nombre del fichero.

La cuarta línea, `Escenario: Se consulta un dueño existente`, es un escenario, que es un ejemplo concreto que ilustra 
cómo se debe comportar el software.

El resto de líneas que comienzan con `Dado`, `Cuando`, `Entonces`, `Y` son los pasos de nuestro escenario, y es lo que 
Wakamiti ejecutará.

[Ver más](https://cucumber.io/docs/gherkin/) en detalle la sintaxis de `gherkin`.


### 3. Lanzar Wakamiti
Los test se ejecutan con el terminal desde el directorio que contiene las características de Wakamiti con el siguiente 
comando:

* Windows:
```Shell
docker run --rm -v "%cd%:/wakamiti" wakamiti/wakamiti
```
* Linux:
```Shell
docker run --rm -v "$(pwd):/wakamiti" wakamiti/wakamiti
```
Con este comando, se bajará la última versión de wakamiti, para trabajar con una versión específica, se debe de indicar 
en el comando docker: `wakamiti/wakamiti:version`, se pueden ver las versiones disponibles en el repositorio
de [dockerhub de Wakamiti](https://hub.docker.com/r/wakamiti/wakamiti/tags).


### 4.Informes
Una vez ejecutados los tests, se generarán los resultados en dos formatos: `wakamiti.json` y `wakamiti.html`.

Los estados que existen actualmente en Wakamiti son:

- <span style="color:#5fc95f">PASSED</span>: el caso de test está correcto, se recibe del sistema el mismo resultado 
  que se espera.
- <span style="color:#4fc3f7">NOT IMPLEMENTED</span>: existe el caso de test, pero no están definidos sus pasos.
- <span style="color:#9e9e9e">SKIPPED</span>: no se ha ejecutado el caso de test.
- <span style="color:#ffc107">UNDEFINED</span>: no existe el paso en Wakamiti.
- <span style="color:#ff7b7e">FAILED</span>: hay un error de comprobación, no coincide lo que se espera de lo que 
  devuelve el sistema.
- <span style="color:#ff0000">ERROR</span>: hay un error no esperado en el sistema (error de conexión, error en base de 
  datos, error time out...).
