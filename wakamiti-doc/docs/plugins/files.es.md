---
title: Pasos de ficheros
date: 2022-09-20
slug: /plugins/files
---

Este plugin proporciona pasos para operar sobre ficheros y directorios locales durante la ejecución de pruebas.
Resulta útil en escenarios end-to-end donde el sistema crea, modifica, copia, mueve o elimina ficheros.

---
## Tabla de contenido

---

## Instalación

Incluye el módulo en la sección correspondiente.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:io-wakamiti-plugin:2.7.0
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>io-wakamiti-plugin</artifactId>
  <version>2.7.0</version>
</dependency>
```

## Configuración

### `files.timeout`
- Tipo: `long`
- Por defecto: `60`

Tiempo máximo de espera en segundos para los pasos de espera de ficheros.

Ejemplo:
```yaml
files:
  timeout: 120
```

### `files.enableCleanupUponCompletion`
- Tipo: `boolean`
- Por defecto: `false`

Si se habilita, las operaciones de ficheros registradas por el plugin se limpian automáticamente al finalizar.

Ejemplo:
```yaml
files:
  enableCleanupUponCompletion: true
```

### `files.links`
- Tipo: `string`

Define enlaces simbólicos creados durante la fase de inicialización.
Formato: `origen=destino`, separados por comas o punto y coma.

Ejemplo:
```yaml
files:
  links: "./tmp/entrada=./runtime/entrada; ./tmp/salida=./runtime/salida"
```

## Pasos

### Definir tiempo de espera
```text copy=true
un tiempo de espera de {value} segundos
```
Forma declarativa de establecer la propiedad de configuración [`files.timeout`](#filestimeout).

#### Parámetros:
| nombre  | tipo wakamiti        | descripción                     |
|---------|----------------------|---------------------------------|
| `value` | `long` *obligatorio* | Tiempo de espera en segundos    |

#### Ejemplos:
```gherkin
Dado un tiempo de espera de 120 segundos
```


### Mover fichero o directorio a un directorio
```text copy=true
(que) el (fichero|directorio) {src} se mueve al directorio {dest}
```
Mueve un fichero o directorio a un directorio de destino.

#### Parámetros:
| nombre | tipo wakamiti        | descripción                     |
|--------|----------------------|---------------------------------|
| `src`  | `file` *obligatorio* | Fichero o directorio origen     |
| `dest` | `file` *obligatorio* | Directorio destino              |

#### Ejemplos:
```gherkin
Cuando el fichero 'tmp/informe.txt' se mueve al directorio 'archivo'
```


### Mover fichero o directorio a un fichero
```text copy=true
(que) el (fichero|directorio) {src} se mueve al fichero {dest}
```
Mueve un fichero o directorio a una ruta de fichero específica.

#### Parámetros:
| nombre | tipo wakamiti        | descripción                     |
|--------|----------------------|---------------------------------|
| `src`  | `file` *obligatorio* | Fichero o directorio origen     |
| `dest` | `file` *obligatorio* | Ruta de fichero destino         |

#### Ejemplos:
```gherkin
Cuando el fichero 'tmp/informe.txt' se mueve al fichero 'archivo/informe-antiguo.txt'
```


### Copiar fichero o directorio a un directorio
```text copy=true
(que) el (fichero|directorio) {src} se copia al directorio {dest}
```
Copia un fichero o directorio a un directorio de destino.

#### Parámetros:
| nombre | tipo wakamiti        | descripción                     |
|--------|----------------------|---------------------------------|
| `src`  | `file` *obligatorio* | Fichero o directorio origen     |
| `dest` | `file` *obligatorio* | Directorio destino              |

#### Ejemplos:
```gherkin
Cuando el fichero 'plantilla.txt' se copia al directorio 'out'
```


### Copiar fichero o directorio a un fichero
```text copy=true
(que) el (fichero|directorio) {src} se copia al fichero {dest}
```
Copia un fichero o directorio a una ruta de fichero específica.

#### Parámetros:
| nombre | tipo wakamiti        | descripción                     |
|--------|----------------------|---------------------------------|
| `src`  | `file` *obligatorio* | Fichero o directorio origen     |
| `dest` | `file` *obligatorio* | Ruta de fichero destino         |

#### Ejemplos:
```gherkin
Cuando el fichero 'plantilla.txt' se copia al fichero 'out/generado.txt'
```


### Eliminar fichero o directorio
```text copy=true
(que) el (fichero|directorio) {file} se elimina
```
Elimina un fichero o directorio.

#### Parámetros:
| nombre | tipo wakamiti        | descripción                       |
|--------|----------------------|-----------------------------------|
| `file` | `file` *obligatorio* | Fichero o directorio a eliminar   |

#### Ejemplos:
```gherkin
Cuando el directorio 'tmp/data' se elimina
```


### Esperar eliminación
```text copy=true
(que) se espera a que el (fichero|directorio) {file} se elimine
```
Espera a que se elimine un fichero o directorio, hasta el tiempo máximo configurado.

#### Parámetros:
| nombre | tipo wakamiti        | descripción                        |
|--------|----------------------|------------------------------------|
| `file` | `file` *obligatorio* | Fichero o directorio a observar    |

#### Ejemplos:
```gherkin
Cuando se espera a que el fichero 'out/processing.lock' se elimine
```


### Esperar modificación
```text copy=true
(que) se espera a que el (fichero|directorio) {file} se modifique
```
Espera a que se modifique un fichero o directorio, hasta el tiempo máximo configurado.

#### Parámetros:
| nombre | tipo wakamiti        | descripción                        |
|--------|----------------------|------------------------------------|
| `file` | `file` *obligatorio* | Fichero o directorio a observar    |

#### Ejemplos:
```gherkin
Cuando se espera a que el fichero 'out/informe.txt' se modifique
```


### Esperar creación
```text copy=true
(que) se espera a que el (fichero|directorio) {file} se cree
```
Espera a que se cree un fichero o directorio, hasta el tiempo máximo configurado.

#### Parámetros:
| nombre | tipo wakamiti        | descripción                        |
|--------|----------------------|------------------------------------|
| `file` | `file` *obligatorio* | Fichero o directorio a observar    |

#### Ejemplos:
```gherkin
Cuando se espera a que el fichero 'out/informe.txt' se cree
```


### Comprobar que existe un fichero
```text copy=true
el fichero {file} existe
```
Verifica que el fichero existe.

#### Parámetros:
| nombre | tipo wakamiti        | descripción               |
|--------|----------------------|---------------------------|
| `file` | `file` *obligatorio* | Ruta del fichero a comprobar |

#### Ejemplos:
```gherkin
Entonces el fichero 'out/informe.txt' existe
```


### Comprobar que no existe un fichero
```text copy=true
el fichero {file} no existe
```
Verifica que el fichero no existe.

#### Parámetros:
| nombre | tipo wakamiti        | descripción               |
|--------|----------------------|---------------------------|
| `file` | `file` *obligatorio* | Ruta del fichero a comprobar |

#### Ejemplos:
```gherkin
Entonces el fichero 'tmp/informe-viejo.txt' no existe
```


### Comprobar contenido de texto de un fichero
```text copy=true
el fichero {file} contiene el siguiente texto:
    {data}
```
Verifica que el contenido completo del fichero coincide con el texto indicado en el propio escenario.

#### Parámetros:
| nombre | tipo wakamiti            | descripción                   |
|--------|--------------------------|-------------------------------|
| `file` | `file` *obligatorio*     | Ruta del fichero a comprobar  |
| `data` | `document` *obligatorio* | Texto completo esperado       |

#### Ejemplos:
```gherkin
Entonces el fichero 'out/informe.txt' contiene el siguiente texto:
  """
  Estado: OK
  """
```


### Comprobar contenido por rangos
```text copy=true
el fichero {file} contiene los siguientes datos:
    {table}
```
Verifica fragmentos del contenido del fichero por rangos de caracteres.
La tabla debe incluir las columnas: `from`, `to`, `value`.

#### Parámetros:
| nombre  | tipo wakamiti         | descripción                                       |
|---------|-----------------------|---------------------------------------------------|
| `file`  | `file` *obligatorio*  | Ruta del fichero a comprobar                      |
| `table` | `table` *obligatorio* | Rangos y valores esperados (`from`,`to`,`value`) |

#### Ejemplos:
```gherkin
Entonces el fichero 'out/informe.txt' contiene los siguientes datos:
  | from | to | value  |
  | 0    | 6  | Estado |
  | 8    | 10 | OK     |
```


### Comprobar longitud de un fichero
```text copy=true
el fichero {file} tiene una longitud de {chars}
```
Verifica que la longitud del fichero en bytes coincide con el valor indicado.

#### Parámetros:
| nombre  | tipo wakamiti           | descripción                            |
|---------|-------------------------|----------------------------------------|
| `file`  | `file` *obligatorio*    | Ruta del fichero a comprobar           |
| `chars` | `integer` *obligatorio* | Longitud esperada del fichero en bytes |

#### Ejemplos:
```gherkin
Entonces el fichero 'out/informe.txt' tiene una longitud de 128
```
