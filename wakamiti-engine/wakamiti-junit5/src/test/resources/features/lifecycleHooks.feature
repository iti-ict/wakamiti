# language: es
@ID-Test7
Característica: Test 7 - Lifecycle hooks

Antecedentes:
  Dado el conjunto de números reales ℝ

@Before
Escenario: Setup feature execution
  Dado un número con valor 4,0 y otro número con valor 5
  Cuando se multiplican ambos números
  Entonces el resultado es 20,0

@ID-Test7_Scenario1
Escenario: Functional scenario
  Dado un número con valor 8,02 y otro número con valor 9
  Cuando se multiplican ambos números
  Entonces el resultado es 72,18

@After
Escenario: Teardown feature execution
  Dado un número con valor 4,0 y otro número con valor 4
  Cuando se multiplican ambos números
  Entonces el resultado es 16,0
