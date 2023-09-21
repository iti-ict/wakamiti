---
title: Pasos Groovy
date: 2023-01-20
slug: /plugins/groovy
---

Este plugin proporciona un compilador de clases `groovy` y pasos para la ejecución de código `groovy`.

```text tabs=coord name=yaml
es.iti.wakamiti:groovy-wakamiti-plugin:2.3.3
```

```xml tabs=coord name=maven
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>groovy-wakamiti-plugin</artifactId>
  <version>2.3.3</version>
</dependency>
```

---
## Tabla de contenido

---

---
## Compilador

El compilador groovy intentará compilar cualquier fichero con extensión `.groovy` presente en el directorio (o 
subdirectorio) de trabajo. Todas las librerías utilizadas en los ficheros `groovy` deben estar incluidas en el core de 
Wakamiti, o en la configuración [`wakamiti.launcher.modules`](wakamiti/architecture#wakamitilaunchermodules).

Estos ficheros `groovy` se podrán utilizar como proveedor de pasos adicionales.

#### Ejemplos:

Creamos un fichero llamado `custom-steps_es.properties` donde definimos un nuevo paso:
```properties
custom.step=se recupera la contraseña del usuario {name:text}
```

Creamos un fichero llamado `CustomSteps.goovy` donde desarrollamos el nuevo paso:
```groovy
package example

import es.iti.wakamiti.api.annotations.I18nResource
import es.iti.wakamiti.api.annotations.Step
import es.iti.wakamiti.api.util.WakamitiLogger
import imconfig.Configurable
import imconfig.Configuration
import org.slf4j.Logger

@I18nResource("custom-steps")
class CustomSteps implements StepContributor, Configurable {

  private static Logger log = WakamitiLogger.forName("es.iti.wakamiti.example");
  private String username
  private String password
  
  @Override
  void configure(Configuration configuration) {
    username = configuration.get("credentials.username", String.class).orElse(null)
    password = configuration.get("credentials.password", String.class).orElse(null)
  }

  @Step(value = "custom.step", args = ["name:text"])
  def customStep(String name) {
    if (name == username) {
      log.info("Hello, {}! Your password is {}", name, password)
      return password
    }
  }
}
```

Incluímos esta nueva clase en la configuración 
[`wakamiti.nonRegisteredStepProviders`](wakamiti/architecture#wakamitinonRegisteredStepProviders) de Wakamiti, y añadimos las 
propiedades con las credenciales:
```yml
  nonRegisteredStepProviders:
    - example.CustomSteps
  credentials:
    username: user
    password: s3cr3t
```

Creamos un fichero `example.feature` con el paso custom:
```gherkin
Característica: Ejemplo pasos custom
  Escenario: Prueba
    Cuando se recupera la contraseña del usuario 'user'
```

Al ejecutarse, se mostraría en el log:
```
[e.i.w.c.r.PlanNodeLogger.logStepResult]   INFO -  [ PASSED ]  Cuando se recupera la contraseña del usuario 'user' (0.011) 
[e.i.w.example.CustomSteps.customStep]   INFO - Hello, user! Your password is s3cr3t
```

---
## Pasos

### Ejecutar código
```
(que) se ejecuta el siguiente código groovy:
```
Ejecuta el script groovy indicado, incluyendo las siguentes variables:
- `ctx`: Contexto del escenario. Se trata de un contenedor con el `id` del escenario, los resultados de los diferentes 
  pasos, y cualquier otra variable que se añada.
- `log`: Logger de Wakamiti para depurar el script.

#### Parámetros:
| Nombre | Wakamiti type | Descripción          |
|--------|---------------|----------------------|
|        | `document`    | Contenido del script |

#### Ejemplos:
```gherkin
@ID-01
Escenario: Ejemplo
  Cuando se ejecuta el siguiente código groovy:
    """groovy
    ctx['a'] = 'something'
    1+1
    """
  Y se ejecuta el siguiente código groovy:
    """groovy
    log.debug("Context: {}", ctx)
    assert ctx.results[0] == 2
    assert ctx.a == 'something'
    assert ctx.id == 'ID-01'
    """
```

---
## Propiedades dinámicas

### Propiedad groovy
Obtener el resultado de la ejecución de una línea de código groovy, mediante la sintaxis `${=[expresión]}`, donde
`[expresión]` es el código groovy que se quiere ejecutar. En esta expresión también se incluye la variable `ctx`.

#### Ejemplos:
Tenemos el siguiente escenario:
```gherkin
@ID-01
Escenario: Ejemplo
  Cuando se ejecuta el script SQL del fichero 'data/${=ctx.id}/script-${=new Date().format("yyyyMMdd")}.sql'
```

Suponiendo que hoy es `20/09/2023`, al ejecutarse, se resolvería como:
```gherkin
@ID-01
Escenario: Ejemplo
  Cuando se ejecuta el script SQL del fichero 'data/ID-01/script-20230920.sql'
```
