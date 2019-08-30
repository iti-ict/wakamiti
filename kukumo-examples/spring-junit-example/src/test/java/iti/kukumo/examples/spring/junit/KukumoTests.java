package iti.kukumo.examples.spring.junit;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import iti.commons.configurer.Configurator;
import iti.kukumo.spring.junit.KukumoSpringJUnitRunner;

// Kukumo Configuration
@RunWith(KukumoSpringJUnitRunner.class)
@Configurator(path = "classpath:application-test.yaml", pathPrefix = "kukumo")

// Spring Configuration
@ContextConfiguration(classes = AppTestConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles="test")
public class KukumoTests {


}
