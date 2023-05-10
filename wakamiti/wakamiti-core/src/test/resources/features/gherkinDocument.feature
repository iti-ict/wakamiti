# charset: UTF-8
# language: es
Característica: Test Spanish 
  Esto es una prueba de definición de característica en Español.
  Los casos son triviales.

Antecedentes: Estos son los antecedentes
  Dado el conjunto de números reales ℝ

Escenario: Escenario de test
 Dado un número con valor 6,1 y otro número con valor 3
 Cuando se multiplican ambos números
 Entonces el resultado es 18,3
 
@ID-1
Esquema del escenario: Esquema de escenario de test
 Dado un número con valor <a> y otro número con valor <b>
 Cuando se multiplican ambos números
 Entonces el resultado es <c>
 Ejemplos:
 | a   | b |  c   |
 | 1,0 | 2 |  2,0 |
 | 2,0 | 3 |  6,0 |
 | 5,0 | 4 | 20,0 |

@ID-2
Escenario: Escenario de test con tabla
 Dado un número con valor 3,1 y los siguientes datos:
  | 1,1 |
  | 2,1 |
  | 3,1 | 
 Cuando se multiplica la tabla por el número
 Entonces los resultados son los siguientes:
  | 3,41 |
  | 6,51 |
  | 9,61 |

@ID-3
Escenario: Escenario de test con texto  
 Dada la palabra pepino y el siguiente párafro:
 """
 pepinos y calabazas, dulces vegetables,
 pepinos a pares,
 pepinos en bares
 """
 Entonces cada línea empieza por la palabra dada