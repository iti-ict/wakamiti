Create a new plugin using the Maven archetype
---------------------------------------------

1. Install the Maven archetype in the local repository (since it is an internal 
development tools, is not available in the Maven central repository)
```
./mvnw clean install -pl wakamiti-plugin-maven-archetype -U
```

2. Create the new Maven module in the `plugins` folder
```
cd plugins
../mvnw archetype:generate -DarchetypeGroupId=es.iti.wakamiti -DarchetypeArtifactId=wakamiti-plugin-maven-archetype -U
```

You will be asked for some properties such as the plugin name and description,
and the Wakamiti API and Core versions. The only required property is the 
plugin identifier, the rest of properties can be automatically filled using the 
default values.

```
[INFO] --- maven-archetype-plugin:3.2.1:generate (default-cli) @ wakamiti-plugin-aggregator ---
[INFO] Generating project in Interactive mode
[INFO] Archetype [es.iti.wakamiti:wakamiti-plugin-maven-archetype:1.0.0] found in catalog local

Define value for property 'pluginId': myplugin

[INFO] Using property: defaultWakamitiApiVersion = 2.3.2
[INFO] Using property: defaultWakamitiCoreVersion = 2.3.2
[INFO] Using property: groupId = es.iti.wakamiti

Define value for property 'version' 1.0-SNAPSHOT: : 
Define value for property 'PluginId' Myplugin: : 
Define value for property 'package' es.iti.wakamiti.plugins.myplugin: : 
Define value for property 'pluginName' myplugin: : 
Define value for property 'pluginDescription' myplugin: : 
Define value for property 'wakamitiApiVersion' 2.3.2: : 
Define value for property 'wakamitiCoreVersion' 2.3.2: : 
Define value for property 'artifactId' myplugin-wakamiti-plugin: :
 
Confirm properties configuration:
pluginId: myplugin
defaultWakamitiApiVersion: 2.3.2
defaultWakamitiCoreVersion: 2.3.2
groupId: es.iti.wakamiti
version: 1.0-SNAPSHOT
PluginId: Myplugin
package: es.iti.wakamiti.plugins.myplugin
pluginName: myplugin
pluginDescription: myplugin
wakamitiApiVersion: 2.3.2
wakamitiCoreVersion: 2.3.2
artifactId: myplugin-wakamiti-plugin
 Y: : 
[INFO] ----------------------------------------------------------------------------
[INFO] Using following parameters for creating project from Archetype: wakamiti-plugin-maven-archetype:1.0.0
[INFO] ----------------------------------------------------------------------------
[INFO] Parameter: groupId, Value: es.iti.wakamiti
[INFO] Parameter: artifactId, Value: myplugin-wakamiti-plugin
[INFO] Parameter: version, Value: 1.0-SNAPSHOT
[INFO] Parameter: package, Value: es.iti.wakamiti.plugins.myplugin
[INFO] Parameter: packageInPathFormat, Value: es/iti/wakamiti/plugins/myplugin
[INFO] Parameter: defaultWakamitiApiVersion, Value: 2.3.2
[INFO] Parameter: package, Value: es.iti.wakamiti.plugins.myplugin
[INFO] Parameter: pluginId, Value: myplugin
[INFO] Parameter: groupId, Value: es.iti.wakamiti
[INFO] Parameter: wakamitiCoreVersion, Value: 2.3.2
[INFO] Parameter: PluginId, Value: Myplugin
[INFO] Parameter: version, Value: 1.0-SNAPSHOT
[INFO] Parameter: wakamitiApiVersion, Value: 2.3.2
[INFO] Parameter: pluginName, Value: myplugin
[INFO] Parameter: pluginDescription, Value: myplugin
[INFO] Parameter: artifactId, Value: myplugin-wakamiti-plugin
[INFO] Parameter: defaultWakamitiCoreVersion, Value: 2.3.2
[INFO] Parent element not overwritten in /home/linesta/github/wakamiti/plugins/myplugin-wakamiti-plugin/pom.xml
[INFO] Project created from Archetype in dir: /home/linesta/github/wakamiti/plugins/myplugin-wakamiti-plugin
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO] 
[INFO] [Wakamiti Plugin] AMQP Steps 2.3.2 ................. SKIPPED
[INFO] [Wakamiti Plugin] Appium Steps 2.2.2 ............... SKIPPED
[INFO] [Wakamiti Plugin] Azure integration 1.3.2 .......... SKIPPED
[INFO] [Wakamiti Plugin] Database Steps 2.3.2 ............. SKIPPED
[INFO] [Wakamiti Plugin] Cucumber Exporter 2.3.2 .......... SKIPPED
[INFO] [Wakamiti Plugin] Groovy Steps 2.3.2 ............... SKIPPED
[INFO] [Wakamiti Plugin] HTML Report 2.3.2 ................ SKIPPED
[INFO] [Wakamiti Plugin] REST Steps 2.3.2 ................. SKIPPED
[INFO] [Wakamiti Plugin] SpringBoot integration 2.3.2 ..... SKIPPED
[INFO] [Wakamiti Plugin] Files Steps 2.3.2 ................ SKIPPED
[INFO] [Wakamiti Plugin] File Uploader 2.4.2 .............. SKIPPED
[INFO] [Wakamiti Plugin] Email Steps 1.1.2 ................ SKIPPED
[INFO] Wakamiti Plugin Aggregator 1.0.0 ................... SUCCESS [ 31.470 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  32.647 s
[INFO] Finished at: 2023-10-03T10:16:57+02:00
[INFO] ------------------------------------------------------------------------
```

3. Add the new plugin to the plugin aggregator POM in `plugins/pom.xml`

```xml
  ...
  <name>Wakamiti Plugin Aggregator</name>
      
  <modules>
    ...
    <module>myplugin-wakamiti-plugin</module>
  
  </modules>

```