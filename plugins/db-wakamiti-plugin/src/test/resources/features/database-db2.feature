# language: es
Característica: Testing database steps


  Escenario: Test 1
    * Al finalizar, la tabla client no está vacía
    * Al finalizar, el usuario identificado por '1' existe en la tabla client
    * Al finalizar, el usuario identificado por '4' no existe en la tabla client
    * Al finalizar, el usuario con birth_date = '2000-10-30' existe en la tabla client
    * Al finalizar, el usuario con birth_date = '1982-12-30' no existe en la tabla client
    * Al finalizar, el usuario que satisface la siguiente cláusula SQL existe en la tabla client:
      """
      BIRTH_DATE < date '2000-01-01'
      """
    * Al finalizar, el siguiente registro existe en la tabla client:
      | id | first_name | second_name | active | birth_date |
      | 1  | John       | Smith       | 1      | 2000-10-30 |
    * Al finalizar, se limpia la tabla client
    * Al finalizar, se limpia la tabla city
    * Al finalizar, el usuario que satisface la siguiente cláusula SQL no existe en la tabla client:
      """
      BIRTH_DATE < date '2000-01-01'
      """
    * Al finalizar, el siguiente registro no existe en la tabla client:
      | id | first_name | second_name | active | birth_date |
      | 1  | John       | Smith       | 1      | 2000-10-30 |
    Cuando se insertan los siguientes usuarios en la tabla client:
      | id | first_name | second_name | active | birth_date |
      | 1  | John       | Smith       | 1      | 2000-10-30 |
      | 2  | Annie      | Hall        | 0      | 2011-09-12 |
      | 3  | Bruce      | <null>      | 1      | 1982-12-31 |
    Entonces la tabla client no está vacía
    Y el número de usuarios con active = '1' en la tabla client es 2
    Y el número de usuarios que satisfacen la siguiente cláusula SQL en la tabla client es 2:
      """
      BIRTH_DATE > date '2000-01-01'
      """
    Y la tabla city está vacía
    Y el siguiente registro existe en la tabla client:
      | first_name | second_name | birth_date |
      | John       | Smith       | 2000-10-30 |


  Escenario: Test 2
    * Al finalizar, se ejecuta el script SQL del fichero '${data.dir}/db/clean.sql'
    * Al finalizar, el número de usuarios que satisfacen la siguiente cláusula SQL en la tabla client es 0:
      """
      1=1
      """
    * Al finalizar, el número de usuarios que satisfacen lo siguiente en la tabla client es 0:
      | id | first_name | second_name | active | birth_date |
      | 1  | John       | Smith       | 1      | 2000-10-30 |
    Cuando se ejecuta el script SQL del fichero '${data.dir}/db/dml.sql'
    Y se ejecuta el procedimiento SQL del fichero '${data.dir}/db/procedure-db2.sql'
    Entonces el siguiente registro no existe en la tabla client:
      | id | first_name | second_name | active  | birth_date |
      | 1  | Rosa       | Melano      | 1       | <null>     |
    Pero los siguientes registros existen en la tabla client:
      | id | first_name | second_name | active | birth_date |
      | 1  | Rosa       | Melano      | 0      | 1980-12-25 |
      | 2  | Ester      | Colero      | 0      | 2000-01-02 |
    Y los siguientes registros existen en la tabla city:
      | id | name     | latitude  | longitude |
      | 1  | Valencia | 39.469906 | -0.376288 |
      | 2  | Madrid   | 40.416775 | -3.703790 |
    Y los siguientes registros existen en la tabla client_city:
      | CLIENTID | CITYID |
      | 1        | 1      |
      | 2        | 2      |
      | 2        | 1      |
    Y el usuario identificado por '1' existe en la tabla client


  Escenario: Test 3
    * Al finalizar, la tabla client no está vacía usando la conexión 'db'
    * Al finalizar, se limpia la tabla client usando la conexión 'db'
    * Al finalizar, la tabla client está vacía usando la conexión 'db'
    * Al finalizar, se inserta el contenido del fichero XLS '${data.dir}/data1.xlsx' en la base de datos usando la conexión 'db'
    * Al finalizar, el contenido del fichero XLS '${data.dir}/data1.xlsx' existe en la base de datos usando la conexión 'db'
    * Al finalizar, se elimina el contenido del fichero XLS '${data.dir}/data1.xlsx' de la base de datos usando la conexión 'db'
    * Al finalizar, el contenido del fichero XLS '${data.dir}/data1.xlsx' no existe en la base de datos usando la conexión 'db'
    * Al finalizar, se inserta el contenido del fichero CSV '${data.dir}/data1.csv' en la tabla client usando la conexión 'db'
    * Al finalizar, el contenido del fichero CSV '${data.dir}/data1.csv' existe en la tabla client usando la conexión 'db'
    * Al finalizar, se elimina el contenido del fichero CSV '${data.dir}/data1.csv' de la tabla client usando la conexión 'db'
    * Al finalizar, el contenido del fichero CSV '${data.dir}/data1.csv' no existe en la tabla client usando la conexión 'db'
    * Al finalizar, se insertan los siguientes usuarios en la tabla client usando la conexión 'db':
      | id | first_name | second_name | active | birth_date |
      | 1  | John       | Smith       | 1      | 2000-10-30 |
      | 2  | Annie      | Hall        | 0      | 2011-09-12 |
      | 3  | Bruce      | <null>      | 1      | 1982-12-31 |
    * Al finalizar, el siguiente registro existe en la tabla client usando la conexión 'db':
      | id | first_name | second_name | active | birth_date |
      | 1  | John       | Smith       | 1      | 2000-10-30 |
    * Al finalizar, se elimina el siguiente usuario de la tabla client usando la conexión 'db':
      | id | first_name | second_name | active | birth_date |
      | 1  | John       | Smith       | 1      | 2000-10-30 |
    * Al finalizar, el siguiente registro no existe en la tabla client usando la conexión 'db':
      | id | first_name | second_name | active | birth_date |
      | 1  | John       | Smith       | 1      | 2000-10-30 |
    * Al finalizar, el usuario identificado por '2' existe en la tabla client usando la conexión 'db'
    * Al finalizar, el usuario identificado por '1' no existe en la tabla client usando la conexión 'db'
    * Al finalizar, se elimina el usuario con first_name = 'Annie' de la tabla client usando la conexión 'db'
    * Al finalizar, el usuario con first_name = 'Annie' no existe en la tabla client usando la conexión 'db'
    * Al finalizar, el usuario con second_name = '<null>' existe en la tabla client usando la conexión 'db'
    * Al finalizar, el usuario que satisface la siguiente cláusula SQL existe en la tabla client usando la conexión 'db':
      """
      BIRTH_DATE < date '2000-01-01'
      """
    * Al finalizar, se eliminan los usuarios que satisfacen la siguiente cláusula SQL de la tabla client usando la conexión 'db':
      """
      BIRTH_DATE < date '2000-01-01'
      """
    * Al finalizar, el usuario que satisface la siguiente cláusula SQL no existe en la tabla client usando la conexión 'db':
      """
      BIRTH_DATE < date '2000-01-01'
      """
    * Al finalizar, se ejecuta el siguiente script SQL usando la conexión 'db':
      """
      DELETE FROM CLIENT_CITY;
      DELETE FROM CITY;
      DELETE FROM CLIENT;
      DELETE FROM OTHER;
      """
    * Al finalizar, el número de usuarios que satisfacen la siguiente cláusula SQL en la tabla client es 0 usando la conexión 'db':
      """
      1=1
      """
    * Al finalizar, el número de usuarios que satisfacen lo siguiente en la tabla client es 0 usando la conexión 'db':
      | id | first_name | second_name | active | birth_date |
      | 1  | John       | Smith       | 1      | 2000-10-30 |
    Dada la URL de conexión a BBDD '${database.connection.url}' usando el usuario '${database.connection.username}' y la contraseña '${database.connection.password}' como 'db'
    Y que se usa la conexión 'db'
    Cuando se inserta el contenido del fichero CSV '${data.dir}/data1.csv' en la tabla client
    Y se ejecuta el siguiente procedimiento SQL:
      """
      declare l_c CURSOR;
      BEGIN
           UPDATE CLIENT SET ACTIVE = 0 WHERE ID = 1;
          open l_c for SELECT * FROM CLIENT WHERE ID = 1;
      END;
      """
    Entonces el usuario identificado por '1' existe en la tabla client en 1 segundo
    Y usuarios con active = '0' existen en la tabla client en 1 segundo
    Y usuarios con active = '1' no existen en la tabla client en 1 segundo
    Y el número de usuarios con active = '0' en la tabla client es 1 en 1 segundo
    Y el usuario que satisface la siguiente cláusula SQL existe en la tabla client en 1 segundo:
      """
      BIRTH_DATE = date '1980-12-25'
      """
    Y los usuarios que satisfacen la siguiente cláusula SQL no existen en la tabla client en 1 segundo:
      """
      BIRTH_DATE > date '2000-01-01'
      """
    Y el número de usuarios que satisfacen la siguiente cláusula SQL en la tabla client es 1 en 1 segundo:
      """
      BIRTH_DATE = date '1980-12-25'
      """
    Y el siguiente registro existe en la tabla client en 1 segundo:
      | id | first_name | second_name | active | birth_date |
      | 1  | Rosa       | Melano      | 0      | 1980-12-25 |
    Pero el siguiente registro no existe en la tabla client en 1 segundo:
      | id | first_name | second_name | active | birth_date |
      | 1  | Rosa       | Melano      | 1      | 1980-12-25 |
    Y el número de registros que satisfacen lo siguiente en la tabla client es 1 en 1 segundo:
      | id | first_name | second_name | active | birth_date |
      | 1  | Rosa       | Melano      | 0      | 1980-12-25 |
    Y el contenido del fichero CSV '${data.dir}/data2.csv' existe en la tabla client en 1 segundo
    Pero el contenido del fichero CSV '${data.dir}/data1.csv' no existe en la tabla client en 1 segundo
    Y la tabla city está vacía en 1 segundo
    Y la tabla client no está vacía en 1 segundo


  Escenario: Test 4
    * Al finalizar, se ejecuta el script SQL del fichero '${data.dir}/db/clean.sql' usando la conexión 'db'
    * Al finalizar, se inserta el contenido del fichero XLS '${data.dir}/data1.xlsx' en la base de datos
    * Al finalizar, el contenido del fichero XLS '${data.dir}/data1.xlsx' existe en la base de datos
    * Al finalizar, se elimina el contenido del fichero XLS '${data.dir}/data1.xlsx' de la base de datos
    * Al finalizar, el contenido del fichero XLS '${data.dir}/data1.xlsx' no existe en la base de datos
    * Al finalizar, se inserta el contenido del fichero CSV '${data.dir}/data1.csv' en la tabla client
    * Al finalizar, el contenido del fichero CSV '${data.dir}/data1.csv' existe en la tabla client
    * Al finalizar, se elimina el contenido del fichero CSV '${data.dir}/data1.csv' de la tabla client
    * Al finalizar, el contenido del fichero CSV '${data.dir}/data1.csv' no existe en la tabla client
    * Al finalizar, se insertan los siguientes usuarios en la tabla client:
      | id | first_name | second_name | active | birth_date |
      | 1  | John       | Smith       | 1      | 2000-10-30 |
      | 2  | Annie      | Hall        | 0      | 2011-09-12 |
      | 3  | Bruce      | <null>      | 1      | 1982-12-31 |
    * Al finalizar, se elimina el siguiente usuario de la tabla client:
      | id | first_name | second_name | active | birth_date |
      | 1  | John       | Smith       | 1      | 2000-10-30 |
    * Al finalizar, se elimina el usuario con first_name = 'Annie' de la tabla client
    * Al finalizar, se eliminan los usuarios que satisfacen la siguiente cláusula SQL de la tabla client:
      """
      BIRTH_DATE < date '2000-01-01'
      """
    Dado la URL de conexión a BBDD '${database.connection.url}' usando el usuario '${database.connection.username}' y la contraseña '${database.connection.password}' como 'db'
    Y que se usa la conexión por defecto
    Cuando se ha insertado el contenido del fichero XLS '${data.dir}/data1.xlsx' en la base de datos
    Entonces el contenido del fichero XLS '${data.dir}/data1.xlsx' existe en la base de datos
    Y el contenido del fichero XLS '${data.dir}/data1.xlsx' existe en la base de datos en 1 segundo
    Y se recupera el valor de la siguiente consulta SQL:
      """
      SELECT * FROM CLIENT WHERE FIRST_NAME = 'Rosa';
      """
    Y el número de registros con id = '${-1#[0].ID}' en la tabla client es 1
    Y el siguiente registro no existe en la tabla client:
      | first_name | second_name | active | birth_date |
      | Rosa       | Melano      | 0      | 1980-12-25 |
    Y el número de registros que satisfacen lo siguiente en la tabla client es 1:
      | first_name | second_name | active | birth_date |
      | Rosa       | Melano      | 1      | 1980-12-25 |


  Escenario: Test 5
    * Al finalizar, se ejecuta el siguiente script SQL:
      """
      DELETE FROM CLIENT_CITY;
      DELETE FROM CITY;
      DELETE FROM CLIENT;
      DELETE FROM OTHER;
      """
    Dado que se inserta el contenido del fichero XLS '${data.dir}/data1.xlsx' en la base de datos
    Y el valor de la consulta SQL del fichero '${data.dir}/db/select.sql'
    Cuando se ha limpiado la tabla client_city
    Y se eliminan los usuarios con name = 'Valencia' de la tabla city
    Entonces el contenido del fichero CSV '${data.dir}/data1.csv' existe en la tabla client
    Y el contenido del fichero CSV '${data.dir}/data2.csv' no existe en la tabla client
    Y la ciudad identificada por '1' no existe en la tabla city
    Y la ciudad con name = 'Valencia' no existe en la tabla city
    Y el número de registros que satisfacen la siguiente cláusula SQL en la tabla client es 1:
      """
      ACTIVE = 1
      """
    Y los usuarios que satisfacen la siguiente cláusula SQL no existen en la tabla client:
      """
      ACTIVE = 0
      """


    Escenario: Test 6
      Dado que se inserta el contenido del fichero XLS '${data.dir}/data1.xlsx' en la base de datos
      Cuando se ha eliminado el contenido del fichero XLS '${data.dir}/data1.xlsx' de la base de datos
      Entonces el contenido del fichero XLS '${data.dir}/data1.xlsx' no existe en la base de datos
      Y el contenido del fichero XLS '${data.dir}/data1.xlsx' no existe en la base de datos en 1 segundo


  # database.enableCleanupUponCompletion: true
  Escenario: Cleanup of data with foreign key
    * Al finalizar, la tabla client está vacía
    * Al finalizar, la tabla city está vacía
    * Al finalizar, la tabla client_city está vacía
    Dado que se inserta el siguiente usuario en la tabla client:
      | id | first_name | second_name | active | birth_date |
      | 1  | Rosa       | Melano      | 1      | 1980-12-25 |
    Y se inserta la siguiente ciudad en la tabla city:
      | id | name     |
      | 1  | Valencia |
    Y se inserta la siguiente relación en la tabla client_city:
      | clientid | cityid |
      | 1        | 1      |
    Cuando se ha limpiado la tabla client_city
    Y se ha ejecutado el siguiente script SQL:
      """
      DELETE FROM CITY WHERE id = 1;
      INSERT INTO city (ID, name, latitude, longitude) VALUES (1, 'Valencia', 39.469906, -0.376288);
      UPDATE city SET latitude = 40.469906 WHERE id = 1;
      """
    Y se ha eliminado el contenido del fichero CSV '${data.dir}/data1.csv' de la tabla client