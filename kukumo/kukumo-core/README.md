# TODO

== Usage

===== JUnit runner



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