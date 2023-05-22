---
title: Pasos BD
date: 2022-09-20
slug: /plugins/database
---

Este plugin proporciona una serie de pasos para interactuar con una base de datos vía JDBC, facilitando la carga y
validación de datos.

> **RECUERDA** <br />
> Debido a los muchos motores existentes de bases de datos, este plugin no incluye ningún driver específico. Esto
> significa que, para funcionar correctamente, es necesario inluir el módulo con el controlador JDBC adecuado en la
> configuración de Wakamiti.



**Configuración**:
- [`database.connection.url`](#databaseconnectionurl)
- [`database.connection.username`](#databaseconnectionusername)
- [`database.connection.password`](#databaseconnectionpassword)
- [`database.metadata.schema`](#databasemetadataschema)
- [`database.metadata.catalog`](#databasemetadatacatalog)
- [`database.metadata.caseSensitivity`](#databasemetadatacasesensititvy)
- [`database.csv.format`](#databasecsvformat)
- [`database.nullSymbol`](#databasenullsymbol)
- [`database.enableCleanupUponCompletion`](#databaseenablecleanupuponcompletion)

**Pasos**:
- [Definir conexión](#definir-conexi%C3%B3n)
- [Definir script post ejecución](#definir-script-post-ejecuci%C3%B3n)
- [Definir script post ejecución (fichero)](#definir-script-post-ejecuci%C3%B3n-fichero)
- [Ejecutar script](#ejecutar-script)
- [Ejecutar script (fichero)](#ejecutar-script-fichero)
- [Vaciar tabla](#vaciar-tabla)
- [Eliminar datos](#eliminar-datos)
- [Eliminar datos (tabla)](#eliminar-datos-tabla)
- [Eliminar datos (xls)](#eliminar-datos-xls)
- [Eliminar datos (csv)](#eliminar-datos-csv)
- [Insertar datos (tabla)](#insertar-datos-tabla)
- [Insertar datos (xls)](#insertar-datos-xls)
- [Insertar datos (csv)](#insertar-datos-csv)
- [Comprobar existencia de datos](#comprobar-existencia-de-datos)
- [Comprobar inexistencia de datos](#comprobar-inexistencia-de-datos)
- [Comprobar existencia de datos (id)](#comprobar-existencia-de-datos-id)
- [Comprobar inexistencia de datos (id)](#comprobar-inexistencia-de-datos-id)
- [Comprobar existencia de datos (tabla)](#comprobar-existencia-de-datos-tabla)
- [Comprobar inexistencia de datos (tabla)](#comprobar-inexistencia-de-datos-tabla)
- [Comprobar existencia de datos (XLS)](#comprobar-existencia-de-datos-xls)
- [Comprobar inexistencia de datos (XLS)](#comprobar-inexistencia-de-datos-xls)
- [Comprobar existencia de datos (CSV)](#comprobar-existencia-de-datos-csv)
- [Comprobar inexistencia de datos (CSV)](#comprobar-inexistencia-de-datos-csv)
- [Comprobar existencia de datos (documento)](#comprobar-existencia-de-datos-documento)
- [Comprobar inexistencia de datos (documento)](#comprobar-inexistencia-de-datos-documento)
- [Comprobar número de filas](#comprobar-n%C3%BAmero-de-filas)
- [Comprobar número de filas (documento)](#comprobar-n%C3%BAmero-de-filas-documento)
- [Comprobar número de filas (tabla)](#comprobar-n%C3%BAmero-de-filas-documento)
- [Comprobar tabla vacía](#comprobar-tabla-vac%C3%ADa)
- [Comprobar tabla no vacía](#comprobar-tabla-no-vac%C3%ADa)




## Configuración

---
### `database.connection.url`
Establece la URL de conexión a la base de datos con esquema JDBC. El driver usado para acceder a la base de datos se
determinará a partir del formato de URL indicado.

Ejemplo:
```yaml
database:
  connection:
    url: jdbc:h2:tcp://localhost:9092/~/test
```

---
### `database.connection.username`
Establece el nombre de usuario requerido para conectar a la base de datos.

Ejemplo:
```yaml
database:
  connection:
    username: test
```

---
### `database.connection.password`
Establece la contraseña requerida para conectar a la base de datos.

Ejemplo:
```yaml
database:
  connection:
    password: test
```

---
### `database.metadata.schema`
Establece manualmente el esquema de base de datos que se usará para recuperar metadatos como claves privadas y/o
nulabilidad. Si no se indica, se usará el esquema por defecto que retorne la conexión.

Ejemplo:
```yaml
database:
  metadata:
    schema: TESTDB
```

---
### `database.metadata.catalog`
Establece manualmente el catálogo de base de datos (si el motor soporta esta característica) que se usará para recuperar
metadatos como claves privadas y/o nulabilidad. Si no se indica, se usará el esquema por defecto que retorne la conexión.

Ejemplo:
```yaml
database:
  metadata:
    catalog: TESTCAT
```

---
### `database.metadata.caseSensititvy`
Establece si se debería hacer alguna transformación de mayúsculas/minúsculas cuando se intente acceder a los metadatos.
Algunos motores son estrictos con respecto a esto, y puede causar errores inesperados si no se configura correctamente.
Los posibles valores son:
- `INSENSITIVE`: Los identificadores se mantendrán como estén escritos.
- `LOWER_CASED`: Los identificadores se convertirán a minúsculas.
- `UPPER_CASED`: Los identificadores se convertirán a mayúsculas.

Ejemplo:
```yaml
database:
  metadata:
    caseSensitivity: UPPER_CASED
```

---
### `database.csv.format`

Establece la variante de formato usado a la hora de leer ficheros CSV. Los valores aceptados son directamente los usados
por el projecto [Commons CSV][1] (consultar el enlace para una explicación exhaustiva de cada variante). Los posibles
valores son:
- `DEFAULT`
- `INFORMIX_UNLOAD`
- `INFORMIX_UNLOAD_CSV`
- `MYSQL`
- `ORACLE`
- `POSTGRESQL_CSV`
- `POSTGRESQL_TEXT`
- `RFC4180`

El valor por defecto es `DEFAULT`.

Ejemplo:
```yaml
database:
  csv:
    format: ORACLE
```

---
### `database.xls.ignoreSheetPattern`
Establece la expresión regular usada para determinar qué hojas se deberían ignorar cuando se cargan datos de un fichero
XLSX.

El valor por defecto es `#.*`.

Ejemplo:
```yaml
database:
  xls:
    ignoreSheetPattern: //.*
```

---
### `database.nullSymbol`
Establece el literal usado para marcar una celda específica con el valor correspondiente al `NULL` de SQL. Se usa en
cualquier orígen de datos (ficheros CSV, XLSX, y tablas embebidas en la definición del test).

El valor por defecto es `<null>`.

Ejemplo:
```yaml
database:
  nullSymbol: (null)
```

---
### `database.enableCleanupUponCompletion`
El comportamiento por defecto del plugin no realiza ninguna operación de limpieza de la base de datos al acabar la
ejecución de los tests. Esto es así para poder comprobar resultados manualmente y depurar errores. Los posibles valores
son:
- `false`: no se realizará ninguna acción de limpieza.
- `true`: se forzará a limpiar la base de datos borrando los datos de prueba introducidos durante la ejecución.

El valor por defecto es `false`.

Ejemplo:
```yaml
database:
  enableCleanupUponCompletion: "true"
```



## Pasos

---
### Definir conexión
```
la URL de conexión a BBDD {url} usando el usuario {username} y la contraseña {password}
```
Establece la URL, nombre de usuario y contraseña para conectarse a la base de datos. Este paso es equivalente a
configurar las propiedades [`database.connection.url`](#databaseconnectionurl),
[`database.connection.username`](#databaseconnectionusername),
[`database.connection.password`](#databaseconnectionpassword).

#### Parámetros:
| Nombre     | Wakamiti type | Descripción           |
|------------|-------------|-----------------------|
| `url`      | `text`      | La URL de conexión    |
| `username` | `text`      | Nombre de usuario     |
| `password` | `text`      | Contraseña de usuario |

#### Ejemplos:
```gherkin
  Dada la URL de conexión a BBDD 'jdbc:h2:tcp://localhost:9092/~/TESTDB' usando el usuario 'test' y la contraseña 'test'
```

---
### Definir script post ejecución
```
Al finalizar, se ejecuta el siguiente script SQL:
```
Establece las sentencias SQL que se ejecutarán una vez finalizado el escenario, independientemente del estado de la
ejecución.

#### Parámetros:
| Nombre | Wakamiti type | Descripción          |
|--------|-------------|----------------------|
|        | `document`  | Contenido del script |

#### Ejemplos:
```gherkin
  * Al finalizar, se ejecuta el siguiente script SQL:
    """
    UPDATE AAAA SET STATE = 2 WHERE ID = 1;
    DELETE FROM BBBB WHERE ID = 2;
    """
```

---
### Definir script post ejecución (fichero)
```
Al finalizar, se ejecuta el script SQL del fichero {file}
```
Establece las sentencias SQL del fichero indicado que se ejecutarán una vez finalizado el escenario, independientemente
del estado de la ejecución.

#### Parámetros:
| Nombre | Wakamiti type | Descripción |
|--------|-------------|-------------|
| `file` | `file`      | Fichero SQL |

#### Ejemplos:
```gherkin
  * Al finalizar, se ejecuta el script SQL del fichero 'data/insert-users.sql'
```

---
### Ejecutar script
```
se ejecuta el siguiente script SQL:
```
Ejecuta el script SQL escrito a continuación.

#### Parámetros:
| Nombre | Wakamiti type | Descripción          |
|--------|-------------|----------------------|
|        | `document`  | Contenido del script |

#### Ejemplos:
```gherkin
  Cuando se ejecuta el siguiente script SQL:
    """sql
    UPDATE USER SET STATE = 2 WHERE BLOCKING_DATE IS NULL;
    DELETE FROM USER WHERE STATE = 3;
    """
```

---
### Ejecutar script (fichero)
```
se ejecuta el script SQL del fichero {file}
```
Ejecuta el script SQL existente en el fichero indicado.

#### Parámetros:
| Nombre | Wakamiti type | Descripción |
|--------|-------------|-------------|
| `file` | `file`      | Fichero SQL |

#### Ejemplos:
```gherkin
  Cuando se ejecuta el script SQL del fichero 'data/insert-users.sql'
```

---
### Vaciar tabla
```
se limpia la tabla de BBDD {word}
```
Limpia la tabla indicada, intentando primero con la sentencia `TRUNCATE` , y con la sentencia `DELETE FROM` en caso de
fallar la primera.

#### Parámetros:
| Nombre | Wakamiti type | Descripción        |
|--------|-------------|--------------------|
| `word` | `word`      | Nombre de la tabla |

#### Ejemplos:
```gherkin
  Cuando se limpia la tabla de BBDD USERS
```

---
### Eliminar datos
```
se (ha) elimina(n|do) * con {column} = {value} de la tabla de BBDD {table}
```
Elimina de una tabla dada las filas que satisfagan la comparación indicada.

#### Parámetros:
| Nombre   | Wakamiti type | Descripción          |
|----------|-------------|----------------------|
| `column` | `word`      | Nombre de la columna |
| `value`  | `text`      | Valor de la columna  |
| `table`  | `word`      | Nombre de la tabla   |

#### Ejemplos:
```gherkin
  Cuando se eliminan los usuarios con STATE = '2' de la tabla de BBDD USER 
```
```gherkin
  Cuando se ha eliminado los usuarios con STATE = '2' de la tabla de BBDD USER 
```

---
### Eliminar datos (tabla)
```
se (ha) elimina(n|do) (lo|el|la|los|las) siguiente(s) * de la tabla de BBDD {word}:
```
Elimina de una tabla dada las filas indicadas.

#### Parámetros:
| Nombre | Wakamiti type | Descripción         |
|--------|-------------|---------------------|
| `word` | `word`      | Nombre de la tabla  |
|        | `table`     | Tabla con los datos |

#### Ejemplos:
```gherkin
  Cuando se elimina los siguientes usuarios de la tabla de BBDD USER:
    | USER  | STATE | BLOCKING_DATE |   
    | user1 | 2     | <null>        |
    | user2 | 3     | 2020-02-13    |
```
```gherkin
  Cuando se ha eliminado lo siguiente de la tabla de BBDD USER:
    | USER  | STATE | BLOCKING_DATE |   
    | user1 | 2     | <null>        |
```

---
### Eliminar datos (XLS)
```
se (ha) elimina(do) el contenido del fichero XLS {file} de la BBDD
```
Elimina las filas que concuerdan con los datos del fichero XLS indicado.

#### Parámetros:
| Nombre | Wakamiti type | Descripción |
|--------|-------------|-------------|
| `file` | `file`      | Fichero XLS |

#### Ejemplos:
```gherkin
  Cuando se elimina el contenido del fichero XLS 'data/users.xls' de la BBDD
```
```gherkin
  Cuando se ha eliminado el contenido del fichero XLS 'data/users.xls' de la BBDD
```

---
### Eliminar datos (CSV)
```
se (ha) elimina(do) el contenido del fichero CSV {csv} de la tabla de BBDD {table}
```
Elimina las filas de una tabla dada que concuerdan con los datos del fichero CSV proporcionado.

#### Parámetros:
| Nombre  | Wakamiti type | Descripción        |
|---------|-------------|--------------------|
| `csv`   | `file`      | Fichero CSV        |
| `table` | `word`      | Nombre de la tabla |

#### Ejemplos:
```gherkin
  Cuando se elimina el contenido del fichero CSV 'data/users.csv' de la tabla de BBDD USER
```
```gherkin
  Cuando se ha eliminado el contenido del fichero CSV 'data/users.csv' de la tabla de BBDD USER
```

---
### Insertar datos (tabla)
```
se (ha) inserta(do) (lo|el|la|los|las) siguiente(s) * en la tabla de BBDD {word}:
```
Inserta las filas indicadas a continuación una tabla dada. Las columnas no-nulables para las que no se proporcionen
datos se rellenarán con datos aleatorios.

#### Parámetros:
| Nombre | Wakamiti type | Descripción         |
|--------|-------------|---------------------|
| `word` | `word`      | Nombre de la tabla  |
|        | `table`     | Tabla con los datos |

#### Ejemplos:
```gherkin
  Cuando se inserta lo siguiente en la tabla de BBDD USER:
    | USER  | STATE | BLOCKING_DATE |   
    | user1 | 2     | <null>        |
    | user2 | 3     | 2020-02-13    |
```
```gherkin
  Cuando se ha insertado los siguientes usuarios en la tabla de BBDD USER:
    | USER  | STATE | BLOCKING_DATE |   
    | user1 | 2     | <null>        |
    | user2 | 3     | 2020-02-13    |
```

---
### Insertar datos (XLS)
```
se (ha) inserta(do) el contenido del fichero XLS {file} en la BBDD
```
Inserta las filas contenidas en el fichero XLS indicado, una hoja por tabla. Las columnas no-nulables para las que no se
proporcionen datos se rellenarán con datos aleatorios.

#### Parámetros:
| Nombre | Wakamiti type | Descripción |
|--------|-------------|-------------|
| `file` | `file`      | Fichero XLS |

#### Ejemplos:
```gherkin
  Cuando se inserta el contenido del fichero XLS 'data/users.xls' en la BBDD
``` 
```gherkin
  Cuando se ha insertado el contenido del fichero XLS 'data/users.xls' en la BBDD
``` 

---
### Insertar datos (CSV)
```
se (ha) inserta(do) el contenido del fichero CSV {csv} en la tabla de BBDD {table}
```
Inserta en una tabla dada las files contenidas en el fichero CSV proporcionado. Las columnas no-nulables para las que no
se proporcionen datos se rellenarán con datos aleatorios.

#### Parámetros:
| Nombre   | Wakamiti type | Descripción         |
|----------|-------------|---------------------|
| `csv`    | `file`      | Fichero CSV         |
| `table`  | `word`      | Nombre de la tabla  |

#### Ejemplos:
```gherkin
  Cuando se inserta el contenido del fichero CSV 'data/users.csv' en la tabla de BBDD USER
```
```gherkin
  Cuando se ha insertado el contenido del fichero CSV 'data/users.csv' en la tabla de BBDD USER
```

---
### Comprobar existencia de datos
```
* con {column} = {value} existe(n) en la tabla de BBDD {table}
```
Comprueba que existe al menos una fila en la tabla indicada para la cual se cumple una comparación dada.

#### Parámetros:
| Nombre   | Wakamiti type | Descripción          |
|----------|-------------|----------------------|
| `column` | `word`      | Nombre de la columna |
| `value`  | `text`      | Valor de la columna  |
| `table`  | `word`      | Nombre de la tabla   |

#### Ejemplos:
```gherkin
  Entonces varios usuarios con STATE = '1' existen en la tabla de BBDD USER
```

---
### Comprobar inexistencia de datos
```
* con {column} = {value} no existe(n) en la tabla de BBDD {table}
```
Comprueba que no existe ninguna fila en la tabla indicada para la cual se cumpla una comparación.

#### Parámetros:
| Nombre   | Wakamiti type | Descripción          |
|----------|-------------|----------------------|
| `column` | `word`      | Nombre de la columna |
| `value`  | `text`      | Valor de la columna  |
| `table`  | `word`      | Nombre de la tabla   |

#### Ejemplos:
```gherkin
  Entonces usuarios con STATE = '1' no existen en la tabla de BBDD USER
```

---
### Comprobar existencia de datos (id)
```
* identificad(o|a|os|as) por {id} existe(n) en la tabla de BBDD {table}
```
Comprueba que existe una fila en la tabla dada cuya clave primaria coincide con la indicada. La tabla debe tener una
clave primaria formada por una sola columna.

#### Parámetros:
| Nombre  | Wakamiti type | Descripción                |
|---------|-------------|----------------------------|
| `id`    | `text`      | Valor de la clave primaria |
| `table` | `word`      | Nombre de la tabla         |

#### Ejemplos:
```gherkin
  Entonces el usuario identificado por 'user1' existe en la tabla de BBDD USER
```
```gherkin
  Entonces los usuarios identificados por 'algo' existen en la tabla de BBDD USER
```

---
### Comprobar inexistencia de datos (id)
```
* identificad(o|a|os|as) por {id} no existe(n) en la tabla de BBDD {table}
```
Comprueba que no existe una fila en la tabla dada cuya clave primaria coincide con la indicada. La tabla debe tener una
clave primaria formada por una sola columna.

#### Parámetros:
| Nombre  | Wakamiti type | Descripción                |
|---------|-------------|----------------------------|
| `id`    | `text`      | Valor de la clave primaria |
| `table` | `word`      | Nombre de la tabla         |

#### Ejemplos:
```gherkin
  Entonces el usuario identificado por 'user1' no existe en la tabla de BBDD USER
```
```gherkin
  Entonces los usuarios identificados por 'algo' no existen en la tabla de BBDD USER
```

---
### Comprobar existencia de datos (tabla)
```
(el|los) siguiente(s) registro(s) existe(n) en la tabla de BBDD {table}:
```
Comprueba que todas las filas siguientes existen en una tabla dada.

#### Parámetros:
| Nombre  | Wakamiti type | Descripción         |
|---------|-------------|---------------------|
| `table` | `word`      | Nombre de la tabla  |
|         | `table`     | Tabla con los datos |

#### Ejemplos:
```gherkin
  Entonces los siguientes registros existen en la tabla de BBDD USER:
    | USER  | STATE | BLOCKING_DATE |   
    | user1 | 2     | <null>        |
    | user2 | 3     | 2020-02-13    |
```

---
### Comprobar inexistencia de datos (tabla)
```
(el|los) siguiente(s) registro(s) no existe(n) en la tabla de BBDD {table}:
```
Comprueba que ninguna de las filas siguientes existen en una tabla dada.

#### Parámetros:
| Nombre  | Wakamiti type | Descripción         |
|---------|-------------|---------------------|
| `table` | `word`      | Nombre de la tabla  |
|         | `table`     | Tabla con los datos |

#### Ejemplos:
```gherkin
  Entonces los siguientes registros no existen en la tabla de BBDD USER:
    | USER  | STATE | BLOCKING_DATE |   
    | user1 | 2     | <null>        |
    | user2 | 3     | 2020-02-13    |
```

---
### Comprobar existencia de datos (XLS)
```
el contenido del fichero XLS {file} existe en la base de datos
```
Comprueba que todas las filas del fichero XLS proporcionado existen en la base de datos, correspondiendo cada hoja a una
tabla.

#### Parámetros:
| Nombre  | Wakamiti type | Descripción |
|---------|-------------|-------------|
| `file`  | `file`      | Fichero XLS |

#### Ejemplos:
```gherkin
  Entonces el contenido del fichero XLS 'data/example.xls' existe en la base de datos
```

---
### Comprobar inexistencia de datos (XLS)
```
el contenido del fichero XLS {file} no existe en la base de datos
```
Comprueba que ninguna de las filas del fichero XLS proporcionado existen en la base de datos, correspondiendo cada hoja
a una tabla.

#### Parámetros:
| Nombre  | Wakamiti type | Descripción |
|---------|-------------|-------------|
| `file`  | `file`      | Fichero XLS |

#### Ejemplos:
```gherkin
  Entonces el contenido del fichero XLS 'data/example.xls' no existe en la base de datos
```

---
### Comprobar existencia de datos (CSV)
```
el contenido del fichero CSV {csv} existe en la tabla de BBDD {table}
```
Comprueba que todas las filas incluidas en el fichero CSV proporcionado existen en una tabla dada.

#### Parámetros:
| Nombre   | Wakamiti type | Descripción         |
|----------|-------------|---------------------|
| `csv`    | `file`      | Fichero CSV         |
| `table`  | `word`      | Nombre de la tabla  |

#### Ejemplos:
```gherkin
  Entonces el contenido del fichero CSV 'data/users.csv' existe en la tabla de BBDD USER
```

---
### Comprobar inexistencia de datos (CSV)
```
el contenido del fichero CSV {csv} no existe en la tabla de BBDD {table}
```
Comprueba que todas las filas incluidas en el fichero CSV proporcionado no existen en una tabla dada.

#### Parámetros:
| Nombre   | Wakamiti type | Descripción         |
|----------|-------------|---------------------|
| `csv`    | `file`      | Fichero CSV         |
| `table`  | `word`      | Nombre de la tabla  |

#### Ejemplos:
```gherkin
  Entonces el contenido del fichero CSV 'data/users.csv' no existe en la tabla de BBDD USER
```

---
### Comprobar existencia de datos (documento)
```
* que satisface(n) la siguiente cláusula SQL existe(n) en la tabla de BBDD {table}:
```
Comprueba que al menos una fila de la tabla dada satisface la claúsula SQL indicada.

#### Parámetros:
| Nombre  | Wakamiti type | Descripción        |
|---------|-------------|--------------------|
| `table` | `word`      | Nombre de la tabla |
|         | `document`  | Cláusula where     |

#### Ejemplos:
```gherkin
  Entonces al menos un usuario que satisface la siguiente cláusula SQL existe en la tabla de BBDD USER:
    """
    STATE IN (2,3) OR BLOCKING_DATE IS NULL
    """
```

---
### Comprobar inexistencia de datos (documento)
```
* que satisface(n) la siguiente cláusula SQL no existe(n) en la tabla de BBDD {table}:
```
Comprueba que ninguna fila de la tabla dada satisface la claúsula SQL indicada.

#### Parámetros:
| Nombre  | Wakamiti type | Descripción        |
|---------|-------------|--------------------|
| `table` | `word`      | Nombre de la tabla |
|         | `document`  | Cláusula where     |

#### Ejemplos:
```gherkin
  Entonces usuarios que satisfacen la siguiente cláusula SQL no existen en la tabla de BBDD USER:
    """
    STATE IN (2,3) OR BLOCKING_DATE IS NULL
    """
```

---
### Comprobar número de datos
```
el número de * con {column} = {value} en la tabla de BBDD {table} {matcher}
```
Comprueba que el número de filas que satisfacen la condición indicada en la tabla indicada cumple la comparación
numérica indicada.

#### Parámetros:
| Nombre    | Wakamiti type      | Descripción              |
|-----------|------------------|--------------------------|
| `column`  | `word`           | Nombre de la columna     |
| `value`   | `text`           | Valor de la columna      |
| `table`   | `word`           | Nombre de la tabla       |
| `matcher` | `long-assertion` | [Comparador][2] numérico |

#### Ejemplos:
```gherkin
  Entonces el número de usuarios con STATE = '1' en la tabla de BBDD USER es mayor que 5
```

---
### Comprobar número de datos (tabla)
```
el número de * que satisfacen (lo|el|la|los|las) siguiente(s) * en la tabla de BBDD {table} {matcher}:
```
Comprueba que el número de filas que satisfacen los valores indicados en la tabla indicada cumple la comparación
numérica indicada.

#### Parámetros:
| Nombre    | Wakamiti type      | Descripción              |
|-----------|------------------|--------------------------|
| `table`   | `word`           | Nombre de la tabla       |
| `matcher` | `long-assertion` | [Comparador][2] numérico |
|           | `table`          | Tabla con los datos      |

#### Ejemplos:
```gherkin
  Entonces el número de usuarios que satisfacen lo siguiente en la tabla de BBDD USER es 0
    | USER  | STATE | BLOCKING_DATE |   
    | user1 | 2     | <null>        |
```

---
### Comprobar número de datos (documento)
```
el número de * que satisfacen la siguiente cláusula SQL en la tabla de BBDD {table} {matcher}:
```
Comprueba que el número de filas que satisfacen la condición indicada en la tabla indicada cumple la comparación
numérica indicada.

#### Parámetros:
| Nombre    | Wakamiti type      | Descripción              |
|-----------|------------------|--------------------------|
| `table`   | `word`           | Nombre de la tabla       |
| `matcher` | `long-assertion` | [Comparador][2] numérico |
|           | `document`       | Cláusula where           |

#### Ejemplos:
```gherkin
  Entonces el número de usuarios que satisfacen la siguiente cláusula SQL en la tabla de BBDD USER es menor que 3:
    """
    STATE IN (2,3) OR BLOCKING_DATE IS NULL
    """
```

---
### Comprobar tabla vacía
```
la tabla de BBDD {word} está vacía
```
Comprueba que no existe ninguna fila en la tabla indicada.

#### Parámetros:
| Nombre | Wakamiti type | Descripción        |
|--------|-------------|--------------------|
| `word` | `word`      | Nombre de la tabla |

#### Ejemplos:
```gherkin
  Entonces la tabla de BBDD USER está vacía
```

---
### Comprobar tabla no vacía
```
la tabla de BBDD {word} no está vacía
```
Comprueba que existe al menos una fila en la tabla indicada.

#### Parámetros:
| Nombre | Wakamiti type | Descripción        |
|--------|-------------|--------------------|
| `word` | `word`      | Nombre de la tabla |

#### Ejemplos:
```gherkin
  Entonces la tabla de BBDD USER no está vacía
```





[1]:  https://commons.apache.org/proper/commons-csv/
[2]: plugins/wakamiti/architecture#comparador