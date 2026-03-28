---
title: Launchers
date: 2026-03-28
slug: /en/wakamiti/launchers
---


---
## Table of contents

---


## JUnit launcher

This launcher integrates Wakamiti into a JUnit execution flow. The test plan is built from class-level configuration
and reported as JUnit events, so it can run from IDEs, surefire, or pipelines already consuming JUnit results.

This is the right option when Wakamiti must be part of an existing Java test suite and you want a single execution and
reporting entry point for all tests.

### Minimal configuration

```java
@RunWith(WakamitiJUnitRunner.class)
@AnnotatedConfiguration(path = "classpath:wakamiti.yaml", pathPrefix = "wakamiti")
public class WakamitiAcceptanceTest {
}
```

Inline properties are also supported:

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
- Type: `boolean`
- Default: `false`

When enabled, each step is reported to JUnit as an individual test.

Example:
```yaml
wakamiti:
  junit:
    treatStepsAsTests: true
```


### JUnit profile-based execution

Annotation:
```java
@Profile("smoke")
@RunWith(WakamitiJUnitRunner.class)
public class WakamitiSmokeTest {
}
```

JVM Activation properties:

- `wakamiti.junit.profile`

Strict mode:

- `wakamiti.junit.profile.strict`

Strict mode behavior:

| `strict` | Active profile | Class with `@Profile` | Class without `@Profile` |
|---|---|---|---|
| `false` | no | runs | runs |
| `false` | yes | only if matches | runs |
| `true` | no | does not run | runs |
| `true` | yes | only if matches | does not run |

Example:
```shell copy=true
mvn test -Dwakamiti.junit.profile=smoke -Dwakamiti.junit.profile.strict=true
```


## Maven launcher

This launcher runs Wakamiti as part of the Maven lifecycle. Configuration is controlled from `pom.xml` and can be
combined with external files, inline properties, and project classpath.

This is the right option when you need build-phase control (`integration-test` / `verify`) and want Wakamiti behavior
aligned with Maven profiles, goals, and failure policies.

### `configurationFiles`
- Type: `string[]`
- Default: `[]`

List of configuration files (`wakamiti` root) merged in order.


### `properties`
- Type: `map<string,string>`
- Default: `{ }`

Overrides Wakamiti properties from `pom.xml`.


### `includeProjectDependencies`
- Type: `boolean`
- Default: `false`

When enabled, runtime classpath from project dependencies is added to execution.


### `skipTests`
- Type: `boolean`
- Default: `false`

Skips Wakamiti test plan execution.


### `testFailureIgnore`
- Type: `boolean`
- Default: `false`

When enabled, failing tests do not break the build.


### `logLevel`
- Type: `string`
- Default: `info`

Log level used by Maven launcher.


### Configuration example

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

Run:
```shell copy=true
mvn verify
```


## Wakamiti CLI

This launcher runs Wakamiti from the command line, without depending on JUnit or build-plugin wiring. It loads
configuration, resolves modules, and executes the plan in a standalone way.

This is the right option for fast local runs, lightweight automations, and environments where only configuration files
and dependency repositories are available.

General syntax:
```shell copy=true
wakamiti [options]
```


### `wakamiti.launcher.modules`
- Type: `string[]`

Maven modules in `<groupId>:<artifactId>:<version>` format.

Example:
```yaml
wakamiti:
  launcher:
    modules:
      - com.example:functional-plugin:1.0.0
      - com.example:report-plugin:1.0.0
```


### `mavenFetcher.remoteRepositories`
- Type: `URL[]` (separated by `;`)

Example:
```yaml
mavenFetcher:
  remoteRepositories: https://repo.maven.apache.org/maven2;file:///C:/Users/user/.m2/repository
```


### `mavenFetcher.localRepository`
- Type: `file`

Local repository used to cache artifacts downloaded by launcher.


### `-h`, `--help`
- Type: `boolean` (flag)

Shows help and exits without running tests.

Example:
```shell copy=true
wakamiti -h
```


### `-d`, `--debug`
- Type: `boolean` (flag)

Enables debug traces. If [`wakamiti.log.level`](/en/wakamiti/architecture#wakamitiloglevel) is not set, `debug` is used.

Example:
```shell copy=true
wakamiti -d
```


### `-c`, `--clean`
- Type: `boolean` (flag)

Cleans local cache before downloading modules.

Example:
```shell copy=true
wakamiti -c
```


### `-f`, `--file`
- Type: `file`
- Default: `wakamiti.yaml`

Specifies the configuration file to load.

Example:
```shell copy=true
wakamiti -f wakamiti.ci.yaml
```


### `-m`, `--modules`
- Type: `string[]` (comma-separated)

Adds modules from CLI and concatenates them with `wakamiti.launcher.modules`.

Example:
```shell copy=true
wakamiti -m com.example:functional-plugin:1.0.0,com.example:report-plugin:1.0.0
```


### `-n`, `--dry-run`
- Type: `boolean` (flag)

Forces dry-run execution. Maps to [`wakamiti.dryRun`](/en/wakamiti/architecture#wakamitidryrun).

Example:
```shell copy=true
wakamiti -n
```


### `-K key=value`
- Type: `key=value` (repeatable)

Overrides `wakamiti.*` properties from CLI.

Example:
```shell copy=true
wakamiti -K tagFilter="@smoke and not @ignore" -K outputFilePath=results/wakamiti.json
```


### `-M key=value`
- Type: `key=value` (repeatable)

Overrides `mavenFetcher.*` properties from CLI.

Example:
```shell copy=true
wakamiti -M remoteRepositories="https://repo.maven.apache.org/maven2"
```


### `-l`, `--list`
- Type: `boolean` (flag)

Shows available contributions loaded for execution.

Example:
```shell copy=true
wakamiti -l
```


### Full CLI example

```shell copy=true
wakamiti -f wakamiti.ci.yaml -m com.example:functional-plugin:1.0.0 -K tagFilter="@smoke" -M remoteRepositories="https://repo.maven.apache.org/maven2"
```


### Docker usage

Windows:
```shell copy=true
docker run --rm -v "%cd%:/wakamiti" wakamiti/wakamiti
```

Linux:
```shell copy=true
docker run --rm -v "$(pwd):/wakamiti" --add-host=host.docker.internal:host-gateway wakamiti/wakamiti
```
