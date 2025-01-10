# language: es
@definition @consultarUsuario
Característica: Consultas de los propietarios de las mascotas


  @ID-consultarUsuario01
  Escenario: Se consulta un usuario
    Dado que existe un dueño que tiene una mascota
    Cuando se busca al dueño por el nombre
    Entonces se obtiene el nombre y los apellidos de la persona

  @ID-consultarUsuario02
  Escenario: Se consulta un usuario que no existe
    Dado que el dueño que se quiere consultar no existe en el sistema
    Cuando se busca al dueño por el nombre
    Entonces se obtiene del sistema la siguiente respuesta: "No existe el usuario en el sistema"
