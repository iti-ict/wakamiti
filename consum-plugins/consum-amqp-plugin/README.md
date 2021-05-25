Kukumo - AMQP Plugin
================================

Este plugin ofrece pasos relacionados con el envío y 
recepción de mensajes mediante el protocolo de mensajería
AMQP.

Un ejemplo de escenario sería:

```gherkin
Característica: Mensajería AMQP

    Escenario: Enviar y recibir un mensaje

        Dada la conexión AMQP con URL 'amqp://127.0.0.1:5671' usando el usuario 'usuario1' y la contraseña 'pwd'
        Y la cola de destino COLA_DESTINO
        Cuando se envía a la cola COLA_ORIGEN el siguiente mensaje JSON:
        """json
            {
                "data": {
                    "message": "Mensaje enviado"
                }
            }
        """
        Entonces el siguiente mensaje JSON se recibe en 5 segundos:
        """json
            {
                "data": {
                    "message": "Mensaje recibido"
                }
            }
        """
```

Pasos disponibles
---------------------------------

### `la conexión AMQP con URL {url:text} usando el usuario {username:text} y la contraseña {password:text}`
Define los parámetros de conexión con el broker AMQP. Alternativamente, se puede definir con 
parámetros de configuración.

### `se envía a la cola {word} el siguiente mensaje JSON:`
Envía a una cola un mensaje de texto indicado en el propio paso. El mensaje será
enviado con la propiedad `content-type: application/json`.

### `se envía a la cola {queue:word} el mensaje del fichero JSON {file:file}`
Envía a una cola un mensaje de texto almacenado en un fichero. El mensaje será
enviado con la propiedad `content-type: application/json`.

### `la cola de destino {word}`
Declara una cola de la que se van a recibir mensajes. Es necesario definir este paso *antes* de que 
se realice algún paso de comprobación de mensajes recibidos.
> **AVISO**  
> Los mensajes enviados a esta cola por la aplicación serán consumidos por el test y 
> dejarán de estar disponibles para el resto del sistema

### `el siguiente mensaje JSON se recibe en {integer} segundos:`
Comprueba si un determinado mensaje, indicado en el propio paso, se ha recibido en la cola de destino.
El test se marcará como fallido si no se recibe en un lapso de tiempo determinado.


### `el mensaje del fichero JSON {file:file} se recibe en {seconds:integer} segundos`
Comprueba si un determinado mensaje, almacenado en un fichero, se ha recibido en la cola de destino.
El test se marcará como fallido si no se recibe en un lapso de tiempo determinado.


Parámetros de configuración
----------------------------------

- `amqp.connection.url` : URL de conexión con el broker AMQP 
- `amqp.connection.username` : Nombre de usuario para la conexión
- `amqp.connection.password` : Contraseña de usuario para la conexión