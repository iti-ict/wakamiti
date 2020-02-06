#language: es
#modules: database-steps, rest-steps
Característica: Operaciones del servicio de usuarios

   Antecedentes:
     Dado el servicio REST '/users'

   Escenario: Se consulta un usuario existente
      Dado un usuario identificado por '3'
      Y que se ha insertado los siguientes datos en la tabla de BBDD USER:
        | ID | FIRST_NAME | LAST_NAME |
        | 3  | Samuel L.  | Jackson   |
      Cuando se consulta el usuario
      Entonces el código de respuesta HTTP es 200
      Y la respuesta es parcialmente:
      """
      { "firstName": "Samuel L." }
      """