## Interesting parts

### pom.xml

Since we want to attach Wakamiti to the verify goal, we need to declare the `build` section 
including the `wakamiti-maven-plugin` plugin. Also, we use `spring-boot-maven-plugin` that allows 
to start the application prior to running the tests, and stop it afterwards.

```xml
<build>
    <plugins>

        <!-- Start Spring Boot application prior to integration tests and stop it afterwards -->
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <executions>
                <execution>
                    <id>pre-integration-test</id>
                    <goals>
                        <goal>start</goal>
                    </goals>
                </execution>
                <execution>
                    <id>post-integration-test</id>
                    <goals>
                        <goal>stop</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>


        <!-- Attach Wakamiti to the verify phase of the project -->
        <plugin>
            <groupId>es.iti.wakamiti</groupId>
            <artifactId>wakamiti-maven-plugin</artifactId>
            <version>1.0.0</version>
            <executions>
                <!-- Executed at verify phase -->
                <execution>
                    <goals>
                        <goal>verify</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <!-- Enable/disable tests execution (enabled by default) -->
                <skipTests>${skipExampleTests}</skipTests>
                <!-- Wakamiti configuration -->
                <properties>
                    <resourceTypes>gherkin</resourceTypes>
                    <resourcePath>src/test/resources</resourcePath>
                    <outputFilePath>target/wakamiti/wakamiti.json</outputFilePath>
                    <logs.showStepSource>false</logs.showStepSource>
                    <!-- more configuration required -->
                </properties>
            </configuration>
            <dependencies>
                <!-- Wakamiti plugins -->
                <dependency>
                    <groupId>es.iti.wakamiti</groupId>
                    <artifactId>wakamiti-rest</artifactId>
                    <version>1.0.0</version>
                </dependency>
                <dependency>
                    <groupId>es.iti.wakamiti</groupId>
                    <artifactId>wakamiti-db</artifactId>
                    <version>1.0.0</version>
                </dependency>
            </dependencies>
        </plugin>

    </plugins>
</build>
```    

Also notice:

- Wakamiti is configured in the `properties` tag inside the general `configuration` tag 
allowed for any Maven plugin. Properties used by other Wakamiti plugins are declared here 
as well. 

- The required Wakamiti plugins are included as dependencies of `wakamiti-maven-plugin`, 
not as dependencies of the project.

### .mvn/jvm.config

Currently, there is no user-friendly method to configure the logging level of a Maven plugin. The 
workaround to get this done is create a file named `jvm.config` inside a folder `.mvn` at the 
root folder of the project. In this file, you can add custom MAVEN_OPTS parameters in order to 
configure the logger (that is by default SLF4J Simple Logger).

This way, to enable debug logs for Wakamiti as a Maven plugin, this file should contain:
```
-Dorg.slf4j.simpleLogger.log.iti.wakamiti=debug
```  