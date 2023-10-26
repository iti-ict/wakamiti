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
- El escenario debe tener definidas las siguientes propiedades:
    - `azurePlan` : nombre del plan de test en Azure
    - `azureArea` : nombre del área al que pertenece el plan
    - `azureIteration` : ruta de la iteración del plan, separada por `\\`
    - `azureTest` : nombre del caso de test (si no se indica, se tomará el nombre de la carácterística/escenario de Wakamiti)
    - `azureTestId` : identificador del elemento de trabajo correspondiente al caso de test en Azure

Los casos de test que no tengan esto definido se ignorarán a la hora de hacer la integración.

En caso de que la ejecución de Wakamiti incluya casos de tests de varios planes de Azure distintos,
se creará una ejecución Azure distinta por cada uno de ellos.


Ejemplos:

```gherkin
@Azure
# azurePlan: MyPlan
# azureArea: AAA
# azureIteration: AAA\\Iteration 1
# azureSuite: MySuite
# azureTest: MyTest
Característica: Pruebas de alta de usuario

Escenario: Alta de usuario inexiste
...

Escenario: Alta de usuario existente
...
```

```gherkin
@Azure
# azurePlan: MyPlan
# azureArea: AAA
# azureIteration: AAA\\Iteration 1
# azureSuite: MySuite
Característica: Pruebas de alta de usuario

# azureTest: MyFirstTest
# azureTestId: 543543  
Escenario: Alta de usuario inexiste
...

# azureTest: MySecondTest
Escenario: Alta de usuario existente
...
```

Los casos de test que no tengan esto definido se ignorarán a la hora de hacer la integración.

En caso de que la ejecución de Wakamiti incluya casos de tests de varios planes de Azure distintos,
se creará una ejecución Azure distinta por cada uno de ellos.



---
## Tabla de contenido

---



Coordenadas
----------------------------------------------------------------------------------------------------

### Fichero de configuración Wakamiti

```yaml
wakamiti:
    launcher:
        modules:
            - es.iti.wakamiti:azure-wakamiti-plugin:1.5.0
```

### Maven

```
  <dependency>
    <groupId>es.iti.wakamiti</groupId>
    <artifactId>azure-wakamiti-plugin</artifactId>
    <version>1.5.0</version>
  </dependency>
```


## Configuración



---
####  `azure.host`
La dirección de la máquina donde está alojado el servidor Azure.

Ejemplo:

```yaml
azure:
  host: azure.mycompany.org
  
```

---
####  `azure.credentials.user`
El nombre de usuario que se usará en la API REST de Azure, como parte de la autorización básica HTTP.
Si se usa autenticación por token, no se requiere esta propiedad.

Ejemplo:

```yaml
azure:
  credentials:
    user: myuser

```


---
####  `azure.credentials.password`
El password o token que se usará en la API REST de Azure, como parte de la autorización básica HTTP.


Ejemplo:

```yaml
azure:
  credentials:
    password: xKHJFHLKJ7897
  
```


---
####  `azure.apiVersion`
El número de versión de la API REST de Azure que se va a usar para enviar las notificaciones.

El valor por defecto es `6.0-preview`. Usar una versión anterior puede provocar que ciertas funcionalidades,
como la creación de nuevos casos de test, no funcionen correctamente.

Ejemplo:

```yaml
azure:
  apiVersion: '6.0-preview'
  
```


---
####  `azure.organization`
El nombre de la organización en Azure al que pertenece el plan de test.

Ejemplo:

```yaml
azure:
  organization: MyOrganization
  
```


---
####  `azure.project`
El nombre del proyecto Azure al que pertenece el plan de test.

Ejemplo:

```yaml
azure:
  project: MyProject
  
```


---
####  `azure.tag`
La etiqueta que se buscará a la hora de determinar si se debe realizar o no la integración
con Azure.

El valor por defecto es `Azure`.

Ejemplo:

```yaml
azure:
  tag: AzureExecution
  
```



---
####  `azure.attachments`
Una lista de ficheros, o de patrones de nombre de fichero en formato _glob_, que
se adjuntarán a la ejecucón Azure.


Ejemplo:

```yaml
azure:
  attachments:
    - 'wakamiti.html'
    - '*.json'  
```



---
####  `azure.createItemsIfAbsent`
Establece si se debe crear automáticamente los elementos (planes, suites y casos de test) que no
existan en Azure.

El valor por defecto es `false`.

Ejemplo:

```yaml
azure:
  createItemsIfAbsent: true
```



---
####  `azure.workItemTestCaseType`
La nomenclatura que usa la instancia de Azure para referirse a los elementos de trabajo clasificados
como casos de prueba. Requerido si se habilita la creación de nuevos casos de test.


Ejemplo:

```yaml
azure:
  workItemTestCaseType: "Caso de prueba"
```



---
####  `azure.timeZoneAdjustment`
Aplica un ajuste horario a la hora de notificar los instantes de inicio y fin de ejecución, en caso
de que la instancia de Azure funcione con una zona horaria distinta.


El valor por defecto es `0`.

Ejemplo:

```yaml
azure:
  timezoneAdjustment: -2
```


