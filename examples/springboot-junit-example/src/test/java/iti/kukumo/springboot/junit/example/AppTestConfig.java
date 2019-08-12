package iti.kukumo.springboot.junit.example;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;

@TestConfiguration
@ComponentScan(basePackages = {
    "iti.commons.jext.spring", // include jExt Spring integration
    "iti.kukumo.spring"        // include Kukumo Spring integration
})
public class AppTestConfig {

}
