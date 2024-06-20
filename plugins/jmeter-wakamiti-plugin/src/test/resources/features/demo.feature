#language: es
Característica: jmeter demo

#     Escenario: Prueba de carga
#       Dada la URL base 'http://localhost:8888'
#       Y se realiza un GET al endpoint '/inicio' y se extrae el valor en el Json Path '$.mensaje' almacenándolo en 'prueba'
#       Y se realiza un PUT al endpoint '/actualizar' con la variable almacenada 'prueba' como cuerpo del mensaje
#       Y se realiza un POST al endpoint '/login' / y la variable 'prueba' extraida previamente con el siguiente mensaje:
#       """
#       {
#       "nombre": "Ana",
#       "apellido": "Lopez"
#       }
#       """
#       Cuando se ejecuta una prueba de carga con 40 usuarios durante 1 minutos
#       Entonces comprueba que el percentil 99 de tiempo de respuesta es menor que 5 segundos

    # Escenario: Prueba CSV
    #   Dada la URL base 'http://localhost:8888'
    #   Y se realiza un POST usando un fichero con los siguientes datos './src/test/resources/UsersData.csv' trabajando con las variables:
    #     |variables|
    #     |Username|
    #   Cuando se ejecuta una prueba de carga con 40 usuarios durante 1 minutos
    #   Entonces comprueba que el percentil 99 de tiempo de respuesta es menor que 5 segundos

