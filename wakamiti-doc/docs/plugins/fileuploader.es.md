---
title: File Uploader
date: 2023-05-29
slug: /plugins/fileuploader
---

Este plugin registra un observador de eventos que se dispara cuando se genera un fichero de 
salida, e intenta subir dicho fichero a una ubicación remota usando el protocolo FTP (o FTPS).
Más específicamente, se dispara ante cualquier evento de tipo `STANDARD_OUTPUT_FILE_WRITTEN`,
`TEST_CASE_OUTPUT_FILE_WRITTEN` o `REPORT_OUTPUT_FILE_WRITTEN`.


---
## Tabla de contenido

---


## Instalación


Incluye el módulo en la sección correspondiente.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:file-uploader-wakamiti-plugin:2.6.0
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>file-uploader-wakamiti-plugin</artifactId>
  <version>2.6.0</version>
</dependency>
```


## Configuración


### `fileUploader.enabled`
- Tipo: `boolean`
- Por defecto: `true`

Indica si el plugin está activado.

Ejemplo:
```yaml
fileUploader:
  enabled: "false"
```


### `fileUploader.host`
- Tipo: `string` *obligatorio*


El nombre o dirección IP de la máquina a la que se van a subir los ficheros. Opcionalmenbte,
puede incluir un número de puerto, en la forma `hostname:port`.

Ejemplo:
```yaml
fileUploader:
  host: 172.0.0.1:22
```


### `fileUploader.credentials.username`
- Tipo: `string` *obligatorio*

El nombre de usuario usado para establecer la conexión.

Ejemplo:
```yaml
fileUploader:
  credentials: 
    username: test
```


### `fileUploader.credentials.password`
- Tipo: `string` *obligatorio*

La contraseña usada para establecer la conexión.

Ejemplo:
```yaml
fileUploader:
  credentials:
    password: test
```

### `fileUploader.protocol`
- Tipo: `string` *obligatorio*
- Por defecto: `ftps`

El protocolo específico a usar. Posibles valores:
- `ftp`
- `ftps`
- `sftp`

Ejemplo:
```yaml
fileUploader:
  protocol: sftp
```


### `fileUploader.destinationDir`
- Tipo: `file` *obligatorio*

El directorio de destino al cual los ficheros deberían subirse, dentro de la ubicación remota. 
Puede incluir variables de ruta como `%DATE%`, `%TIME%`, or `%execID%`

Ejemplo:
```yaml
fileUploader:
  destinationDir: /home/test/file-%DATE%.txt
```


### `fileUploader.identity`
- Type: `file` 

Ruta del fichero de identidad usado para autenticarse.

Ejemplo:
```yaml
fileUploader:
  identity: /.ssh/identity.ppk
```


## Uso

Esta configuración global se aplica a todos los tipos de evento recibidos. Sin embargo, existe la posibilidad de ajustar 
una o más propiedades con valores específicos según el tipo de evento. Por ejemplo, la siguiente configuración usaría 
los mismos parámetros de conexión pero subiendo los ficheros a diferentes directorios según el tipo de evento:

```yaml copy=true
fileUploader:
  host: 192.168.1.40
  protocol: ftps
  credentials:
    username: test
    password: testpwd
  standardOutputs:
    destinationDir: data/results
  reportOutputs:
    destinationDir: data/reports
  testCaseOutputs:
    destinationDir: data/tests/%DATE%%TIME%
```