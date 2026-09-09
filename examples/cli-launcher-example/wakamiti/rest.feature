# language: es
Característica: Pruebas de API REST con Spring Petclinic


  Antecedentes:
    Dado el servicio REST '/owners/{id}'

  @Before
  Escenario: Inicialización de la característica
    * se ha insertado el siguiente registro en la tabla de BBDD owners:
      | ID | FIRST_NAME | LAST_NAME      |
      | 20 | Pepe       | Perez Martinez |

  @ID-1
  Escenario: Se consulta un usuario
    Dado el parámetro de ruta 'id' con el valor '20'
    Y la cola de destino test
    # id: s1
    Cuando se consulta el usuario
    Y se envía a la cola test el siguiente mensaje JSON:
      """json
      {
        "orderId": 12345,
        "customer": "John Doe",
        "status": "pending"
      }
      """
    Y se suma 1 y 2
    Entonces el código de respuesta HTTP es 200
    Y la respuesta es parcialmente:
      """json
      {
        "id": 20,
        "firstName": "Pepe",
        "lastName": "Perez Martinez"
      }
      """
    Y el siguiente fragmento JSON se recibe en 5 segundos:
      """json
      {
        "customer": "John Doe"
      }
      """
    Y se ejecuta el siguiente código groovy:
      """groovy
      assert ctx.results['s1'].body.firstName?.asText() == 'Pepe'
      """
    Y el fichero 'abc' no existe
    Y el número de correos sin leer es 0

  @After
  Escenario: Finalización de la catacterística
    * se ha eliminado el siguiente registro de la tabla owners:
      | ID | FIRST_NAME | LAST_NAME      |
      | 20 | Pepe       | Perez Martinez |