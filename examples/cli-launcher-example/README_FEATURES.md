# Guía de Features Wakamiti - Ejemplo CLI Launcher

Este proyecto contiene **9 características de prueba** que demuestran el uso de todos los plugins disponibles en Wakamiti.

## 📊 Resumen de Coverage

| Plugin | Feature | Escenarios | Descripción |
|--------|---------|-----------|-------------|
| **REST** | `rest/rest.feature` | 10 | Pruebas de API HTTP: GET, POST, PUT, PATCH, DELETE |
| **Database** | `db/database.feature` | 12 | Consultas SQL, validaciones, joins, agregaciones |
| **Email** | `email/email.feature` | 10 | Recepción de correos, validación de contenido, adjuntos |
| **AMQP** | `amqp/amqp.feature` | 10 | Mensajería, colas, intercambios, propiedades |
| **Modbus** | `modbus/modbus.feature` | 11 | Protocolo TCP, lectura/escritura de registros |
| **Appium** | `appium/appium.feature` | 14 | Automatización móvil, interacciones, validaciones |
| **I/O** | `io/io.feature` | 15 | Operaciones con archivos y directorios |
| **Groovy** | `groovy/groovy.feature` | 11 | Scripts inline, lógica personalizada |
| **JMeter** | `jmeter/jmeter.feature` | 16 | Pruebas de carga, stress testing, métricas |

**Total: 109 escenarios de prueba**

## 🐳 Docker Compose Services

El archivo `docker-compose.yml` incluye los siguientes servicios simulados:

- **Spring Petclinic (REST/DB)**: Puerto 9966
- **MySQL**: Puerto 3309
- **RabbitMQ (AMQP)**: Puerto 5672, Management: 15672
- **GreenMail (Email)**: SMTP 1025, IMAP 3143
- **Modbus TCP Server**: Puerto 502

## ✨ Ejemplos de Pasos Correctos

### REST Plugin
```gherkin
Dada la URL base http://localhost:9966
Y JSON como el tipo de contenido REST
Dado el servicio REST '/api/pets'
Cuando se realiza la búsqueda de mascotas
Entonces el código de respuesta HTTP es 200
```

### Database Plugin
```gherkin
Dada la URL de conexión a BBDD 'jdbc:mysql://localhost:3309/petclinic' 
  usando el usuario 'root' y la contraseña 'petclinic'
Cuando se recupera los valores de la siguiente consulta SQL:
  """sql
  SELECT * FROM pets WHERE id = 1
  """
Entonces el resultado contiene:
  """json
  [{"id": 1, "name": "Leo"}]
  """
```

### Email Plugin
```gherkin
Dada el servidor de correo ubicado en 'localhost':3143 usando el protocolo imap
Y el usuario de correo con dirección 'test@example.com' y contraseña 'test'
Cuando se recibe un nuevo correo en los próximos 30 segundos
Entonces el asunto del correo empieza por 'Confirmación'
```

### AMQP Plugin
```gherkin
Dada la conexión AMQP con URL 'amqp://localhost:5672' 
  usando el usuario 'admin' y la contraseña 'admin'
Y la cola de destino 'order.queue'
Cuando se envía a la cola 'order.queue' el siguiente mensaje JSON:
  """json
  {"orderId": 12345, "status": "pending"}
  """
Entonces el mensaje se envía correctamente
```

### Modbus Plugin
```gherkin
Dada la dirección del servidor Modbus es 'localhost:502'
Y el identificador de esclavo es 1
Cuando se conecta al servidor Modbus
Entonces la conexión es exitosa
```

### Appium Plugin
```gherkin
Dada la conexión Appium en 'localhost:4723'
Y las capacidades del dispositivo:
  | nombre       | valor                      |
  | platformName | Android                    |
  | app          | ./apps/ApiDemos-debug.apk  |
Cuando busco el elemento con ID 'io.appium.android.apis:id/action_bar'
Entonces encuentro el elemento
```

### I/O Plugin
```gherkin
Dado que existe el fichero 'test-files/input.txt'
Cuando verifico la existencia del fichero
Entonces el fichero existe
```

### Groovy Plugin
```gherkin
Cuando ejecuto el siguiente script Groovy:
  """groovy
  def a = 10
  def b = 20
  return a + b
  """
Entonces el script realiza los cálculos correctamente
```

### JMeter Plugin
```gherkin
Dada la URL base http://localhost:9966
Y JSON como el tipo de contenido
Dado una llamada GET al servicio '/api/pets'
Cuando se ejecutan 10 hilos en 5 segundos manteniendo 30 segundos
Entonces la solicitud se ejecuta correctamente
```

## 🚀 Ejecutar las Pruebas

1. **Iniciar servicios Docker:**
```bash
docker-compose up -d
```

2. **Ejecutar todas las features:**
```bash
mvn clean verify
```

3. **Ejecutar feature específico:**
```bash
mvn verify -Dcucumber.features=rest/rest.feature
```

4. **Ejecutar con tag específico:**
```bash
mvn verify -Dcucumber.features=rest/rest.feature -Dcucumber.tags=@api
```

## 📝 Notas Importantes

- Los pasos usan **parámetros tipados** (text, url, document, table, etc.)
- Las tablas de datos usan el formato **Gherkin estándar**
- Los pasos están en **español** siguiendo la convención del proyecto
- Los ejemplos usan **Spring Petclinic** como aplicación de prueba
- Los simuladores están configurados con credenciales por defecto (ajustar según necesidad)

## 🔗 Referencias

- Documentación oficial: `wakamiti-doc/docs/plugins/`
- Archivos de bundles i18n: En cada plugin `src/main/resources/`
- Ejemplos detallados: Cada archivo `.feature` contiene Antecedentes y ejemplos completos
