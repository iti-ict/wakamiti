# language: es
Característica: Pruebas de API REST con Spring Petclinic


  Escenario: Se consulta un usuario
    Dado las siguientes variables:
      | name   | value |
      | userId | 20    |
    Y que se ha insertado los siguientes datos en la tabla de BBDD owners:
      | ID | FIRST_NAME | LAST_NAME      |
      | 20 | Pepe       | Perez Martinez |
    Y la cola de destino test
    Y una llamada GET al servicio '/users/{userId}'
    Cuando se ejecuta 1 hilo
    Y se envía a la cola test el siguiente mensaje JSON:
      """json
      {
        "orderId": 12345,
        "customer": "John Doe",
        "status": "pending"
      }
      """
    Y se suma 1 y 2
    Entonces el siguiente fragmento JSON se recibe en 5 segundos:
      """json
      {
        "customer": "John Doe"
      }
      """
    Y se ejecuta el siguiente código groovy:
      """groovy
      assert 1 == 1
      """
    Y el fichero 'abc' no existe
    Y el número de correos sin leer es 0