# language: es
Característica: Pasos Java custom en Wakamiti

  @ID-java-1
  Escenario: Ejecutar StepContributor Java custom
    * Al finalizar, se ejecuta la accion 'limpieza custom'
    Dado un numero base 7
    Cuando el numero se multiplica por 2
    Entonces el resultado calculado es 14
