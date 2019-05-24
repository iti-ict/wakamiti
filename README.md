
# Kukumo

Kukumo is Cucumber-based automatic testing framework

## Usage

### Configuration

### Runners

Kukumo can be executed using three different mechanisms, regarding the nature of the project.

#### JUnit runner

For Java projects, you can create an empty testing class to be run as a normal JUnit test suite, 
setting the custom JUnit runner ```KukumoJUnitRunner```. For example:

```java
package iti.kukumo.gherkin.test.runner;

import iti.commons.configurer.Configurator;
import iti.commons.configurer.Property;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.gherkin.GherkinResourceType;
import iti.kukumo.junit.KukumoJUnitRunner;
import org.junit.runner.RunWith;

@Configurator(properties = {
   @Property(key=KukumoConfiguration.RESOURCE_TYPE, value= GherkinResourceType.NAME),
   @Property(key=KukumoConfiguration.RESOURCE_PATH, value="src/test/resources/features"),
   @Property(key=KukumoConfiguration.OUTPUT_FILE_PATH, value="target/kukumo.json")
})
@RunWith(KukumoJUnitRunner.class)
public class TestKukumoRunner {

    
}
```

#### Maven plugin

For non-Java Maven projects, you can execute Kukumo attaching the ```kukumo-maven-plugin``` plugin to the 
Maven lifecycle.  Simply add something like the following to your ```pom.xml``` :

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

#### Standalone launcher

## License

## Authors
