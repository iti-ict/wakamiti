#language: es
Característica: : jmeter demo

     Escenario: Prueba de carga
       Dada la URL base 'http://localhost:8888'
       Y hago un GET al endpoint '/inicio' y extraigo el valor en el Json Path '$.mensaje' y lo almaceno en 'prueba'
       Y hago un PUT al endpoint '/actualizar' con la variable almacenada 'prueba' como cuerpo del mensaje
       Y hago un POST al endpoint '/login' / y la variable 'prueba' extraida previamente con el siguiente mensaje:
       """
       {
       "nombre": "Ana",
       "apellido": "Lopez"
       }
       """
       Cuando ejecuto una prueba de carga con 40 usuarios durante 1 minutos
       Entonces comprueba que el percentil 99 de tiempo de respuesta es menor que 5 segundos

    # Escenario: Prueba CSV
    #   Dada la URL base 'http://localhost:8888'
    #   Y un fichero con los siguientes datos './src/test/resources/UsersData.csv' trabajando con las variables:
    #     |variables|
    #     |Username|
    #   Cuando ejecuto una prueba de carga con 40 usuarios durante 1 minutos
    #   Entonces comprueba que el percentil 99 de tiempo de respuesta es menor que 5 segundos

