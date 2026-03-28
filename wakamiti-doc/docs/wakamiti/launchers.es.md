---
title: Launchers
date: 2026-03-28
slug: /wakamiti/launchers
---


---
## Tabla de contenido

---


## JUnit launcher

Este launcher integra Wakamiti dentro de una ejecución JUnit. El plan de pruebas se construye a partir de la
configuración declarada en la clase y se notifica como eventos JUnit, lo que permite ejecutarlo desde IDE, surefire
o pipelines que ya consumen resultados JUnit.

Es la opción adecuada cuando Wakamiti forma parte de una suite Java existente y quieres mantener un único punto de
ejecución y reporte para todos los tests.

### Configuración mínima

```java
@RunWith(WakamitiJUnitRunner.class)
@AnnotatedConfiguration(path = "classpath:wakamiti.yaml", pathPrefix = "wakamiti")
public class WakamitiAcceptanceTest {
}
```

También puedes declarar propiedades inline:

```java
@RunWith(WakamitiJUnitRunner.class)
@AnnotatedConfiguration({
    @Property(key = "resourceTypes", value = "gherkin"),
    @Property(key = "resourcePath", value = "src/test/resources/features"),
    @Property(key = "junit.treatStepsAsTests", value = "true")
})
public class WakamitiAcceptanceTest {
}
```


### `wakamiti.junit.treatStepsAsTests`
- Tipo: `boolean`
- Por defecto: `false`

Cuando se activa, cada paso se notifica a JUnit como test individual.

Ejemplo:
```yaml
wakamiti:
  junit:
    treatStepsAsTests: true
```


### Perfiles de ejecución en JUnit

Anotación:
```java
@Profile("smoke")
@RunWith(WakamitiJUnitRunner.class)
public class WakamitiSmokeTest {
}
```

Propiedad JVM de activación:

- `wakamiti.junit.profile`

Modo estricto:

- `wakamiti.junit.profile.strict`

Comportamiento del modo estricto:

| `strict` | Perfil activo | Clase con `@Profile` | Clase sin `@Profile` |
|---|---|---|---|
| `false` | no | se ejecuta | se ejecuta |
| `false` | sí | solo si coincide | se ejecuta |
| `true` | no | no se ejecuta | se ejecuta |
| `true` | sí | solo si coincide | no se ejecuta |

Ejemplo:
```shell copy=true
mvn test -Dwakamiti.junit.profile=smoke -Dwakamiti.junit.profile.strict=true
```


## Maven launcher

Este launcher ejecuta Wakamiti como parte del ciclo de vida de Maven. La configuración se controla desde el `pom.xml`
y puede combinarse con ficheros externos, propiedades y classpath del proyecto.

Es la opción adecuada cuando quieres gobernar la ejecución desde fases de build (`integration-test` / `verify`) y
alinear el comportamiento de Wakamiti con perfiles, objetivos y políticas de fallo de Maven.

### `configurationFiles`
- Tipo: `string[]`
- Por defecto: `[]`

Lista de ficheros de configuración (`wakamiti` root) que se combinan en orden.


### `properties`
- Tipo: `map<string,string>`
- Por defecto: `{ }`

Permite sobrescribir propiedades de Wakamiti desde el `pom.xml`.


### `includeProjectDependencies`
- Tipo: `boolean`
- Por defecto: `false`

Si está activo, añade el classpath runtime del proyecto a la ejecución.


### `skipTests`
- Tipo: `boolean`
- Por defecto: `false`

Salta la ejecución del plan Wakamiti.


### `testFailureIgnore`
- Tipo: `boolean`
- Por defecto: `false`

Si está activo, no interrumpe el build al fallar pruebas.


### `logLevel`
- Tipo: `string`
- Por defecto: `info`

Nivel de log usado por el launcher Maven.


### Ejemplo de configuración

```xml
<plugin>
  <configuration>
    <configurationFiles>
      <configurationFile>wakamiti.yaml</configurationFile>
    </configurationFiles>
    <properties>
      <tagFilter>@smoke and not @ignore</tagFilter>
    </properties>
    <includeProjectDependencies>true</includeProjectDependencies>
    <skipTests>false</skipTests>
    <testFailureIgnore>false</testFailureIgnore>
    <logLevel>info</logLevel>
  </configuration>
  <executions>
    <execution>
      <goals>
        <goal>verify</goal>
        <goal>report</goal>
        <goal>control</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

Ejecución:
```shell copy=true
mvn verify
```


## Wakamiti CLI

Este launcher ejecuta Wakamiti por línea de comandos, sin depender de JUnit ni de la configuración de un plugin de
build. Carga configuración, resuelve módulos y ejecuta el plan de forma autónoma.

Es la opción adecuada para ejecuciones locales rápidas, automatizaciones ligeras y entornos donde solo se dispone de
ficheros de configuración y acceso a repositorios de dependencias.

Sintaxis general:
```shell copy=true
wakamiti [opciones]
```


### `wakamiti.launcher.modules`
- Tipo: `string[]`

Módulos Maven con formato `<groupId>:<artifactId>:<version>`.

Ejemplo:
```yaml
wakamiti:
  launcher:
    modules:
      - com.example:mi-plugin-funcional:1.0.0
      - com.example:mi-plugin-reporte:1.0.0
```


### `mavenFetcher.remoteRepositories`
- Tipo: `URL[]` (separadas por `;`)

Ejemplo:
```yaml
mavenFetcher:
  remoteRepositories: https://repo.maven.apache.org/maven2;file:///C:/Users/usuario/.m2/repository
```


### `mavenFetcher.localRepository`
- Tipo: `file`

Repositorio local donde se cachean artefactos descargados por el launcher.


### `-h`, `--help`
- Tipo: `boolean` (flag)

Muestra ayuda y termina sin ejecutar pruebas.

Ejemplo:
```shell copy=true
wakamiti -h
```


### `-d`, `--debug`
- Tipo: `boolean` (flag)

Activa trazas de depuración. Si no se define [`wakamiti.log.level`](/wakamiti/architecture#wakamitiloglevel), se usa `debug`.

Ejemplo:
```shell copy=true
wakamiti -d
```


### `-c`, `--clean`
- Tipo: `boolean` (flag)

Limpia la caché local antes de descargar módulos.

Ejemplo:
```shell copy=true
wakamiti -c
```


### `-f`, `--file`
- Tipo: `file`
- Por defecto: `wakamiti.yaml`

Indica el fichero de configuración a usar.

Ejemplo:
```shell copy=true
wakamiti -f wakamiti.ci.yaml
```


### `-m`, `--modules`
- Tipo: `string[]` (separados por coma)

Añade módulos por CLI y los concatena con `wakamiti.launcher.modules`.

Ejemplo:
```shell copy=true
wakamiti -m com.example:mi-plugin-funcional:1.0.0,com.example:mi-plugin-reporte:1.0.0
```


### `-n`, `--dry-run`
- Tipo: `boolean` (flag)

Fuerza ejecución en seco. Corresponde a [`wakamiti.dryRun`](/wakamiti/architecture#wakamitidryrun).

Ejemplo:
```shell copy=true
wakamiti -n
```


### `-K clave=valor`
- Tipo: `key=value` (repetible)

Sobrescribe propiedades `wakamiti.*` desde CLI.

Ejemplo:
```shell copy=true
wakamiti -K tagFilter="@smoke and not @ignore" -K outputFilePath=results/wakamiti.json
```


### `-M clave=valor`
- Tipo: `key=value` (repetible)

Sobrescribe propiedades `mavenFetcher.*` desde CLI.

Ejemplo:
```shell copy=true
wakamiti -M remoteRepositories="https://repo.maven.apache.org/maven2"
```


### `-l`, `--list`
- Tipo: `boolean` (flag)

Muestra las contribuciones disponibles cargadas para la ejecución.

Ejemplo:
```shell copy=true
wakamiti -l
```


### Ejemplo CLI completo

```shell copy=true
wakamiti -f wakamiti.ci.yaml -m com.example:mi-plugin-funcional:1.0.0 -K tagFilter="@smoke" -M remoteRepositories="https://repo.maven.apache.org/maven2"
```


### Uso con Docker

Windows:
```shell copy=true
docker run --rm -v "%cd%:/wakamiti" wakamiti/wakamiti
```

Linux:
```shell copy=true
docker run --rm -v "$(pwd):/wakamiti" --add-host=host.docker.internal:host-gateway wakamiti/wakamiti
```
