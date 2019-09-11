#language: es

@definition
Característica: Operaciones con usuarios

   @ID-1
   Escenario: Se consulta un usuario existente
      Dado un usuario existente
      Cuando se consultan los datos del usuario
      Entonces retorna el nombre del usuario
