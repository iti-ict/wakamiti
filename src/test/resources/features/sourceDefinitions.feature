# language: es

Característica: Pruebas de extensión de Cucumber con capas de definición [Definicion]

@TestCase-1 @Definition
Escenario: Consultar un usuario que no existe
    Dado un usuario inexistente
    Cuando se consulta la información de dicho usuario
    Entonces se indica que el usuario no existe

@TestCase-2 @Definition
Esquema del escenario: Consultar un usuario existente
    Dado el usuario identificado por el código <codigo>
    Cuando se consulta dicho usuario
    Entonces se obtiene el nombre <nombre>
    Ejemplos:
    | codigo   | nombre       |
    | usuario1 | Usuario Uno  |
    | usuario2 | Usuario Dos  |

@TestCase-3 @Definition
Escenario: Cargar datos en una tabla y consultarla
    Dado que se han cargado datos en una tabla
    Cuando se consultan los datos
    Entonces se obtienen los datos cargados
