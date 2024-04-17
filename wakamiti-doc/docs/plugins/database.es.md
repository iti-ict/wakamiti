---
title: Pasos BD
date: 2022-09-20
slug: /plugins/database
---


Este plugin proporciona una serie de pasos para interactuar con una base de datos vía JDBC, facilitando la carga y
validación de datos.

> **RECUERDA**
>
> Debido a los muchos motores existentes de bases de datos, este plugin no incluye ningún driver específico. Esto
> significa que, para funcionar correctamente, es necesario inluir el módulo con el controlador JDBC adecuado.


---
## Tabla de contenido

---


## Instalación


Incluye el módulo y el controlador(es) JDBC en la sección correspondiente.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:db-wakamiti-plugin:3.1.0
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>db-wakamiti-plugin</artifactId>
  <version>3.1.0</version>
</dependency>
```



## Configuración


### `database.connection.url`
- Tipo: `string` *requerido*

Establece la URL de conexión a la base de datos por defecto con esquema JDBC. El driver usado para acceder a la base de
datos se determinará a partir del formato de URL indicado.

Ejemplo:
```yaml
database:
  connection:
    url: jdbc:h2:tcp://localhost:9092/~/test
```


### `database.connection.username`
- Tipo: `string` *requerido*

Establece el nombre de usuario requerido para conectar a la base de datos por defecto.

Ejemplo:
```yaml
database:
  connection:
    username: test
```


### `database.connection.password`
- Tipo: `string` *requerido*

Establece la contraseña requerida para conectar a la base de datos por defecto.

Ejemplo:
```yaml
database:
  connection:
    password: test
```


### `database.metadata.schema`
- Tipo: `string`

Establece manualmente el esquema de base de datos que se usará para recuperar metadatos como claves privadas y/o
nulabilidad. Si no se indica, se usará el esquema por defecto que retorne la conexión.

Ejemplo:
```yaml
database:
  metadata:
    schema: TESTDB
```


### `database.metadata.catalog`
- Tipo: `string`

Establece manualmente el catálogo de base de datos (si el motor soporta esta característica) que se usará para recuperar
metadatos como claves privadas y/o nulabilidad. Si no se indica, se usará el esquema por defecto que retorne la conexión.

Ejemplo:
```yaml
database:
  metadata:
    catalog: TESTCAT
```


### `database.{alias}...`

Establece los prámetros de conexión JDBC y/o los metadatos de una base de datos identificada por un alias. Se pueden
establecer tantas conexiones como se desée. La primera base de datos configurada será tomada como la configuración por
defecto.

Ejemplo:
```yaml
database:
  db1:
    connection:
      url: jdbc:h2:tcp://localhost:9092/~/test
      username: test1
      password: test1
    metadata:
      schema: TESTDB1
      catalog: TESTCAT1
  db2:
    connection:
      url: jdbc:mysql://other.host:3306/test
      username: test2
      password: test2
    metadata:
      schema: TESTDB2
      catalog: TESTCAT2
```


### `database.csv.format`
- Tipo: `string`
- Por defecto `DEFAULT`

Establece la variante de formato usado a la hora de leer ficheros CSV. Los valores aceptados son directamente los usados
por el proyecto [Commons CSV][1] (consultar el enlace para una explicación exhaustiva de cada variante). Los posibles
valores son:
- `DEFAULT`
- `INFORMIX_UNLOAD`
- `INFORMIX_UNLOAD_CSV`
- `MYSQL`
- `ORACLE`
- `POSTGRESQL_CSV`
- `POSTGRESQL_TEXT`
- `RFC4180`

Ejemplo:
```yaml
database:
  csv:
    format: ORACLE
```


### `database.xls.ignoreSheetPattern`
- Tipo: `regex`
- Por defecto `#.*`

Establece la expresión regular usada para determinar qué hojas se deberían ignorar cuando se cargan datos de un fichero
XLSX.

Ejemplo:
```yaml
database:
  xls:
    ignoreSheetPattern: //.*
```


### `database.nullSymbol`
- Tipo: `string`
- Por defecto `<null>`

Establece el literal usado para marcar una celda específica con el valor correspondiente al `NULL` de SQL. Se usa en
cualquier origen de datos (ficheros CSV, XLSX, y tablas embebidas en la definición del test).

Ejemplo:
```yaml
database:
  nullSymbol: (null)
```


### `database.enableCleanupUponCompletion`
- Tipo: `boolean`
- Por defecto `false`

El comportamiento por defecto del plugin no realiza ninguna operación de limpieza de la base de datos al acabar la
ejecución de los tests. Esto es así para poder comprobar resultados manualmente y depurar errores.
Los posibles valores son:
- `false`: no se realizará ninguna acción de limpieza.
- `true`: se forzará a limpiar la base de datos borrando los datos de prueba introducidos durante la ejecución.

Ejemplo:
```yaml
database:
  enableCleanupUponCompletion: "true"
```


## Pasos


### Definir conexión
```text copy=true
la URL de conexión a BBDD {url} usando el usuario {username} y la contraseña {password} (como {alias})
```

Configura los parámetros de conexión a la base de datos con el alias especificado. Si no se incluye `como {alias}`, se
establecerá como conexión por defecto.

Este paso es equivalente a configurar las propiedades
[`database.connection.url`](#databaseconnectionurl), [`database.connection.username`](#databaseconnectionusername),
[`database.connection.password`](#databaseconnectionpassword), [`database.{alias}...`](#databasealias).

#### Parámetros:
| Nombre     | Wakamiti type        | Descripción           |
|------------|----------------------|-----------------------|
| `url`      | `text` *obligatorio* | La URL de conexión    |
| `username` | `text` *obligatorio* | Nombre de usuario     |
| `password` | `text` *obligatorio* | Contraseña de usuario |
| `alias`    | `text`               | Nombre de la conexión |

#### Ejemplos:
```gherkin
Dada la URL de conexión a BBDD 'jdbc:h2:tcp://localhost:9092/~/TESTDB' usando el usuario 'test' y la contraseña 'test'
```
```gherkin
Dada la URL de conexión a BBDD 'jdbc:mysql://other.host:3306/test' usando el usuario 'test' y la contraseña 'test' como 'db1'
```


### Cambiar conexión
```text copy=true
(que) se usa la conexión ({alias:text}|por defecto)
```

Cambiar la conexión de base de datos activa a la especificada, o la predeterminada.

#### Parámetros:
| Nombre  | Wakamiti type | Descripción              |
|---------|---------------|--------------------------|
| `alias` | `text`        | El nombre de la conexión |

#### Ejemplos:
```gherkin
Cuando se usa la conexión por defecto
```
```gherkin
Cuando se usa la conexión 'db1'
```


### Ejecutar script
```text copy=true
(que) se (ha) ejecuta(do) el siguiente (script|procedimiento) SQL:
   {script}
```
- [Modo post-ejecución][3]

Ejecuta las sentencias SQL o el procedimiento indicado y recupera los datos insertados o seleccionados como un objeto
JSON.

#### Parámetros:
| Nombre   | Wakamiti type            | Descripción             |
|----------|--------------------------|-------------------------|
| `script` | `document` *obligatorio* | El contenido del script |

#### Ejemplos:
```gherkin
When se ejecuta el siguiente script SQL:
"""sql
  INSERT INTO users (id, first_name) VALUES (1, 'Rosa');
  INSERT INTO users (id, first_name) VALUES (2, 'Pepe');
  """
```

Podría devolver el siguiente resultado:
```json
[
  {
    "id": 1, 
    "first_name": "Rosa"
  },
  {
    "id": 2,
    "first_name": "Pepe"
  }
]
```


### Ejecutar script (fichero)
```text copy=true
(que) se (ha) ejecuta(do) el (script|procedimiento) SQL del fichero {script}
```
- [Modo post-ejecución][3]

Ejecuta las sentencias SQL o el procedimiento indicado y recupera los datos insertados o seleccionados como un objeto
JSON.

#### Parámetros:
| Nombre   | Wakamiti type        | Descripción     |
|----------|----------------------|-----------------|
| `script` | `file` *obligatorio* | Ruta del script |

#### Ejemplos:
```gherkin
Cuando se ejecuta el script SQL del fichero 'data/script.sql'
```


### Seleccionar datos
```text copy=true
(se recupera) (el|los) valor(es) de la siguiente consulta SQL:
   {script}
```

Recupera los datos de la sentencia SQL especificada como un objeto JSON.

#### Parámetros:
| Nombre   | Wakamiti type             | Descripción          |
|----------|---------------------------|----------------------|
| `script` | `document`  *obligatorio* | Contenido del script |

#### Ejemplos:
```gherkin
Dado los valores de la siguiente consulta SQL:
  """
  SELECT id, first_name FROM users
  """
```

Podría devolver el siguiente resultado:
```json
[
  {
    "id": 1, 
    "first_name": "Rosa"
  },
  {
    "id": 2,
    "first_name": "Pepe"
  }
]
```


### Seleccionar datos (fichero)
```text copy=true
(se recupera) (el|los) valor(es) de la consulta SQL del fichero {sql}
```

Recupera los datos de la sentencia SQL especificada como un objeto JSON.

#### Parámetros:
| Nombre | Wakamiti type | Descripción |
|--------|---------------|-------------|
| `sql`  | `file`        | Fichero SQL |

#### Ejemplos:
```gherkin
Dado los valores de la consulta SQL del fichero 'data/select-users.sql'
```


### Insertar datos
```text copy=true
se (ha) inserta(do) (lo|el|la|los|las) siguiente(s) * en la tabla de BBDD {word}:
    {data}
```
- [Modo post-ejecución][3]

Inserta las filas indicadas en la tabla y recupera los datos insertados como un objeto JSON.


#### Parámetros:
| Nombre | Wakamiti type         | Descripción         |
|--------|-----------------------|---------------------|
| `word` | `word` *obligatorio*  | Nombre de la tabla  |
| `data` | `table` *obligatorio* | Tabla con los datos |

#### Ejemplos:
```gherkin
Cuando se inserta lo siguiente en la tabla de BBDD USER:
    | USER  | STATE | BLOCKING_DATE |   
    | user1 | 2     | <null>        |
    | user2 | 3     | 2020-02-13    |
```

Podría devolver el siguiente resultado:
```json
[
  {
    "id": 1, 
    "first_name": "Rosa"
  },
  {
    "id": 2,
    "first_name": "Pepe"
  }
]
```


### Insertar datos (fichero)
```text copy=true
se (ha) inserta(do) el contenido del fichero XLS {file} en la base de datos
```
```text copy=true
se (ha) inserta(do) el contenido del fichero CSV {file} en la tabla (de BBDD) {table}
```
- [Modo post-ejecución][3]

Inserta las filas contenidas en el fichero XLS o CSV indicado y recupera los datos insertados como un objeto JSON. Si se
trata de un fichero XLS, cada hoja representará una tabla y deberá llamarse como tal. Si se trata de un fichero CSV, se
deberá indicar el nombre de la tabla en la que se insertarán los datos.

#### Parámetros:
| Nombre  | Wakamiti type        | Descripción        |
|---------|----------------------|--------------------|
| `file`  | `file` *obligatorio* | Ruta del fichero   |
| `table` | `word`               | Nombre de la tabla |

#### Ejemplos:
```gherkin
Cuando se inserta el contenido del fichero XLS 'data/users.xls' en la BBDD
``` 
```gherkin
Cuando se ha insertado el contenido del fichero CSV 'data/users.csv' en la tabla USER
``` 


### Eliminar datos
```text copy=true
se (ha) elimina(n|do) (lo|el|la|los|las) siguiente(s) * de la tabla (de BBDD) {table}:
    {data}
```
- [Modo post-ejecución][3]

Elimina las filas indicadas en la tabla.

#### Parámetros:
| Nombre  | Wakamiti type         | Descripción         |
|---------|-----------------------|---------------------|
| `table` | `word` *obligatorio*  | Nombre de la tabla  |
| `data`  | `table` *obligatorio* | Tabla con los datos |

#### Ejemplos:
```gherkin
Cuando se eliminan los siguientes usuarios de la tabla USER:
    | USER  | STATE | BLOCKING_DATE |   
    | user1 | 2     | <null>        |
    | user2 | 3     | 2020-02-13    |
```


### Eliminar datos (columna)
```text copy=true
se (ha) elimina(n|do) * con {column} = {value} de la tabla (de BBDD) {table}
```
- [Modo post-ejecución][3]

Elimina de la tabla las filas que satisfagan la comparación indicada.

#### Parámetros:
| Nombre   | Wakamiti type        | Descripción          |
|----------|----------------------|----------------------|
| `column` | `word` *obligatorio* | Nombre de la columna |
| `value`  | `text` *obligatorio* | Valor de la columna  |
| `table`  | `word` *obligatorio* | Nombre de la tabla   |

#### Ejemplos:
```gherkin
  Cuando se eliminan los usuarios con STATE = '2' de la tabla USER 
```


### Eliminar datos (where)
```text copy=true
(que) se (ha) elimina(n|do) * que satisfacen la siguiente cláusula SQL en la tabla (de BBDD) {table}:
    {where}
```
- [Modo post-ejecución][3]

Elimina los registros de la tabla que satisfacen la cláusula WHERE indicada.

#### Parámetros:
| Nombre  | Wakamiti type            | Descripción        |
|---------|--------------------------|--------------------|
| `table` | `word` *obligatorio*     | Nombre de la tabla |
| `where` | `document` *obligatorio* | Cláusula where     |

#### Ejemplos:
```gherkin
Cuando se eliminan los usuarios que satisfacen la siguiente cláusula SQL en la tabla client:
    """
    birth_date < date '2000-01-01'
    """
```


### Eliminar datos (fichero)
```text copy=true
se (ha) elimina(do) el contenido del fichero XLS {file} de la base de datos
```
```text copy=true
se (ha) elimina(do) el contenido del fichero CSV {file} de la tabla (de BBDD) {table}
```
- [Modo post-ejecución][3]

Elimina las filas contenidas en el fichero XLS o CSV indicado. Si se trata de un fichero XLS, cada hoja representará una
tabla y deberá llamarse como tal. Si se trata de un fichero CSV, se deberá indicar el nombre de la tabla en la que se
eliminarán los datos.

#### Parámetros:
| Nombre  | Wakamiti type        | Descripción        |
|---------|----------------------|--------------------|
| `file`  | `file` *obligatorio* | Ruta del fichero   |
| `table` | `word`               | Nombre de la tabla |

#### Ejemplos:
```gherkin
Cuando se elimina el contenido del fichero XLS 'data/users.xls' de la BBDD
```
```gherkin
Cuando se ha eliminado el contenido del fichero CSV 'data/users.csv' de la tabla USER
```


### Vaciar tabla
```text copy=true
se limpia la tabla (de BBDD) {word}
```
- [Modo post-ejecución][3]

Elimina todos los registros de la tabla.

#### Parámetros:
| Nombre | Wakamiti type        | Descripción        |
|--------|----------------------|--------------------|
| `word` | `word` *ôbligatorio* | Nombre de la tabla |

#### Ejemplos:
```gherkin
Cuando se limpia la tabla USERS
```


### Comprobar existencia de datos
```text copy=true
(el|los) siguiente(s) registro(s) (no) existe(n) en la tabla (de BBDD) {table}:
    {data}
```
- [Modo post-ejecucion][3]
- [Modo async][4]

Comprueba que todas las filas siguientes existen, o no, en la tabla indicada.

#### Parámetros:
| Nombre  | Wakamiti type         | Descripción         |
|---------|-----------------------|---------------------|
| `table` | `word` *obligatorio*  | Nombre de la tabla  |
| `data`  | `table` *obligatorio* | Tabla con los datos |

#### Ejemplos:
```gherkin
Entonces el siguiente usuario existe en la tabla USER:
    | USER  | STATE | BLOCKING_DATE |   
    | user2 | 3     | 2020-02-13    |
```
```gherkin
Entonces los siguientes usuarios no existen en la tabla USER:
    | USER  | STATE | BLOCKING_DATE |   
    | user1 | 2     | <null>        |
    | user2 | 3     | 2020-02-13    |
```


### Comprobar existencia de datos (id)
```text copy=true
* identificad(o|a|os|as) por {id} (no) existe(n) en la tabla (de BBDD) {table}
```
- [Modo post-ejecucion][3]
- [Modo async][4]

Comprueba que una fila de la tabla indicada tiene, o no, una clave primaria que coincide con el valor especificado. La
tabla debe tener una clave primaria de una sola columna accesible desde los metadatos de la base de datos.

#### Parámetros:
| Nombre  | Wakamiti type        | Descripción                |
|---------|----------------------|----------------------------|
| `id`    | `text` *obligatorio* | Valor de la clave primaria |
| `table` | `word` *obligatorio* | Nombre de la tabla         |

#### Ejemplos:
```gherkin
Entonces el usuario identificado por 'user1' existe en la tabla USER
```
```gherkin
Entonces el usuario identificados por 'algo' no existe en la tabla USER
```


### Comprobar existencia de datos (columna)
```text copy=true
* con {column} = {value} (no) existe(n) en la tabla (de BBDD) {table}
```
- [Modo post-ejecucion][3]
- [Modo async][4]

Comprueba que las filas con el valor especificado en la columna dada existen, o no, en la tabla indicada.

#### Parámetros:
| Nombre   | Wakamiti type        | Descripción          |
|----------|----------------------|----------------------|
| `column` | `word` *obligatorio* | Nombre de la columna |
| `value`  | `text` *obligatorio* | Valor de la columna  |
| `table`  | `word` *obligatorio* | Nombre de la tabla   |

#### Ejemplos:
```gherkin
Entonces varios usuarios con STATE = '1' existen en la tabla USER
```
```gherkin
Entonces los usuarios con STATE = '1' no existen en la tabla USER
```


### Comprobar existencia de datos (where)
```text copy=true
* que satisface(n) la siguiente cláusula SQL (no) existe(n) en la tabla de BBDD {table}:
    {where}
```
- [Modo post-ejecucion][3]
- [Modo async][4]

Comprueba que las filas con los valores especificados en la cláusula dada existen, o no, en la tabla.

#### Parámetros:
| Nombre  | Wakamiti type            | Descripción        |
|---------|--------------------------|--------------------|
| `table` | `word` *obligatorio*     | Nombre de la tabla |
| `where` | `document` *obligatorio* | Cláusula where     |

#### Ejemplos:
```gherkin
Entonces al menos un usuario que satisface la siguiente cláusula SQL existe en la tabla USER:
    """
    STATE IN (2,3) OR BLOCKING_DATE IS NULL
    """
```
```gherkin
Entonces el usuario que satisface la siguiente cláusula SQL no existe en la tabla USER:
    """
    STATE IN (2,3) OR BLOCKING_DATE IS NULL
    """
```


### Comprobar existencia de datos (fichero)
```text copy=true
el contenido del fichero XLS {file} existe en la base de datos
```
```text copy=true
el contenido del fichero CSV {file} existe en la table (de BBDD) {table}
```
- [Modo post-ejecucion][3]
- [Modo async][4]

Comprueba que las filas del archivo XLS o CSV proporcionado existen, o no, en la base de datos. Si se trata de un
fichero XLS, cada hoja representará una tabla y deberá nombrarse como tal. Si se trata de un fichero CSV, se indicará el
nombre de la tabla donde se comprobarán los datos.

#### Parámetros:
| Nombre  | Wakamiti type        | Descripción        |
|---------|----------------------|--------------------|
| `file`  | `file` *obligatorio* | Ruta del fichero   |
| `table` | `word`               | Nombre de la tabla |

#### Ejemplos:
```gherkin
Entonces el contenido del fichero XLS 'data/example.xls' existe en la base de datos
```
```gherkin
Entonces el contenido del fichero CSV 'data/users.csv' no existe en la tabla USER
```


### Comprobar número de datos (tabla)
```text copy=true
el número de * que satisfacen (lo|la) siguiente (información) en la tabla de BBDD {table} {matcher}:
    {data}
```
- [Modo post-ejecucion][3]
- [Modo async][4]

Comprueba que el número de filas que satisfacen los valores indicados en la tabla cumple la comparación numérica
indicada.

#### Parámetros:
| Nombre    | Wakamiti type                  | Descripción              |
|-----------|--------------------------------|--------------------------|
| `table`   | `word` *obligatorio*           | Nombre de la tabla       |
| `matcher` | `long-assertion` *obligatorio* | [Comparador][2] numérico |
| `data`    | `table` *obligatorio*          | Tabla con los datos      |

#### Ejemplos:
```gherkin
Entonces el número de usuarios que satisfacen lo siguiente en la tabla de BBDD USER es 0:
    | USER  | STATE | BLOCKING_DATE |   
    | user1 | 2     | <null>        |
```
```gherkin
Entonces el número de registros que satisfacen la siguiente información en la tabla USER es mayor que 0:
    | USER  | STATE | BLOCKING_DATE |   
    | user1 | 2     | <null>        |
```


### Comprobar número de datos (columna)
```text copy=true
el número de * con {column} = {value} en la tabla (de BBDD) {table} {matcher}
```
- [Modo post-ejecucion][3]
- [Modo async][4]

Comprueba que el número de filas que satisfacen la condición cumple la comparación numérica indicada.

#### Parámetros:
| Nombre    | Wakamiti type                  | Descripción              |
|-----------|--------------------------------|--------------------------|
| `column`  | `word` *obligatorio*           | Nombre de la columna     |
| `value`   | `text` *obligatorio*           | Valor de la columna      |
| `table`   | `word` *obligatorio*           | Nombre de la tabla       |
| `matcher` | `long-assertion` *obligatorio* | [Comparador][2] numérico |

#### Ejemplos:
```gherkin
Entonces el número de usuarios con STATE = '1' en la tabla USER es mayor que 5
```


### Comprobar número de datos (where)
```text copy=true
el número de * que satisfacen la siguiente cláusula SQL en la tabla (de BBDD) {table} {matcher}:
    {where}
```
- [Modo post-ejecucion][3]
- [Modo async][4]

Comprueba que el número de filas que satisfacen la condición cumple la comparación numérica indicada.

#### Parámetros:
| Nombre    | Wakamiti type     | Descripción              |
|-----------|-------------------|--------------------------|
| `table`   | `word`            | Nombre de la tabla       |
| `matcher` | `long-assertion`  | [Comparador][2] numérico |
| `where`   | `document`        | Cláusula where           |

#### Ejemplos:
```gherkin
Entonces el número de usuarios que satisfacen la siguiente cláusula SQL en la tabla USER es menor que 3:
    """
    STATE IN (2,3) OR BLOCKING_DATE IS NULL
    """
```


### Comprobar contenido de tabla
```text copy=true
la tabla (de BBDD) {word} (no) está vacía
```
- [Modo post-ejecucion][3]
- [Modo async][4]

Comprueba que la tabla indicada está vacía, o no.

#### Parámetros:
| Nombre | Wakamiti type        | Descripción        |
|--------|----------------------|--------------------|
| `word` | `word` *obligatorio* | Nombre de la tabla |

#### Ejemplos:
```gherkin
Entonces la tabla USER está vacía
```
```gherkin
Entonces la tabla USER no está vacía
```


## Modos especiales


Algunos pasos pueden ejecutarse con un comportamiento diferente si se definen de las siguientes maneras:

### Modo post-ejecución
```text copy=true
Al finalizar, * (usando la conexión {alias})
```

El paso se ejecutará una vez finalizado el escenario, independientemente del resultado de la ejecución. Si no se incluye
el alias, se utilizará la conexión por defecto.

#### Parámetros:
| Nombre  | Wakamiti type | Descripción           |
|---------|---------------|-----------------------|
| `alias` | `text`        | Nombre de la conexión |

#### Ejemplos:
```gherkin
Al finalizar, se ejecuta el script SQL del fichero 'data/sript.sql'
```
```gherkin
Al finalizar, se ejecuta el siguiente script SQL usando la conexión 'db1':
  """
  UPDATE AAAA SET STATE = 2 WHERE ID = 1;
  DELETE FROM BBBB WHERE ID = 2;
  """
```


### Modo async
```text copy=true
* en {time} segundos
```

El paso espera un máximo de los segundos indicados hasta que se cumple la condición indicada en el paso para continuar.

#### Parámetros:
| Nombre | Wakamiti type       | Descripción   |
|--------|---------------------|---------------|
| `time` | `int` *obligatorio* | Tiempo máximo |

#### Ejemplos:
```gherkin
Entonces un usuario identificado por '1' existe en la tabla USERS en 10 segundos
```


[1]: https://commons.apache.org/proper/commons-csv/
[2]: wakamiti/architecture#comparador
[3]: #modo-post-ejecución
[4]: #modo-async
