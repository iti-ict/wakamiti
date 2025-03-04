---
title: Integración XRay
date: 2025-03-04
slug: /plugins/xray
---


Este plugin integra los resultados de la ejecución de Wakamiti con un plan de test de 
[Jira](https://www.atlassian.com/es/software/jira) existente mediante el plugin de [XRay](https://www.getxray.app/), al tiempo que permite adjuntar ficheros (como el generado por el plugin 
HTML Reporter) en la ejecución.

---
## Tabla de contenido

---


## Instalación


Incluye el módulo en la sección correspondiente.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:xray-wakamiti-plugin:2.5.2
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>xray-wakamiti-plugin</artifactId>
  <version>2.5.2</version>
</dependency>
```


## Configuración

### XRAY

###  `xray.enabled`
- Tipo: `boolean` 
- Por defecto: `true`

Activa/desactiva por completo la funcionalidad de este plugin.

Example:
```yaml
xray:
  enabled: false
  
```


###  `xray.baseURL`
- Tipo: `url` *obligatorio*

Establece la URL base de la máquina donde está alojado el servidor XRay.

Ejemplo:
```yaml
xray:
  baseURL: https://eu.xray.cloud.getxray.app
```


###  `xray.auth.credentials.client-id`
- Tipo: `string` *obligatorio*

El Client Id que se usará en la API REST de XRay, como parte de la autorización HTTP.

Ejemplo:
```yaml
xray:
  auth:
    credentials:
      client-id: MY-CLIENT-ID
```


###  `xray.auth.credentials.client-secret`
- Tipo: `string` *obligatorio*

El Client Secret de usuario que se usará en la API REST de XRay, como parte de la autorización HTTP.

Ejemplo:
```yaml
xray:
  auth:
    credentials:
      client-secret: MY-CLIENT-SECRET
```


###  `xray.project`
- Tipo: `string` *obligatorio*

El nombre del proyecto Jira al que pertenece el plan de test.

Ejemplo:
```yaml
xray:
  project: MyProject
```


### `xray.plan.summary`
- Tipo: `string` *obligatorio*

Nombre del plan de test en XRay.

Ejemplo:
```yaml
xray:
  plan:
    summary: Wakamiti Test Plan
```


###  `xray.attachments`
- Tipo: `glob[]` 

Patrones glob de los reports que se deseen adjuntar.

Ejemplo:
```yaml
xray:
  attachments: 
    - '**/*.html'
    - '**/wakamiti.json'
```


### `xray.testCasePerFeature`
- Tipo: `boolean`
- Por defecto: `false`

Establece si el mapeo con los casos de test de XRay debe ser a nivel de feature o de escenario.

Ejemplo:
```yaml
xray:
  testCasePerFeature: true
```


###  `xray.createItemsIfAbsent`
- Tipo: `boolean`
- Por defecto: `true`

Establece si se debe crear automáticamente los elementos que no existan en XRay.

Ejemplo:
```yaml
xray:
  createItemsIfAbsent: true
```

### JIRA

###  `jira.baseURL`
- Tipo: `url` *obligatorio*

Establece la URL base de la máquina donde está alojado el servidor Jira.

Ejemplo:
```yaml
jira:
  baseURL: https://my-jira.atlassian.net
```


###  `jira.auth.credentials`
- Tipo: `string` *obligatorio*

Los credenciales que se usarán en la API REST de Jira, como parte de la autorización HTTP. Se trata del `usuario:token` codificado en base64.

Ejemplo:
```yaml
jira:
  auth:
    credentials: MY-CREDENTIALS
```


## Uso

La sincronización con XRay se realizará antes de la ejecución de los tests. En caso de que haya algún problema, se 
detendrá la ejecución.
Al finalizar la ejecución, se procederá a sincronizar los resultados con XRay.

