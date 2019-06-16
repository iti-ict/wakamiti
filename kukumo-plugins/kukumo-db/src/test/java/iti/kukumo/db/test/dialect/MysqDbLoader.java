package iti.kukumo.db.test.dialect;

import org.junit.Rule;

import iti.commons.testing.embeddeddb.EmbeddedDatabaseRule;
import iti.commons.testing.embeddeddb.EmbeddedDatabaseTemplates;

/*@RunWith(KukumoJUnitRunner.class)
@Configurator(properties = { 
    @Property(key=KukumoConfiguration.RESOURCE_PATH,value="src/test/resources/features"),    
    @Property(key=DatabaseStepConfiguration.DATABASE_CONNECTION_URL,value="jdbc:mysql://localhost:3307/testdb"),
    @Property(key=DatabaseStepConfiguration.DATABASE_CONNECTION_USERNAME,value="test"),
    @Property(key=DatabaseStepConfiguration.DATABASE_CONNECTION_PASSWORD,value="test")
})*/
public class MysqDbLoader {

    @Rule 
    public EmbeddedDatabaseRule embeddedDatabase = new EmbeddedDatabaseRule(EmbeddedDatabaseTemplates.MYSQL);
   
    
    
    
}
