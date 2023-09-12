---
title: Integración Azure
date: 2023-07-04
slug: /plugins/azure
---

Este plugin integra los resultados de la ejecución de Wakamiti con un plan de test 
de [Azure](https://azure.microsoft.com/) existente, al tiempo que permite adjuntar 
ficheros (como el generado por el plugin HTML Reporter) en la ejecución.


Para que el plugin envíe los resultados, se deben cumplir dos condiciones:

- El escenario debe estar etiquetado con una etiqueta específica (por defecto `@Azure`)
- El escenario debe tener definidas las propiedades `azurePlan`, `azureSuite` y `azureTest`,
con los nombres del plan, de la suite y del caso de test tal cual estén definidos en Azure.

Ejemplos:

```gherkin
@Azure
# azurePlan: MyPlan
# azureSuite: MySuite
# azureTest: MyTest
Característica: Pruebas de alta de usuario

Escenario: Alta de usuario inexistente
...

Escenario: Alta de usuario existente
...
```

```gherkin
@Azure
# azurePlan: MyPlan
# azureSuite: MySuite
Característica: Pruebas de alta de usuario

# azureTest: MyFirstTest
Escenario: Alta de usuario inexistente
...

# azureTest: MySecondTest
Escenario: Alta de usuario existente
...
```

Los casos de test que no tengan esto definido se ignorarán a la hora de hacer la integración.

En caso de que la ejecución de Wakamiti incluya casos de tests de varios planes de Azure distintos,
se creará una ejecución Azure distinta por cada uno de ellos.

---
## toc

---
## Coordenadas


### Fichero de configuración Wakamiti

```yaml
wakamiti:
    launcher:
        modules:
            - es.iti.wakamiti:azure-wakamiti-plugin:1.2.1
```

### Maven

```
  <dependency>
    <groupId>es.iti.wakamiti</groupId>
    <artifactId>azure-wakamiti-plugin</artifactId>
    <version>1.2.1</version>
  </dependency>
```


---
## Configuración


####  `azure.host`
La dirección de la máquina donde está alojado el servidor Azure.

Ejemplo:

```yaml
azure:
  host: azure.mycompany.org
  
```

<br /><br />

####  `azure.credentials.user`
El nombre de usuario que se usará en la API REST de Azure, como parte de la autorización básica HTTP.

Ejemplo:

```yaml
azure:
  credentials:
    user: myuser

```

<br /><br />

####  `azure.credentials.password`
El password que se usará en la API REST de Azure, como parte de la autorización básica HTTP.

Ejemplo:

```yaml
azure:
  credentials:
    password: xKHJFHLKJ7897
  
```

<br /><br />

####  `azure.apiVersion`
El número de versión de la API REST de Azure que se va a usar para enviar las notificaciones.

El valor por defecto es `5.0-preview`.

Ejemplo:

```yaml
azure:
  apiVersion: '6.0-preview'
  
```

<br /><br />

####  `azure.organization`
El nombre de la organización en Azure al que pertenece el plan de test.

Ejemplo:

```yaml
azure:
  organization: MyOrganization
  
```

<br /><br />

####  `azure.project`
El nombre del proyecto Azure al que pertenece el plan de test.

Ejemplo:

```yaml
azure:
  project: MyProject
  
```

<br /><br />

####  `azure.tag`
La etiqueta que se buscará a la hora de determinar si se debe realizar o no la integración
con Azure.

El valor por defecto es `Azure`.

Ejemplo:

```yaml
azure:
  tag: AzureExecution
  
```

<br /><br />

####  `azure.attachments`
Una lista de ficheros, o de patrones de nombre de fichero en formato _glob_, que 
se adjuntarán a la ejecución Azure.


Ejemplo:

```yaml
azure:
  attachments:
    - 'wakamiti.html'
    - '*.json'  
```

<br /><br />

#### `azure.testCasePerFeature`
Establece si el mapeo con los casos de test de Azure debe ser a nivel de feature o de escenario.

Ejemplo:
```yaml
azure:
  testCasePerFeature: true
```
