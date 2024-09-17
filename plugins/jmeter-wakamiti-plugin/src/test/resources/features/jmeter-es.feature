#language: es
@launcher
Característica: Jmeter test


  Escenario: Smoke test
    Dado APPLICATION_XML como el tipo de contenido
    Y que toda petición se considera satisfactoria si su código HTTP es menor que 300
    Y un timeout de 1 segundo
    Y las cookies están desactivadas
    Y la caché está desactivada
    Y que no se descargan los recursos embebidos
    Y la variable 'userId' con el valor 'user1'
    Y una llamada GET al servicio '/users/{userId}'
    * con los siguientes datos:
      """xml
      <user>
        <name>User1</name>
      </user>
      """
    Cuando se ejecuta 1 hilo
    Entonces el número de muestras es 1
    Y el número de errores es 0
    Y el número de muestras por segundo es mayor que 0,0


  Esquema del escenario: multiple calls
    Dado que las cookies están activadas
    Y que toda petición se considera satisfactoria si su código HTTP es menor que 300
    Y la caché está activada
    Y que se descargan los recursos embebidos
    Y que se descargan los recursos embebidos que cumplen el patrón '.*'
    Y una llamada GET al servicio '/users'
    * con las siguientes cabeceras:
      | name   | value |
      | x-data | test  |
    * con los siguientes parámetros:
      | name   | value  |
      | param1 | value1 |
    * con <extractor> en la variable 'userId'
    Y una llamada GET al servicio '/users/{userId}'
    * con la cabecera 'x-data' con el valor 'test'
    * con el parámetro 'param1' con el valor 'value1'
    Cuando se ejecuta 1 hilo
    Entonces el número de muestras es 2
    Y el número de errores es 0

    Ejemplos:
      | extractor                                         |
      | el valor json '[0].id' extraído                   |
      | la expresión regular '"id":\s*"([^"]+)"' extraída |
      | el fragmento entre '"id":"' y '"' extraído        |


  Esquema del escenario: proxy
    Dado que se descargan los recursos embebidos que no cumplen el patrón '.*'
    Y que toda petición se considera satisfactoria si su código HTTP es menor que 300
    Y un proxy con la URL <step>
    Y las siguientes variables:
      | name   | value |
      | userId | user1 |
    Y una llamada GET al servicio '/users/{userId}'
    * con los datos del fichero '${data.dir}/data/token.json'
    Cuando se ejecuta 1 hilo
    Entonces el número de muestras es 1
    Y el número de errores es 0

    Ejemplos:
      | step                                             |
      | ${jmeter.baseURL}                                |
      | ${jmeter.baseURL} y las credenciales 'abc':'123' |


  Esquema del escenario: authentication <info>
    Dado que el servicio usa autenticación <authentication>
    Y que toda petición se considera satisfactoria si su código HTTP es menor que 300
    Y una llamada GET al servicio '/users'
    Cuando se ejecuta 1 hilo
    Entonces el número de muestras es 1
    Y el número de errores es 0

    Ejemplos:
      | authentication                                               | info        |
      | básica con las credenciales 'abc':'123'                      | basic       |
      | oauth con el token 'abc123'                                  | token       |
      | oauth con el token del fichero '${data.dir}/data/token.json' | token       |
      | oauth                                                        | default     |
      | oauth con las credenciales 'abc':'123'                       | credentials |
      | oauth con las credenciales del cliente                       | client      |


  Esquema del escenario: authentication <info> with parameters
    Dado que el servicio usa autenticación <authentication> y los siguientes parámetros:
      | name  | value |
      | scope | test  |
    Y una llamada GET al servicio '/users'
    Cuando se ejecuta 1 hilo
    Entonces el número de muestras es 1
    Y el número de errores es 0

  Ejemplos:
    | authentication                         | info        |
    | oauth con las credenciales 'abc':'123' | credentials |
    | oauth con las credenciales del cliente | client      |


  Escenario: without authentication
    Dado que el servicio no usa autenticación
    Y que toda petición se considera satisfactoria si su código HTTP es igual a 204
    Y una llamada GET al servicio '/users'
    Cuando se ejecuta 1 hilo
    Entonces el número de muestras es 1
    Y el número de errores es 1


  Escenario: form
    Dado que toda petición se considera satisfactoria si su código HTTP es menor que 300
    Y la URL base http://localhost:8888/
    Y una llamada POST al servicio '/token'
    * con los siguientes parámetros de formulario:
      | name     | value |
      | clientId | WEB   |
    * con el parámetro de formulario 'clientSecret' con el valor 's3cr3t'
    * con el fichero adjunto '${data.dir}/data/token.json' con nombre 'file'
    Cuando se ejecuta 1 hilo
    Entonces el número de muestras es 1
    Y el número de errores es 0


  Esquema del escenario: csv
    Dados los datos del fichero '${data.dir}/data/users.csv'
    Y que toda petición se considera satisfactoria si su código HTTP es menor que 300
    Y una llamada GET al servicio '/users/{userId}'
    Cuando se ejecuta 1 hilo en <test>
    Entonces el número de muestras es 3
    Y el número de errores es 0

    Ejemplos:
      | test                            |
      | 1 segundo manteniendo 1 segundo |
      | 1 segundo 1 vez                 |
      | 1 segundo cada 1 segundo 1 vez  |


  Escenario: csv stretches
    Dados los datos del fichero '${data.dir}/data/users.csv'
    Y que toda petición se considera satisfactoria si su código HTTP es menor que 300
    Y una llamada GET al servicio '/users/{userId}'
    Cuando se ejecuta el siguiente tramo:
      | threads | ramp     | hold     |
      | 1       | 1 second | 1 second |
    Entonces el número de muestras es 3
    Y el número de errores es 0
