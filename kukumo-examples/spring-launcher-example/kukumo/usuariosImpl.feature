#language: es
@implementation
Característica: Operaciones con usuarios

   Antecedentes:
      Dada la URL de conexión a BBDD 'jdbc:h2:tcp://localhost:9092/~/test' usando el usuario 'sa' y la contraseña ''
      Y la URL base http://localhost:9191
      Y el servicio REST '/users'

   # redefinition.stepMap: 2-1-2
   @ID-1
   Escenario: Se consulta un usuario existente
      Dado un usuario identificado por '3'
      Y que se ha insertado los siguientes datos en la tabla de BBDD USER:
      | ID | FIRST_NAME | LAST_NAME |
      | 3  | Pepe       | Perez     |
      Cuando se consulta el usuario
      Entonces el código de respuesta HTTP es 200
      Y la respuesta es parcialmente:
      """
       { "firstName": "Pepe" }
      """
