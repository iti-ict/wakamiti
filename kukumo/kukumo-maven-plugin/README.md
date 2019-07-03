# TODO

## Usage

```xml
 <build>
    <plugins>
        <plugin>
            <groupId>iti.kukumo</groupId>
            <artifactId>kukumo-maven-plugin</artifactId>
            <version>0.1.0</version>
            <configuration>
                <configurationFiles>
                    <configurationFile>
                        kukumo.yaml
                    </configurationFile>
                </configurationFiles>
                <properties>
                    <kukumoProperty1>valueOfProperty1</kukumoProperty1>
                    <kukumoProperty2>valueOfProperty2</kukumoProperty2>
                </properties>
            </configuration>
            <dependencies>
                <!-- Kukumo Plugins -->
                <dependency>
                    <groupId>iti.kukumo</groupId>
                    <artifactId>kukumo-gherkin</artifactId>
                    <version>0.1.0</version>
                </dependency>
                <dependency>
                    <groupId>iti.kukumo</groupId>
                    <artifactId>kukumo-rest</artifactId>
                    <version>0.1.0</version>
                </dependency>
                <dependency>
                    <groupId>iti.kukumo</groupId>
                    <artifactId>kukumo-html-report</artifactId>
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