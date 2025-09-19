# language: es
@definition @crearUsuario
Característica: Alta de un nuevo propietario de mascota


  @ID-crearUsuario01
  Escenario: Se crea un usuario
    Dado que hay una persona que quiere llevar a su mascota a la tienda y que no está registrado
    Cuando se piden los datos necesarios para el alta: Nombre, apellidos, dirección, ciudad y teléfono
    Entonces sus datos quedan registrados en el sistema

  @ID-crearUsuario02
  Escenario: Se crea un usuario con datos erróneos
    Dado que hay una persona que quiere llevar a su mascota a la tienda y que no está registrado
    Cuando se insertan mal lo datos en el sistema
    Entonces el sistema no almacena el usuario
