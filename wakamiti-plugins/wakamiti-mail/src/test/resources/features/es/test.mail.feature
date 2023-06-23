# charset: UTF-8
# language: es

Característica: Comprobaciones en el gestor de correo

  Antecedentes:
    Dado el servidor de correo 'abc.example.com':1234 con el protocolo 'protocol'
    Y el usuario de correo 'mail@example' con las credenciales 'password'
    Entonces se obtienen los correos

  Escenario: Leer el título del correo
    Y se lee el título del correo

  Escenario: Leer el remitente del correo
    Y se lee el remitente del correo

  Escenario: Leer el destinatario del correo
    Y se lee el destinatario del correo

  Escenario: Leer el cuerpo del correo
    Y se lee el cuerpo del correo

  Escenario: Comprobar si hay archivos adjuntos
    Y se comprueba si hay archivos adjuntos

  Escenario: Marcar como leído correo
    Entonces se marca como leído el correo


#
#  Escenario: Comprobar el estado del correo
#    Entonces se comprueba el estado del correo

#  Escenario: Borrar el correo
#    Entonces se borra el correo
#
