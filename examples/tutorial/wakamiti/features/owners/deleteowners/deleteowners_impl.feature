# language: es
@implementation @eliminarPropietario
Característica: Eliminar un propietario de mascotas


  Antecedentes:
    Dado el servicio REST '/owners'


  @ID-eliminarPropietario01
    # redefinition.stepMap: 2-1-2
  Escenario: Se elimina un usuario
    Dado un usuario identificado por '30'
    Y se ejecuta el script SQL del fichero 'data/deleteowner.sql'
    Cuando se elimina el dueño
    Entonces el código de respuesta HTTP es 204
    Y el siguiente registro no existe en la tabla de BBDD owners:
      | ID    |
      | 40000 |

  @ID-eliminarPropietario02
    # redefinition.stepMap: 1-1-1
  Escenario: Se elimina un usuario que no existe
    Dado un usuario identificado por '2000'
    Cuando se elimina el usuario
    Entonces el código de respuesta HTTP es 404
      