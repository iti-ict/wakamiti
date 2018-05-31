# language: es

Característica: Pruebas del soporte RESTful.
    El servidor contiene inicialmente el contenido del fichero data.json

Antecedentes:
    Dado el servicio JSON '/usuarios'

@Hola @Adios
Escenario: Consultar un usuario que no existe
    Dado un usuario inexistente identificado por el código 'usuarioX'
    Cuando se consulta dicho usuario usando el servicio
    Entonces se cumple que el servicio retorna el código HTTP 404
    Y se cumple que el servicio no retorna nada

@Hola
Esquema del escenario: Consultar un usuario existente
    Dado el usuario identificado por el código '<codigo>'
    Cuando se consulta dicho usuario usando el servicio
    Entonces se cumple que el servicio retorna el código HTTP 200
    Y se cumple que el servicio retorna datos en formato JSON
    Y se cumple que, en la respuesta, el campo 'nombre' es igual a '<nombre>'
    Ejemplos:
    | codigo   | nombre      |
    | usuario1 | Usuario Uno |
    | usuario2 | Usuario Dos |
