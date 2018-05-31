# language: es
Característica: Testeo de operaciones Cucumber en base de datos

Antecedentes: Al finalizar, se limpiará la tabla


Escenario: Cargar una tabla con Cucumber y consultarla
    Dada la tabla 'usuario'
    Y que se han cargado en la tabla los datos siguientes:
            | codigo    | nombre      | edad |
            | usuario1  | Usuario Uno |   11 |
            | usuario2  | Usuario Dos |   12 |
            | usuario3  | Usuario Tres|   13 |
    Entonces se cumple que existen en la tabla los datos siguientes:
            | codigo    | nombre      | edad |
            | usuario1  | Usuario Uno |   11 |
            | usuario2  | Usuario Dos |   12 |
            | usuario3  | Usuario Tres|   13 |


Escenario: Cargar una tabla con Cucumber, modificarla y consultarla
    Dada la tabla 'usuario'
    Y que se han cargado en la tabla los datos siguientes:
            | codigo    | nombre         | edad |
            | usuario1  | Usuario Uno    |   11 |
            | usuario2  | Usuario Dos    |   12 |
            | usuario3  | Usuario Tres   |   13 |
    Cuando se elimina de la tabla la fila en la que 'nombre='Usuario Uno''
    Y se insertan en la tabla los datos siguientes:
            | codigo    | nombre         | edad |
            | usuario4  | Usuario Cuatro |   14 |
            | usuario5  | Usuario Cinco  |   15 |
    Y se actualiza la tabla con los datos siguientes:
            | codigo    | nombre         | edad |
            | usuario2  | Usuario Dos    |   22 |
    Entonces se cumple que no existen en la tabla los datos siguientes:
            | codigo    | nombre         | edad |
            | usuario1  | Usuario Uno    |   11 |
            | usuario2  | Usuario Dos    |   12 |
    Y se cumple que no existe el identificador 'usuario1' en la tabla
    Y se cumple que existe el identificador 'usuario4' en la tabla
    Y se cumple que existen en la tabla los datos siguientes:
            | codigo    | nombre         | edad |
            | usuario2  | Usuario Dos    |   22 |
            | usuario3  | Usuario Tres   |   13 |
            | usuario4  | Usuario Cuatro |   14 |
            | usuario5  | Usuario Cinco  |   15 |


Escenario: Ejecucion de scripts
    Dado que se ha ejecutado el script SQL 'inserts.sql'
    Y que al finalizar se ejecutará el script SQL 'cleanup.sql'
    Entonces se cumple que existen en la tabla 'usuario' los datos siguientes:
            | codigo    | nombre      | edad |
            | usuario1  | Usuario Uno |   11 |
            | usuario2  | Usuario Dos |   12 |
            | usuario3  | Usuario Tres|   13 |


Escenario: Cargar una tabla desde fichero CSV
    Dada la tabla 'usuario'
    Cuando se insertan en la tabla los datos del fichero CSV 'usuario.csv'
    Entonces se cumple que existen en la tabla los datos del fichero CSV 'usuario.csv'
    Y se cumple que existen en la tabla los datos siguientes:
            | codigo    | nombre      | edad |
            | usuario1  | Usuario Uno |   11 |
            | usuario2  | Usuario Dos |   12 |
            | usuario3  | Usuario Tres|   13 |


Escenario: Cargar varias tablas desde fichero XLSX
    Cuando se insertan en la base de datos los datos del fichero XLS 'data.xlsx'
    Entonces se cumple que existen en la base de datos los datos del fichero XLS 'data.xlsx'
    Y se cumple que existen en la tabla 'usuario' los datos siguientes:
            | codigo    | nombre      | edad |
            | usuario1  | Usuario Uno |   11 |
            | usuario2  | Usuario Dos |   12 |
            | usuario3  | Usuario Tres|   13 |
    Y se cumple que existen en la tabla 'vegetal' los datos siguientes:
            | id | descripcion      |
            | 1  | Pepino           |
            | 2  | Pepinillo        |
            | 3  | Calabacín        |
            | 4  | Pimiento         |
            | 5  | Berenjena        |
