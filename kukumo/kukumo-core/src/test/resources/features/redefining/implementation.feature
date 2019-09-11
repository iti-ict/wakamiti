# language: es
@implementation
Característica: Test feature [Spanish]
  Esto es una prueba de definición de característica en Español.
  Los casos son triviales.

Antecedentes: Estos son los antecedentes
  Dado el conjunto de números reales ℝ

# redefinition.stepMap: 2-1-2
@ID-1
Escenario: Escenario de test en español
 Dado un número con valor 6,1 y otro número con valor 3
 Y un número con valor 6,1 y otro número con valor 3
 Cuando se multiplican ambos números
 Entonces el resultado es 18,3
 Y el resultado es 18,3


@ID-2
# Examples provided by the definition feature
# dataFormatLanguage: en
# redefinition.stepMap: 0-1-1-2
Esquema del escenario: Esquema de escenario de test en español
 # Given a number with value <a>
 # And another number with value <b>
 Dado un número con valor <a> y otro número con valor <b>
 # When they are multiplied
 Cuando se multiplican ambos números
 # Then the result is <c>
 Entonces el resultado es <c>
 Y el resultado es <c>


