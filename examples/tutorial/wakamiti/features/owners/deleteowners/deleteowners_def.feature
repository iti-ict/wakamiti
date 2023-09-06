# language: es
@definition @eliminarPropietario
Característica: Eliminar un propietario de mascotas


  @ID-eliminarPropietario01
  Escenario: Se elimina un usuario
    Dado que existe un dueño que tiene una mascota
    Cuando se elimina al dueño
    Entonces el dueño ya no existe en el sistema


  @ID-eliminarPropietario02
  Escenario: Se elimina un usuario que no existe
    Dado que el dueño que se quiere eliminar no existe en el sistema
    Cuando se elimina el usuario
    Entonces se obtiene del sistema la siguiente respuesta: "No existe el usuario en el sistema"
