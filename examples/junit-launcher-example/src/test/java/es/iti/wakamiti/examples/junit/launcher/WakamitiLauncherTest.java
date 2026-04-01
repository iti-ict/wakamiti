package es.iti.wakamiti.examples.junit.launcher;

import es.iti.wakamiti.api.imconfig.AnnotatedConfiguration;
import es.iti.wakamiti.junit.WakamitiJUnitRunner;
import org.junit.runner.RunWith;

@RunWith(WakamitiJUnitRunner.class)
@AnnotatedConfiguration(path = "classpath:wakamiti-test.yaml", pathPrefix = "wakamiti")
public class WakamitiLauncherTest {
}
