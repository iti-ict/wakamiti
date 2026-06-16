# Wakamiti Maven Plugin

`wakamiti-maven-plugin` integrates Wakamiti execution into the Maven lifecycle. The module currently exposes three goals:

- `verify`: build and execute the Wakamiti plan
- `report`: invoke report contributors on previously generated results
- `control`: rethrow deferred failures when `verify` is configured to continue

## Typical usage

```xml
<build>
    <plugins>
        <plugin>
            <groupId>es.iti.wakamiti</groupId>
            <artifactId>wakamiti-maven-plugin</artifactId>
            <version>{version}</version>
            <executions>
                <execution>
                    <phase>integration-test</phase>
                    <goals>
                        <goal>verify</goal>
                    </goals>
                </execution>
                <execution>
                    <phase>verify</phase>
                    <goals>
                        <goal>report</goal>
                        <goal>control</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <configurationFiles>src/test/resources/wakamiti.yaml</configurationFiles>
                <skipTests>${skipWakamiti}</skipTests>
                <includeProjectDependencies>false</includeProjectDependencies>
                <logLevel>info</logLevel>
                <properties>
                    <resourcePath>${project.basedir}/src/test/resources</resourcePath>
                    <outputFilePath>target/wakamiti/wakamiti.json</outputFilePath>
                    <htmlReport.output>target/wakamiti/wakamiti.html</htmlReport.output>
                </properties>
            </configuration>
            <dependencies>
                <dependency>
                    <groupId>es.iti.wakamiti</groupId>
                    <artifactId>rest-wakamiti-plugin</artifactId>
                    <version>{version}</version>
                </dependency>
                <dependency>
                    <groupId>es.iti.wakamiti</groupId>
                    <artifactId>html-report-wakamiti-plugin</artifactId>
                    <version>{version}</version>
                </dependency>
            </dependencies>
        </plugin>
    </plugins>
</build>
```

## Key configuration fields

### `verify`

- `skipTests`: skips Wakamiti execution.
- `includeProjectDependencies`: adds the current project's runtime classpath to the Wakamiti execution classloader.
- `configurationFiles`: list of configuration files to merge.
- `properties`: inline `wakamiti.*` properties merged over the configuration files.
- `logLevel`: SLF4J simple logger level for `es.iti.wakamiti`.
- `testFailureIgnore`: keeps the build moving even when the plan fails.

### `report`

- `configurationFiles`
- `properties`
- `testFailureIgnore`

`report` reads the effective configuration and calls `Wakamiti.instance().generateReports(...)`.

### `control`

`control` has no extra parameters. It exists to rethrow the stored plugin exception during `verify` when you have deferred failure handling across phases.

## Practical notes

- Wakamiti plugins belong in the Maven plugin's `<dependencies>`, not in the tested application's main dependencies.
- Use `includeProjectDependencies=true` only when the plan needs to load classes from the project itself at runtime.
- `verify` defaults to Maven phase `integration-test`; `report` and `control` default to `verify`.

## Example module

See [examples/spring-verify-example/README.md](../../examples/spring-verify-example/README.md) for a working project that starts a Spring Boot application and runs Wakamiti during Maven execution.
