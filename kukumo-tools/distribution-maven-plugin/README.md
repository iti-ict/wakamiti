# Distribution Maven Plugin

*Distribution Maven Plugin* is a Maven plugin that creates a Jar file with the
contents of a given directory, along with a main class that allows to copy a subset of those
contents to specific directories regarding the operating system where is executed. In other words,
it creates a **basic console installer**.

This plugin is intended to be used along with [Apache Maven Assembly Plugin][1], which can 
gather and assemble all the required dependencies for your project. This way, the resulting 
installer would automatically include all the libraries you need in order to distribute your 
software.

## Usage

Simply provide a *distribution definition* file, in YAML format, like the following:
```yaml
applicationName: My Application
distributions:
  - os: linux
    fileSet:
      - destinationFolder: /usr/local/share/myapp
        files:
          - myapp.sh
          - myapp.jar
          - myapp.properties
          - lib/**
    environmentVariables:
      MYAPP_HOME: /usr/local/share/myapp
  - os: windows
    fileSet:
      - destinationFolder: "%ProgramFiles%\\MyApp"
        files:
          - myapp.bat
          - myapp.jar
          - myapp.properties
          - lib/**
    environmentVariables:
      MYAPP_HOME: "%ProgramFiles%\\MyApp"
      PATH: "%PATH%;%ProgramFiles%\\MyApp"
``` 

And declare both plugins in the ```build``` section of your ```pom.xml```:

```xml
<build>
    <!-- assemble staging folder -->
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-assembly-plugin</artifactId>
        <configuration>
            <descriptors>
                <descriptor>src/assembly/distribution.xml</descriptor>
            </descriptors>
            <appendAssemblyId>false</appendAssemblyId>
            <finalName>staging</finalName>
        </configuration>
        <executions>
            <execution>
                <id>make-assembly</id>
                <phase>prepare-package</phase>
                <goals>
                    <goal>single</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
    <!-- create the installer -->
    <plugin>
        <groupId>iti.commons</groupId>
        <artifactId>distribution-maven-plugin</artifactId>
        <version>1.0.0</version>
        <configuration>
            <distributionDefinition>src/assembly/distribution.yaml</distributionDefinition>
            <stagingDirectory>${project.build.directory}/staging</stagingDirectory>       
            <output>src/bin/${project.artifactId}-${project.version}-installer.jar</output>
        </configuration>
        <executions>
            <execution>
                <phase>package</phase>
                <goals>
                    <goal>generate-installer</goal>
                </goals>
            </execution>
        </executions>
    </plugin>   
</build>
```



[[back to top](#top)]
  
## References  
- [**1**] *Apache Maven Assembly Plugin* - http://maven.apache.org/plugins/maven-assembly-plugin  
  
[1]: http://maven.apache.org/plugins/maven-assembly-plugin/
