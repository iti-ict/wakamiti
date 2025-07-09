---
title: Pasos Modbus
date: 2025-07-09
slug: /plugins/modbus
---


Este plugin proporciona un conjunto de pasos para interactuar con dispositivos y sistemas que utilizan el protocolo 
Modbus, un protocolo de comunicación industrial ampliamente utilizado en la automatización y control de procesos.

Modbus es un protocolo de comunicación serie desarrollado originalmente por Modicon (ahora Schneider Electric) en 
1979 para su uso con controladores lógicos programables (PLCs). Actualmente, es uno de los protocolos más comunes 
utilizados en la industria para conectar dispositivos electrónicos industriales debido a su simplicidad y robustez.

Este plugin implementa la variante Modbus TCP, que permite la comunicación a través de redes TCP/IP, facilitando la 
integración con sistemas modernos y la comunicación a través de redes Ethernet estándar. Con este plugin, podrás:

- Establecer conexiones con dispositivos Modbus TCP
- Leer valores de registros de dispositivos Modbus
- Escribir valores en registros específicos
- Verificar los valores leídos para validar el comportamiento del sistema


---
## Tabla de contenido

---


## Instalación


Incluye el módulo en la sección correspondiente.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:modbus-wakamiti-plugin:1.0.0
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>modbus-wakamiti-plugin</artifactId>
  <version>1.0.0</version>
</dependency>
```


## Configuración

### `modbus.host`
- Tipo: `string`
- Por defecto: `localhost`

Establece la dirección IP o nombre de host del dispositivo Modbus maestro al que se conectará el plugin. Este 
parámetro es fundamental para establecer la comunicación con el servidor Modbus TCP.

Ejemplo:
```yaml
modbus:
  host: 172.17.0.1
```


### `modbus.port`
- Tipo: `integer`
- Por defecto: `5020`

Establece el puerto TCP utilizado para la comunicación con el dispositivo Modbus maestro. El puerto estándar para 
Modbus TCP es 502, aunque en muchos entornos se utilizan puertos alternativos por razones de seguridad o 
configuración de red.

Ejemplo:
```yaml
modbus:
  port: 5021
```


### `modbus.slaveId`
- Tipo: `integer`
- Por defecto: `1`

Establece el identificador del dispositivo esclavo Modbus con el que se desea comunicar. En una red Modbus, cada 
dispositivo esclavo tiene un identificador único que permite al maestro dirigir comandos específicamente a ese 
dispositivo.

Los valores válidos para el ID de esclavo están en el rango de 1 a 247, siendo el valor 1 comúnmente utilizado para 
el primer dispositivo en muchas configuraciones. En redes con múltiples dispositivos Modbus, es esencial configurar 
correctamente este parámetro para comunicarse con el dispositivo deseado.

Ejemplo:
```yaml
modbus:
  slaveId: 11
```


## Pasos

### Definir URL base
```text copy=true
la URL base tcp://{host}:{port}
```
Establece la dirección y puerto de conexión para el dispositivo Modbus maestro. 
Este paso es equivalente a configurar las propiedades [`modbus.host`](#modbushost) y [`modbus.port`](#modbusport) 
simultáneamente.

#### Parámetros:
| Nombre | Wakamiti type           | Descripción |
|--------|-------------------------|-------------|
| `host` | `word` *obligatorio*    | host base   |
| `port` | `integer` *obligatorio* | puerto base |

#### Ejemplos:
```gherkin
Dada la URL base tcp://example.org:5021
```

### Definir ID esclavo
```text copy=true
el id esclavo {id}
```
Establece el identificador del dispositivo esclavo Modbus con el que se desea comunicar. Este paso es equivalente a 
configurar la propiedad [`modbus.slaveId`](#modbusslaveid).

#### Parámetros:
| Nombre | Wakamiti type           | Descripción |
|--------|-------------------------|-------------|
| `id`   | `integer` *obligatorio* | id esclavo  |

#### Ejemplos:
```gherkin
Dado el id esclavo 11
```


### Leer registros
```text copy=true
se leen {quantity} registros a partir de la posición {address}
```
Lee una cantidad específica de registros de retención (holding registers) desde una dirección determinada en el 
dispositivo Modbus esclavo. Los registros de retención son uno de los tipos de datos más comunes en Modbus y se 
utilizan para almacenar valores que pueden ser tanto leídos como escritos.

Este paso ejecuta la función Modbus 03 (Read Holding Registers) y almacena internamente los valores leídos para su 
posterior verificación. Cada registro leído es un valor de 16 bits (2 bytes) que puede representar diversos tipos de 
datos según la implementación del dispositivo.

Es importante tener en cuenta que:
- La dirección de inicio debe ser válida para el dispositivo específico
- La cantidad de registros solicitada no debe exceder la capacidad del dispositivo
- Los registros se numeran desde 0, aunque algunas documentaciones de dispositivos pueden referenciarlos desde 1
- Este paso debe ejecutarse después de establecer correctamente el ID del esclavo

#### Parámetros:
| Nombre     | Wakamiti type           | Descripción                        |
|------------|-------------------------|------------------------------------|
| `quantity` | `integer` *obligatorio* | Cantidad de registros a leer       |
| `address`  | `integer` *obligatorio* | Dirección inicial de los registros |

#### Ejemplos:
```gherkin
Cuando se leen 5 registros a partir de la posición 100
```


### Escribir valor
```text copy=true
se escribe el valor {value} en la posición {address}
```
Escribe un valor entero en un registro de retención específico del dispositivo Modbus esclavo. Este paso permite 
modificar el estado o configuración del dispositivo Modbus remoto.

Este paso ejecuta la función Modbus 06 (Write Single Register), que permite escribir un único valor de 16 bits en 
una dirección de registro específica. Es una de las operaciones más comunes para controlar o configurar dispositivos 
Modbus.

Consideraciones importantes:
- El valor a escribir debe estar dentro del rango permitido para un registro de 16 bits (0-65535)
- La dirección del registro debe ser válida y accesible para escritura en el dispositivo
- Algunos dispositivos pueden tener registros de solo lectura o con restricciones específicas
- Este paso debe ejecutarse después de establecer correctamente el ID del esclavo
- Es recomendable verificar el valor escrito mediante una operación de lectura posterior

#### Parámetros:
| Nombre    | Wakamiti type           | Descripción                      |
|-----------|-------------------------|----------------------------------|
| `value`   | `integer` *obligatorio* | Valor a escribir                 |
| `address` | `integer` *obligatorio* | Posición donde escribir el valor |

#### Ejemplos:
```gherkin
Cuando se escribe el valor 42 en la posición 100
```


### Comprobar valor leído
```text copy=true
los registros leídos contienen el valor {value}
```
Verifica que entre los registros leídos previamente (mediante el paso [Leer registros](#leer-registros)) existe al 
menos uno que contiene exactamente el valor especificado. Este paso es fundamental para validar el comportamiento 
esperado del dispositivo Modbus y confirmar que los valores de los registros son correctos.

Este paso realiza una búsqueda en el array de valores obtenidos en la última operación de lectura y genera un error 
si el valor especificado no se encuentra en ninguno de los registros leídos.

Consideraciones importantes:
- Este paso debe ejecutarse después de un paso de lectura de registros
- Si no se ha realizado ninguna lectura previa, se generará un error
- La comparación es exacta (debe coincidir el valor entero completo)
- Si se necesita verificar valores en posiciones específicas, se recomienda realizar lecturas individuales para cada 
  posición

#### Parámetros:
| Nombre  | Wakamiti type           | Descripción                     |
|---------|-------------------------|---------------------------------|
| `value` | `integer` *obligatorio* | Valor a buscar en los registros |

#### Ejemplos:
```gherkin
Entonces los registros leídos contienen el valor 42
```
