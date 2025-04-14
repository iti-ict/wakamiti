package es.iti.wakamiti.plugins.modbus.test;


import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.core.junit.WakamitiJUnitRunner;
import es.iti.wakamiti.api.imconfig.AnnotatedConfiguration;
import es.iti.wakamiti.api.imconfig.Property;
import org.junit.runner.RunWith;

@RunWith(WakamitiJUnitRunner.class)
@AnnotatedConfiguration({
        @Property(key = WakamitiConfiguration.RESOURCE_TYPES, value = "gherkin"),
        @Property(key = WakamitiConfiguration.RESOURCE_PATH, value = "src/test/resources/features"),
})
public class ModbusTest {


}