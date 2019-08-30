#language: es

Característica: Operaciones con usuarios

   Antecedentes:
      Dada la conexión a BBDD 'jdbc:h2:tcp://localhost:9092/~/test' usando el usuario 'sa' y la contraseña ''
      Y la URL base http://localhost:9191
      Y el servicio REST '/user'

   Escenario: Se consulta un usuario existente
      Dado un usuario identificado por '3'
      Y los siguientes datos se insertan en la tabla de BBDD USER :
	| ID | FIRST_NAME | LAST_NAME |
        | 3  | Pepe       | Perez     |
      Cuando se consulta el usuario
      Entonces el código de respuesta HTTP es 200
      Y la respuesta es parcialmente:
      """
      { "firstName": "Pepe" }
      """