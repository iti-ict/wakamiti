# TODO

## Usage

```xml
 <build>
    <plugins>
        <plugin>
            <groupId>iti.wakamiti</groupId>
            <artifactId>wakamiti-maven-plugin</artifactId>
            <version>0.1.0</version>
            <configuration>
                <configurationFiles>
                    <configurationFile>
                        wakamiti.yaml
                    </configurationFile>
                </configurationFiles>
                <properties>
                    <wakamitiProperty1>valueOfProperty1</wakamitiProperty1>
                    <wakamitiProperty2>valueOfProperty2</wakamitiProperty2>
                </properties>
            </configuration>
            <dependencies>
                <!-- Wakamiti Plugins -->
                <dependency>
                    <groupId>iti.wakamiti</groupId>
                    <artifactId>wakamiti-gherkin</artifactId>
                    <version>0.1.0</version>
                </dependency>
                <dependency>
                    <groupId>iti.wakamiti</groupId>
                    <artifactId>wakamiti-rest</artifactId>
                    <version>0.1.0</version>
                </dependency>
                <dependency>
                    <groupId>iti.wakamiti</groupId>
                    <artifactId>wakamiti-html-report</artifactId>
                    <version>0.1.0</version>
                </dependency>
            </dependencies>
            <executions>
                <!-- Executed at verify phase -->
                <execution>
                    <goals>
                        <goal>verify</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
  </build>
```