---
title: Integración Azure
date: 2023-07-04
slug: /plugins/azure
---


Este plugin integra los resultados de la ejecución de Wakamiti con un plan de test de 
[Azure](https://azure.microsoft.com/) existente, al tiempo que permite adjuntar ficheros (como el generado por el plugin 
HTML Reporter) en la ejecución.

---
## Tabla de contenido

---


## Instalación


Incluye el módulo en la sección correspondiente.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:azure-wakamiti-plugin:3.0.2
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>azure-wakamiti-plugin</artifactId>
  <version>3.0.2</version>
</dependency>
```


## Configuración


###  `azure.enabled`
- Tipo: `boolean` 
- Por defecto: `true`

Activa/desactiva por completo la funcionalidad de este plugin.

Example:
```yaml
azure:
  enabled: false
  
```


###  `azure.baseURL`
- Tipo: `url` *obligatorio*

Establece la URL base de la máquina donde está alojado el servidor Azure.

Ejemplo:
```yaml
azure:
  baseURL: https://azure.mycompany.org
```


###  `azure.auth.username`
- Tipo: `string` 

El nombre de usuario que se usará en la API REST de Azure, como parte de la autorización básica HTTP.
Si se usa autenticación por token, no se requiere esta propiedad.

Ejemplo:
```yaml
azure:
  auth:
    username: myuser
```


###  `azure.auth.password`
- Tipo: `string` 

El password de usuario que se usará en la API REST de Azure, como parte de la autorización básica HTTP.
Si se usa autenticación por token, no se requiere esta propiedad.

Ejemplo:
```yaml
azure:
  auth:
    password: xKHJFHLKJ7897
```


### `azure.auth.token`
- Tipo: `string`

El token que se usará en la API REST de Azure, como parte de la autorización básica HTTP.

Ejemplo:
```yaml
azure:
  auth:
    token: s3cr3t
```

###  `azure.apiVersion`
- Tipo: `string` 
- Por defecto: `6.0-preview`

El número de versión de la API REST de Azure que se va a usar para enviar las notificaciones.

> Usar una versión anterior puede provocar que ciertas funcionalidades, como la creación de nuevos casos de test, no 
> funcionen correctamente.

Ejemplo:
```yaml
azure:
  apiVersion: '6.0-preview'
```


###  `azure.organization`
- Tipo: `string` *obligatorio*

El nombre de la organización en Azure al que pertenece el plan de test.

Ejemplo:
```yaml
azure:
  organization: MyOrganization
```


###  `azure.project`
- Tipo: `string` *obligatorio*

El nombre del proyecto Azure al que pertenece el plan de test.

Ejemplo:
```yaml
azure:
  project: MyProject
```


### `azure.plan.name`
- Tipo: `string` *obligatorio*

Nombre del plan de test en Azure.

Ejemplo:
```yaml
azure:
  plan: 
    name: Wakamiti Test Plan
```


### `azure.plan.area`
- Tipo: `path` *obligatorio*

Ruta del área al que pertenece el plan.

Ejemplo:
```yaml
azure:
  plan: 
    area: ABC/DE
```


### `azure.plan.iteration`
- Tipo: `path` *obligatorio*

Ruta de la iteración del plan.

Ejemplo:
```yaml
azure:
  plan: 
    area: ABC/Iteration 1
```


### `azure.suiteBase`
- Tipo: `path`

Las suites en azure se establecen en base a la ruta donde se encuentran los features. Con esta propiedad se establece 
la ruta desde la cual se tendrán en cuenta las rutas para crear las suites.

También está la opción de indicar manualmente la suite del test [desde el feature](#azuresuite).

Ejemplo:
```yaml
azure:
  suiteBase: features
```


###  `azure.attachments`
- Tipo: `glob[]` 

Patrones glob de los reports que se deseen adjuntar.

Ejemplo:
```yaml
azure:
  attachments: 
    - '**/*.html'
    - '**/wakamiti.json'
```


### `azure.testCasePerFeature`
- Tipo: `boolean`
- Por defecto: `false`

Establece si el mapeo con los casos de test de Azure debe ser a nivel de feature o de escenario.

Ejemplo:
```yaml
azure:
  testCasePerFeature: true
```


###  `azure.createItemsIfAbsent`
- Tipo: `boolean`
- Por defecto: `true`

Establece si se debe crear automáticamente los elementos (planes, suites y casos de test) que no existan en Azure.

Ejemplo:
```yaml
azure:
  createItemsIfAbsent: true
```


## Uso

La sincronización con azure se realizará antes de la ejecución de los tests. En caso de que haya algún problema, se 
detendrá la ejecución.
Al finalizar la ejecución, se procederá a sincronizar los resultados con azure.


### `azureSuite`
- Tipo: `path`

Establece la suite en la que se encontrará el test en azure.

Ejemplo:
```gherkin
# language: es
# azureSuite: My Suite/Subsuite a
Característica: Pruebas de alta de usuario

Escenario: Alta de usuario inexiste
...

Escenario: Alta de usuario existente
...
```
