#language: es
Característica: : jmeter demo

     Escenario: Prueba de carga
       Dada la URL base 'http://localhost:8888'
       Cuando ejecuto una prueba de carga con 400 usuarios durante 2 minutos
       Entonces comprueba que el percentil 99 de tiempo de respuesta es menor que 5 segundos



     Escenario: Prueba de estres
       Dada la URL base 'http://localhost:8888'
       Cuando ejecuto una prueba de estrés comenzando con 100 usuarios, incrementando en 100 hasta 400 usuarios durante 1 minutos
       Entonces comprueba que el percentil 99 de tiempo de respuesta es menor que 5 segundos


     Escenario: Prueba de picos
       Dada la URL base 'http://localhost:8888'
       Cuando ejecuto una prueba de picos con 2 picos de 100 usuarios, bajando a 20 usuarios durante 1 minutos
       Entonces comprueba que el percentil 99 de tiempo de respuesta es menor que 5 segundos

