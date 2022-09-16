
# Database Steps

Este plugin proporciona un conjunto de pasos para interactuar con una base de datos mediante JDBC,
facilitando las tareas de carga y comprobación de datos.

Los conjuntos de datos se pueden definir de distintas formas:

- Ficheros CSV, para una sola tabla
- Ficheros XLSX, para varias tablas (una por hoja)
- Tablas escritas directamente en el documento, usando el formato:

```gherkin
 | columnaA | columnaB | columnaC |
 | fila1A   | fila1B   | fila1C   |
 | fila2A   | fila2B   | fila2C   |
```

El código fuente del plugin ofrece una forma fácil de implementar nuevos orígenes de datos 
extendiendo la clase `DataSet`.


> **IMPORTANTE**  
> Dadas los muchos motores de bases de datos existentes, este plugin **no** incluye ningún controlador
específico. Esto significa que, para funcionar correctamente, se debe incluir el módulo con con controlador 
JDBC adecuado que requiera el motor de base de datos a comprobar.



## Uso

### Propiedades de configuración


#### `database.connection.url`
La URL de conexión a la base de datos con esquema JDBC. El controlador usado para acceder a la base de datos
se determinará a partir del formato de URL indicado.

#### `database.connection.username`
El nombre de usuario requerido para conectar a la base de datos.

#### `database.connection.password`
La contraseña requerida para conectar a la base de datos.

#### `database.metadata.schema`
El esquema de base de datos que se usará para recuperar metadatos como claves privadas y/o nulabilidad.
Si no se indica, se usará el esquema por defecto que retorne la conexión.

#### `database.metadata.catalog`
El catálogo de base de datos (si el motor soporta esta característica) que se usará para recuperar metadatos 
como claves privadas y/o nulabilidad.
Si no se indica, se usará el esquema por defecto que retorne la conexión.

#### `database.metadata.caseSensititvy` = [`INSENSITIVE`] | `LOWER_CASED` | `UPPER_CASED`
Establece si se debería hacer alguna transformación de mayúsculas/minúsculas cuando se intente acceder a 
los metadatos. Algunos motores son estrictos con respecto a esto, y puede causar errores inesperados
si no se configura correctamente.

#### `database.csv.format` = [`DEFAULT`], `INFORMIX_UNLOAD`, `INFORMIX_UNLOAD_CSV`, `MYSQL`, `ORACLE`, `POSTGRESQL_CSV`, `POSTGRESQL_TEXT`, `RFC4180`
La variante de formato usado a la hora de leer ficheros CSV. Los valores aceptados son directamente los usados por el projecto 
[Commons CSV][1] (consultar el enlace para una explicación exhaustiva de cada variante).
project, check it for detailed explanation of each format.

#### `database.xls.ignoreSheetPattern` = `#.*` 
Expresión regular usada para determinar que hojas se deberían ignorar cuando se cargan datos de un fichero XLSX.


#### `database.nullSymbol` = `<null>`
Literal usado para marcar una celda específica con el valor correspondiente al `NULL` de SQL.
Se usa en cualquier orígen de datos (ficheros CSV, XLSX, y tablas embebidas en la definición del test).

#### `database.enableCleanupUponCompletion` = `true | [false]` 
El comportamiento por defecto del plugin no realiza ninguna operación de limpieza de la base de datos 
al acabar la ejecución de los tests. Esto es así para poder comprobar resultados manualmente y depurar errores.
Este parámetro permite modificar este comportamiento; indicando `true` se forzará a limpiar la base de datos
borrando los datos de prueba introducidos durante la ejecución.



### Pasos


#### Preparación

##### `la URL de conexión a BBDD {url:text} usando el usuario {username:text} y la contraseña {password:text}`
Establece la URL, nombre de usuario y contraseña para conectarse a la base de datos. 

Este paso es equivalente a configurar las propiedades `database.connection.url`,
`database.connection.username`, `database.connection.password`.
>```gherkin
> Dada la URL de conexión a BBDD 'jdbc:h2:tcp://localhost:9092/~/test' usando el usuario 'sa' y la contraseña ''
>```


#### Ejecución de scripts


##### `se ejecuta el siguiente script SQL:`
Ejecuta el script SQL escrito a continuación.
>```gherkin
> Cuando se ejecuta el siguiente script SQL:
> """
>  UPDATE USER SET STATE = 2 WHERE BLOCKING_DATE IS NULL;
>  DELETE FROM USER WHERE STATE = 3;
> """
>```

---
##### `se ejecuta el script SQL del fichero {file}`
Ejecuta el script SQL existente en el fichero indicado.
>```gherkin
> Cuando se ejecuta el script SQL del fichero 'data/insert-users.sql'
>


#### Borrado de filas


##### `se limpia la tabla de BBDD {word}`
Limpia la tabla indicada, intentando primero con la sentencia `TRUNCATE` , y con la sentencia `DELETE FROM` en 
caso de fallar la primera.
>```gherkin
> Cuando se limpia la tabla de BBDD {word}
>```


---
##### `se (ha) elimina(n|do) * con {column:word} = {value:text} de la tabla de BBDD {table:word}`
Elimina de una tabla dada las filas que satisfagan la comparación indicada.
>```gherkin
> Cuando se eliminan los usuarios con STATE = '2' de la tabla de BBDD USER 
>```


---
##### `se (ha) elimina(n|do) * con {column1:word} = {value1:text} y {column2:word} = {value2:text} de la tabla de BBDD {table:word}`
Elimina de una tabla dada las filas que satisfagan las dos comparaciones indicadas.
>```gherkin
> Cuando se eliminan los usuarios con STATE = '2' y BLOCKING_DATE = '<null>' de la tabla de BBDD USER
>```


---
##### `se (ha) elimina(do) (lo|el|la|los|las) siguiente(s) * de la tabla de BBDD {word}:`
Elimina de una tabla dada las filas indicadas.
>```gherkin
> Cuando se elimina los siguientes datos de la tabla de BBDD USER:
>  | USER  | STATE | BLOCKING_DATE |   
>  | user1 | 2     | <null>        |
>  | user2 | 3     | 2020-02-13    |
>```

---
##### `se (ha) elimina(do) el contenido del fichero XLS {file} de la BBDD`
Elimina las filas que concuerdan con los datos del fichero XLS indicado.
>```gherkin
> Cuando se elimina el contenido del fichero XLS 'data/users.xls' de la BBDD
>``` 

---
##### `se (ha) elimina(do) el contenido del fichero CSV {csv:file} de la tabla de BBDD {table:word}
Elimina las filas de una tabla dada que concuerdan con los datos del fichero CSV proporcionado.
>```gherkin
> Cuando se elimina el contenido del fichero CSV 'data/users.csv' de la tabla de BBDD USER
>```


#### Inserción de filas


##### `se (ha) inserta(do) (lo|el|la|los|las) siguiente(s) * en la tabla de BBDD {word}:`
Inserta las filas indicadas a continuación una tabla dada.
Las columnas no-nulables para las que no se
proporcionen datos se rellenarán con datos aleatorios.
>```gherkin
> Cuando se inserta lo siguiente en la tabla de BBDD USER:
>  | USER  | STATE | BLOCKING_DATE |   
>  | user1 | 2     | <null>        |
>  | user2 | 3     | 2020-02-13    |
>```


---
##### `se (ha) inserta(do) el contenido del fichero XLS {file} en la BBDD`
Inserta las filas contenidas en el fichero XLS indicado, una hoja por tabla. Las columnas no-nulables para las que no se
proporcionen datos se rellenarán con datos aleatorios.
>```gherkin
> Cuando se inserta el contenido del fichero XLS 'data/users.xls' en la BBDD
>``` 


---
##### `se (ha) inserta(do) el contenido del fichero CSV {csv:file} en la tabla de BBDD {table:word}
Inserta en una tabla dada las files contenidas en el fichero CSV proporcionado. 
Las columnas no-nulables para las que no se
proporcionen datos se rellenarán con datos aleatorios.
>```gherkin
> Cuando se inserta el contenido del fichero CSV 'data/users.csv' en la tabla de BBDD USER
>```


#### Validación de datos existentes


##### `* identificad(o|a|os|as) por {id:text} existe(n) en la tabla de BBDD {table:word}`
Comprueba que existe una fila en la tabla dada cuya clave primaria coincide con la indicada.
La tabla debe tener una clave primaria formada por una sola columna.
>```gherkin
> Entonces el usuario identificado por 'user1' existe en la tabla de BBDD USER
>```


---
##### `* identificad(o|a|os|as) por {id:text} no existe(n) en la tabla de BBDD {table:word}`
Comprueba que no existe una fila en la tabla dada cuya clave primaria coincide con la indicada.
La tabla debe tener una clave primaria formada por una sola columna.
>```gherkin
> Entonces el usuario identificado por 'user1' no existe en la tabla de BBDD USER
>```


---
##### `* con {column:word} = {value:text} existe(n) en la tabla de BBDD {table:word}`
Comprueba que existe al menos una fila en la tabla indicada para la cual se cumple una comparación dada.
>```gherkin
> Entonces varios usuarios con STATE = '1' existen en la tabla de BBDD USER
>```


---
##### `* con {column:word} = {value:text} no existe(n) en la tabla de BBDD {table:word}`
Comprueba que no existe ninguna fila en la tabla indicada para la cual se cumpla una comparación.
>```gherkin
> Entonces usuarios con STATE = '1' no existen en la tabla de BBDD USER
>```


---
##### `el número de * con {column:word} = {value:text} en la tabla de BBDD {table:word} {matcher:long-assertion}`
Comprueba que el número de filas en la tabla indicada para las cuales se cumple una comparación dada satisface 
una validación de enteros.
>```gherkin
> Entonces el número de usuarios con STATE = '1' en la tabla de BBDD USER es mayor que 5
>```


---
##### `* con {column1:word} = {value1:text} y {column2:word} = {value2:text} existe(n) en la tabla de BBDD {table:word}`
Comprueba que existe al menos una fila en la tabla indicada para la cual se cumplen dos comparaciones dadas.
>```gherkin
> Entonces varios usuarios con STATE = '1' y BLOCKING_DATE = '<null>' existen en la tabla de BBDD USER
>```


---
##### `* con {column1:word} = {value1:text} y {column2:word} = {value2:text} no existe(n) en la tabla de BBDD {table:word}`
Comprueba que no existe ninguna fila en la tabla indicada para la cual se cumplen dos comparaciones dadas.
>```gherkin
> Entonces usuarios con STATE = '1' y BLOCKING_DATE = '<null>' no existen en la tabla de BBDD USER
>```


---
##### `el número de * con {column1:word} = {value1:text} y {column2:word} = {value2:text} en la tabla de BBDD {table:word} {matcher:long-assertion}`
Comprueba que el número de filas en la tabla indicada para las cuales se cumplen dos comparaciones dadas satisface
una validación de enteros.
>```gherkin
> Entonces el número de usuarios con STATE = '1' y BLOCKING_DATE = '<null>' en la tabla de BBDD USER es mayor que 5
>```

---
##### `* que satisface(n) la siguiente cláusula SQL existe(n) en la tabla de BBDD {table:word}:`
Comprueba que al menos una fila de la tabla dada satisface la claúsula SQL indicada
>```gherkin
> Entonces al menos un usuario que satisface la siguiente cláusula SQL existe en la tabla de BBDD USER:
> """sql
> STATE IN (2,3) OR BLOCKING_DATE IS NULL
> """
>```


---
##### `* que satisface(n) la siguiente cláusula SQL no existe(n) en la tabla de BBDD {table:word}:`
Comprueba que ninguna fila de la tabla dada satisface la claúsula SQL indicada
>```gherkin
> Entonces usuarios que satisfacen la siguiente cláusula SQL no existen en la tabla de BBDD USER:
> """sql
> STATE IN (2,3) OR BLOCKING_DATE IS NULL
> """
>```


---
##### `el número de * que satisfacen la siguiente cláusula SQL en la tabla de BBDD {table:word} {matcher:long-assertion}:`
Comprueba que el número de filas de la tabla dada que satisfacen la claúsula SQL indicada supera una 
validación de enteros.
>```gherkin
> Entonces el número de usuarios que satisfacen la siguiente cláusula SQL en la tabla de BBDD USER es menor que 3:
> """sql
> STATE IN (2,3) OR BLOCKING_DATE IS NULL
> """
>```


---
##### `(el|los) siguiente(s) registro(s) existe(n) en la tabla de BBDD {table:word}:`
Comprueba que todas las filas siguientes existen en una tabla dada.
>```gherkin
> Entonces los siguientes registros existen en la tabla de BBDD USER:
>  | USER  | STATE | BLOCKING_DATE |   
>  | user1 | 2     | <null>        |
>  | user2 | 3     | 2020-02-13    |
>```


---
##### `(el|los) siguiente(s) registro(s) no existe(n) en la tabla de BBDD {table:word}:`
Comprueba que ninguna de las filas siguientes existen en una tabla dada.
>```gherkin
> Entonces los siguientes registros no existen en la tabla de BBDD USER:
>  | USER  | STATE | BLOCKING_DATE |   
>  | user1 | 2     | <null>        |
>  | user2 | 3     | 2020-02-13    |
>```


---
##### `el número de * que satisfacen (lo|el|la|los|las) siguiente(s) * en la tabla de BBDD {table:word} {matcher:long-assertion}:`
Comprueba que el número de filas de una tabla dada que cumplen los valores dados para distintas columnas
satisface una validación de enteros.
>```gherkin
> Entonces el número de usuarios que satisfacen lo siguiente en la tabla de BBDD USER es 0
>  | USER  | STATE | BLOCKING_DATE |   
>  | user1 | 2     | <null>        |
>```


---
##### `el contenido del fichero XLS {file} existe en la base de datos`
Comprueba que todas las filas del fichero XLS proporcionado existen en la base de datos,
correspondiendo cada hoja a una tabla.
>```gherkin
> Entonces el contenido del fichero XLS 'data/users.csv' existe en la tabla de BBDD USER
>```

---
##### `el contenido del fichero XLS {file} no existe en la base de datos`
Comprueba que ninguna de las filas del fichero XLS proporcionado existen en la base de datos, 
correspondiendo cada hoja a una tabla.
>```gherkin
> Entonces el contenido del fichero XLS 'data/users.csv' no existe en la tabla de BBDD USER
>```


---
##### `el contenido del fichero CSV {csv:file} existe en la tabla de BBDD {table:word}`
Comprueba que todas las filas del fichero CSV proporcionado existen en una tabla dada.
>```gherkin
> Entonces el contenido del fichero CSV 'data/users.csv' existe en la tabla de BBDD USER
>```


---
##### `el contenido del fichero CSV {csv:file} no existe en la tabla de BBDD {table:word}`
Comprueba que todas las filas incluidas en el fichero CSV proporcionado existen en una tabla dada
>```gherkin
> Entonces el contenido del fichero CSV 'data/users.csv' no existe en la tabla de BBDD USER
>```


---
##### `la tabla de BBDD {word} está vacía`
Comprueba que no existe ninguna fila en la tabla indicada
>```gherkin
> Entonces la tabla de BBDD USER está vacía
>```



---
##### `la tabla de BBDD {word} no está vacía`
Comprueba que existe al menos una fila en la tabla indicada
>```gherkin
> Entonces la tabla de BBDD USER no está vacía
>```






  
## References  

- [**1**] *Common CSV* -  https://commons.apache.org/proper/commons-csv/  
  
[1]:  https://commons.apache.org/proper/commons-csv/
