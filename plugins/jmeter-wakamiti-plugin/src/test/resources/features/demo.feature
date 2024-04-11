#language: es
Característica: : jmeter demo

     Escenario: Prueba de carga
       Dada la URL base 'http://localhost:8888'
       Y hago un GET al endpoint '/inicio' y extraigo el valor en el Json Path '$.mensaje' y lo almaceno en 'prueba'
       Y hago un PUT al endpoint '/actualizar' con la variable almacenada 'prueba' como cuerpo del mensaje
       Cuando ejecuto una prueba de carga con 40 usuarios durante 1 minutos
       Entonces comprueba que el percentil 99 de tiempo de respuesta es menor que 5 segundos

