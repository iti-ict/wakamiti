# language: es
@implementation @crearUsuario
Característica: Alta de un nuevo usuario con mascota


  Antecedentes:
    Dado el servicio REST '/owners'


  @ID-crearUsuario01 @test
    # redefinition.stepMap: 2-1-2
  Escenario: Se crea un usuario
    * Al finalizar, se ejecuta el siguiente script SQL:
      """sql
      delete from owners where first_name = 'Pascual'
      """
    Dado que un dueño con first_name = 'Pascual' no existe en la tabla de BBDD owners
    Cuando se crea el dueño con los siguientes datos:
      """json
      {
        "firstName": "Pascual",
        "lastName": "Carlos",
        "telephone": "608552013",
        "address": "C/ prueba",
        "city": "Valencia",
        "id": 20
      }
      """
    Entonces el código de respuesta HTTP es 201
    Y el dueño con first_name = 'Pascual' existe en la tabla de BBDD owners

  @ID-crearUsuario02 @test_aceptacion
    # redefinition.stepMap: 1-1-2
  Escenario: Se crea un usuario con datos erróneos
    Dado que un dueño con first_name = 'Pruebas' no existen en la tabla de BBDD owners
    Cuando se crea el dueño con los siguientes datos:
      """json
      {
        "firstName": "Pruebas automáticas",
        "lastName": "Pruebas automáticas",
        "id": 0
      }
      """
    Entonces el código de respuesta HTTP es 400
    Y dueño con ID = '30000' no existe en la tabla de BBDD owners
