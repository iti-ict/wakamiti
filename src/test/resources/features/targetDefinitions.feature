# language: es

Característica: Pruebas de extensión de Cucumber con capas de definición [Implementacion]

Antecedentes:
    Dado el servicio JSON '/usuarios'

@TestCase-1 @Implementation
Escenario: Consultar un usuario que no existe
    # Dado un usuario inexistente
    Dado un usuario inexistente identificado por el código 'usuarioX'
    # Cuando se consulta la información de dicho usuario
    Cuando se consulta dicho usuario usando el servicio
    # Entonces se indica que el usuario no existe
    Entonces se cumple que el servicio retorna el código HTTP 404
    Y se cumple que el servicio no retorna nada


@TestCase-2 @Implementation
Esquema del escenario: Consultar un usuario existente
    # Dado el usuario identificado por el código <codigo>
    Dado el usuario identificado por el código '<codigo>'
    # Cuando se consulta dicho usuario
    Cuando se consulta dicho usuario usando el servicio
    # Entonces se obtiene el nombre <nombre>
    Entonces se cumple que el servicio retorna el código HTTP 200
    Y se cumple que el servicio retorna datos en formato JSON
    Y se cumple que, en la respuesta, el campo 'nombre' es igual a '<nombre>'


@TestCase-3 @Implementation
Escenario: Cargar datos en una tabla y consultarla
    # Dado que se han cargado datos en una tabla
    Dada la tabla 'usuario'
    Y que al finalizar, se limpiará la tabla
    Y que se han cargado en la tabla los datos siguientes:
            | codigo    | nombre      | edad |
            | usuario1  | Usuario Uno |   11 |
            | usuario2  | Usuario Dos |   12 |
            | usuario3  | Usuario Tres|   13 |
    # Cuando se consultan los datos
    # Entonces se obtienen los datos cargados
    Entonces se cumple que existen en la tabla los datos siguientes:
            | codigo    | nombre      | edad |
            | usuario1  | Usuario Uno |   11 |
            | usuario2  | Usuario Dos |   12 |
            | usuario3  | Usuario Tres|   13 |
