package iti.kukumo.database.test.dialect;

import org.junit.Rule;
import org.junit.runner.RunWith;

import iti.commons.configurer.Configurator;
import iti.commons.configurer.Property;
import iti.commons.testing.embeddeddb.EmbeddedDatabaseRule;
import iti.commons.testing.embeddeddb.EmbeddedDatabaseTemplates;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.database.DatabaseStepConfiguration;
import iti.kukumo.junit.KukumoJUnitRunner;


@RunWith(KukumoJUnitRunner.class)
@Configurator(properties = { 
    @Property(key=KukumoConfiguration.RESOURCE_PATH,value="src/test/resources/features"),    
    @Property(key=DatabaseStepConfiguration.DATABASE_CONNECTION_URL,value="jdbc:h2:mem:testdb"),
    @Property(key=DatabaseStepConfiguration.DATABASE_CONNECTION_USERNAME,value="sa"),
    @Property(key=DatabaseStepConfiguration.DATABASE_CONNECTION_PASSWORD,value="")
})
public class H2DbLoader {

    @Rule 
    public EmbeddedDatabaseRule embeddedDatabase = new EmbeddedDatabaseRule(EmbeddedDatabaseTemplates.H2);
    
    
    
}
