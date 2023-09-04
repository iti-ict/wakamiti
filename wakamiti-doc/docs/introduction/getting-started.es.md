---
title: Primeros pasos
date: 2022-09-20
slug: /introduction/getting-started
---

### 1. Configurar Wakamiti en el proyecto
Para configurar Wakamiti es necesario un fichero de configuración que se situará en el directorio raíz de la carpeta 
dónde se ubiquen los tests. 
Para tener los tests organizados, se necesita una estructura de carpetas, por ejemplo:
```
> src
> wakamiti
    > data
    > features
    > steps
    > results
    wakamiti.yaml
```
Esta es la configuración básica del fichero de configuración para poder ejecutar los tests:
```
wakamiti:
  resourceTypes:
    - gherkin
  tagFilter:  
  log:
    level: debug
  launcher:
    modules:
      - mysql:mysql-connector-java:8.0.28
      - es.iti.wakamiti:wakamiti-rest:2.0.0
      - es.iti.wakamiti:wakamiti-db:2.0.0
      - es.iti.wakamiti:wakamiti-html-report:2.0.0
  outputFilePath: result/wakamiti.json
  htmlReport:
    title: Test
    output: result/ResultTest.html
  rest:
    baseURL: https://localhost
  database:
    connection:
      url: jdbc:mysql://localhost:3306
      username: user
      password: p4ssw0rd
```
[Consulta otras opciones de configuración](setup/configuration.es)

### 2. Desarrollar un caso de test
Una vez hecha la configuración y teniendo clara la estructura de carpetas, se puede empezar a desarrollar los tests. 
Dentro de cada característica, se pueden definir varios escenarios para cubrir la funcionalidad a probar.
Como mínimo debe de haber un escenario por característica, si no hay ningún escenario definido, Wakamiti interpretará la 
característica como "no implementada" y así se mostrará en el informe final.
La característica se organiza como la estructura de Gherkin, para más información, se puede consultar la web 
de [Gherkin](https://cucumber.io/docs/gherkin/)
````
# language: es
Característica: Consultas los propietarios de las mascotas
-------------------------------------------------
    Antecedentes:
        Dado el servicio REST '/owners'

   @ID-consultaExistente
    Escenario: Se consulta un dueño existente
        Dado un usuario identificado por '20'
        Y que se ha insertado los siguientes datos en la tabla de BBDD owners:
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
````
### 3. Ejecutar los tests
Los test se ejecutan desde el directorio que contiene las características de Wakamiti con el siguiente comando:
* Windows:
```Shell
docker run --rm -it -v "%cd%:/wakamiti" wakamiti/wakamiti
```
* Linux:
```Shell
docker run --rm -v "$(pwd):/wakamiti" wakamiti/wakamiti
```
Con este comando, se bajará la última versión de wakamiti, para trabajar con una versión específica, se debe de indicar 
en el comando docker: ```wakamiti/wakamiti:version```, se pueden ver las versiones disponibles en el repositorio
de [dockerhub de Wakamiti](https://hub.docker.com/r/wakamiti/wakamiti/tags).


### 4.Informes
Una vez ejecutados los tests, se generan los resultados en dos formatos: .json y .html. Estos ficheros estarán
en el directorio que se indique en el apartardo  ```outputFilePath``` y ``` htmlReport``` del fichero de configuración.

Los estados que existen actualmente en Wakaimiti son:

- <span style="color:#32cd32">PASSED</span> : el caso de test está correcto, se recibe del sistema el mismo resultado que se espera
- <span style="color:#f08080">FAILED</span> : hay un error de comprobación, no coincide lo que se espera de lo que devuelve el sistema
- <span style="color:#808080">SKIPPED</span> : no se ha ejecutado el caso de test
- <span style="color:#ff0000">ERROR</span> : hay un error de sistema, error de conexión a la base de datos, error de duplicidad de claves, error time out
- <span style="color:#ffd700">UNDEFINED</span> : no existe el paso en Wakamiti
- <span style="color:#87cefa"> NOT IMPLEMENTED</span>: existe la característica pero no hay caso de test definido