---
title: Appium
date: 2023-06-09
slug: /plugins/appium
---

Este plugin permite usar Wakamiti para escribir escenarios que interactuen con un servidor 
[Appium](http://appium.io/docs/en/2.0/). Appium es un proyecto open-source diseñado para 
facilitar la automatización de tests UI en varias plataformas, incluyendo aplicaciones móviles.

El uso de este plugin requiere un servidor Appium en marcha, al igual que un dispositivo
virtual emulado. 

> **AVISO**
> 
> En su estado actual, este plugin es más una prueba de concepto que un plugin plenamente funcional. Los pasos y la 
> configuración indicada pueden variar en futuras versiones.


---
## Tabla de contenido

---


## Instalación


Incluye el módulo en la sección correspondiente.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:appium-wakamiti-plugin:2.5.0
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>appium-wakamiti-plugin</artifactId>
  <version>2.5.0</version>
</dependency>
```


## Configuración


> Se puede especificar cualquier capacidad a pasar al Web Driver usando la clave adecuada con el formato 
> `appium.capabilities.xxxxxx`. En este document se describen las opciones mas relevantes, pero para una lista 
> exhaustiva de las propiedades disponibles, consultar el 
> [borrador W3C WebDriver](https://w3c.github.io/webdriver/#capabilities).


### `appium.url` 
- Tipo: `string` *obligatorio*

La URL del servidor Appium.

Ejemplo:
```yaml
appium:
  url: http://127.0.0.1:4723/wd/hub
```


### `appium.capabilities.app`
- Tipo: `file` *obligatorio*

La ruta completa de la aplicación a testear.

Ejemplo:
```yaml
appium:
  capabilities:
    app: ApiDemos-debug.apk
```


### `appium.capabilities.platformName`
- Tipo: `string` *obligatorio*

El tipo de plataforma móvil que se va a testear.

Ejemplo:
```yaml
appium:
  capabilities:
    platformName: Android
```


### `appium.capabilities.platformVersion`
- Tipo: `integer` *obligatorio*

La versión de la plataforma que se va a testear.

Ejemplo:
```yaml
appium:
  capabilities:
    platformVersion: 11
```


### `appium.capabilities.appPackage`
- Tipo: `string` *obligatorio*

El nombre del paquete que contiene la aplicación que se va a testear.

Ejemplo:
```yaml
appium:
  capabilities:
    appPackage: io.appium.android.apis
```


### `appium.capabilities.appActivity`
- Tipo: `string` *obligatorio*

El nombre de la actividad a testear.

Ejemplo:
```yaml
appium:
  capabilities:
    appActivity: '.view.TextFields'
```


## Pasos


Este plugin proporciona los siguientes pasos:


### Seleccionar un elemento de interfaz a partir de su ID
```text copy=true
el elemento de interfaz con el ID {text}
```
Selecciona un elmento que será el sujeto de los siguientes pasos.

#### Parámetros:
| Nombre | Wakamiti type     | Descripción     |
|--------|-------------------|-----------------|
| `text` | `text` *required* | ID del elemento |

#### Ejemplos:
```gherkin
Dado el elemento de interfaz con el ID '3423423'
```


### Seleccionar un elemento de interfaz a partir de su tipo
```text copy=true
el elemento de interfaz de tipo {text}
```
Selecciona un elmento que será el sujeto de los siguientes pasos.

#### Parámetros:
| Nombre | Wakamiti type     | Descripción       |
|--------|-------------------|-------------------|
| `text` | `text` *required* | Tipo del elemento |

#### Ejemplos:
```gherkin
Dado el elemento de interfaz de tipo 'android.widget.EditText'
```


### Seleccionar un elemento de interfaz a partir de su ruta
```text copy=true
el elemento de interfaz con ruta {text}
```
Selecciona un elmento que será el sujeto de los siguientes pasos.

#### Parámetros:
| Nombre | Wakamiti type     | Descripción       |
|--------|-------------------|-------------------|
| `text` | `text` *required* | Ruta del elemento |

#### Ejemplos:
```gherkin
Dado el elemento de interfaz con ruta  'main.form.name'
```


### Teclear un texto en un elemento
```text copy=true
se escribe el texto {text} en ese elemento
```
Emula la acción de introducir un texto cuando un elemento se ha seleccionado.

#### Parámetros:
| Nombre | Wakamiti type     | Descripción            |
|--------|-------------------|------------------------|
| `text` | `text` *required* | Contenido del elemento |

#### Ejemplos:
```gherkin
Cuando se escribe el texto 'John' en ese elemento
```


### Se pulsa sobre un elemento
```text copy=true
se pulsa sobre ese elemento
```
Emula la acción de pulsar sobre un elemento.

#### Ejemplos:
```gherkin
Cuando se pulsa sobre ese elemento
```


### Se pulsa dos veces sobre un elemento
```text copy=true
se pulsa dos veces ese elmento
```
Emula la acción de pulsar dos veces sobre un elemento.

#### Ejemplos:
```gherkin
Cuando se pulsa dos veces sobre ese elemento
```


### Valida el texto de un elemento
```text copy=true
ese elemento contiene el valor {text}
```
Comprueba que el elemento previamente seleccionado contiene un texto específico.

#### Ejemplos:
```gherkin
Entonces ese elemento contiene el valor 'Accepted'
```


### Valida que un elemento está habilitado
```text copy=true
ese elemento esta habilitado
```
Comprueba que el elemento previamente seleccionado está habilitado.

#### Ejemplos:
```gherkin
Entonces ese elemento esta habilitado
```


### Valida que un elemento está deshabilitado
```text copy=true
ese elemento esta deshabilitado
```
Comprueba que el elemento previamente seleccionado está deshabilitado.

#### Ejemplos:
```gherkin
Entonces ese elemento esta deshabilitado
```


### Validar que se muestra un elemento
```text copy=true
ese elemento se muestra por pantalla
```
Comprueba que el elemento previamente seleccionado está siendo mostrado por pantalla.

#### Ejemplos:
```gherkin
Entonces ese elemento se muestra por pantalla
```


### Validar que no se muestra un elemento
```text copy=true
ese elemento no se muestra por pantalla
```
Comprueba que el elemento previamente seleccionado no está siendo mostrado por pantalla.

#### Ejemplos:
```gherkin
Entonces ese elemento no se muestra por pantalla
```


### Emular una llamada entrante
```text copy=true
se recibe una llamada entrante con el numero {text}
```
Emula una llamada entrante de un número de teléfono específico. Solo disponible si la aplicación a testear está siendo 
ejecutada en un dispositivo emulado.

#### Parámetros:
| Nombre | Wakamiti type     | Descripción        |
|--------|-------------------|--------------------|
| `text` | `text` *required* | Número de teléfono |

#### Ejemplos:
```gherkin
Cuando se recibe una llamada entrante con el numero '555-4324-432'
```


### Aceptar una llamada entrante
```text copy=true
se acepta la llamada entrante
```
Acepta la llamada que está entrando en ese momento.

#### Ejemplos:
```gherkin
Cuando se acepta la llamada entrante
```


### Rechazar una llamada entrante
```text copy=true
se rechaza la llamada entrante
```
Rechaza la llamada que está entrando en ese momento.

#### Ejemplos:
```gherkin
Cuando se rechaza la llamada entrante
```


### Finaliza la llamada actual
```text copy=true
se finaliza la llamada
```
Finaliza (cuelga) la llamada actual.

#### Ejemplos:
```gherkin
  Cuando se finaliza la llamada
```


## Uso


1. Instalar Android SDK
```text copy=true
  sudo apt install android-sdk
```


2. Agregar la variable de entorno ANDROID_HOME
```
  (p.ej. $HOME/Android/Sdk )
```

3. Instalar cmdline-tools

![android-sdk][android-sdk]

4. Instalar Appium y Appium-doctor
```text copy=true
   npm install -g appium
   npm install @appium/doctor --location=global
```

5. Comprobar la instalación
```text copy=true
appium-doctor --android
```
Todo debería salir como OK, en caso contrario revisar los checks en rojo.

6. Arrancar el servidor Appium
```text copy=true
appium
```
El puerto por defecto es 4723

Descargar APK de prueba en:
https://github.com/appium/appium/raw/1.x/sample-code/apps/ApiDemos-debug.apk

7. Crear un dipositivo Virtual
```text copy=true
$ANDROID_HOME/cmdline-tools/latest/bin/avdmanager create avd --force --name Nexus6P --abi google_apis_playstore/x86 --package 'system-images;android-30;google_apis_playstore;x86' --device "Nexus 6P"
```

8. Iniciar el emulador Android
```text copy=true
$ANDROID_HOME/emulator/emulator -avd Nexus6P
```


Si en algún momento el emulador se queda congelado y dice que ya existe una emulación en curso, se puede limpiar el
estado con
```text copy=true
$ANDROID_HOME/platform-tools/adb kill-server
```

Lo ideal sería poder lanzar todo esto de manera semiautomática únicamente
a partir del APK, pero de momento hay que:
- Instalar Android Studio
- Crear dispositivo virtual (AVD) Por ejemplo, Pixel 2 API 30 con Android 11
- Arrancar el AVD con el botón de play



[android-sdk]: https://iti-ict.github.io/wakamiti/android-sdk.png
