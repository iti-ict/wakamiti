# language: es
@implementation @consultarUsuario
Característica: Consultas de los propietarios de las mascotas


  Antecedentes:
    Dado el servicio REST '/owners'


  @ID-consultarUsuario01
    # redefinition.stepMap: 2-1-2
  Escenario: Se consulta un usuario
    Dado un usuario identificado por '20'
    Y que se ha insertado los siguientes datos en la tabla de BBDD owners:
      | ID | FIRST_NAME | LAST_NAME      |
      | 20 | Pepe       | Perez Martinez |
    Cuando se consulta el usuario

    Entonces el código de respuesta HTTP es 200
    Y la respuesta es parcialmente:
      """json
      {
        "id": 20,
        "firstName": "Pepe",
        "lastName": "Perez Martinez"
      }
      """

  @ID-consultarUsuario02
    # redefinition.stepMap: 1-1-1
  Escenario: Se consulta un usuario que no existe
    Dado un usuario identificado por '2000'
    Cuando se consulta el usuario
    Entonces el código de respuesta HTTP es 404
