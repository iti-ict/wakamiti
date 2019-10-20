/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.examples.spring.junit;


import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;


@TestConfiguration
@ComponentScan(basePackages = {
                "iti.commons.jext.spring", // include jExt Spring integration
                "iti.kukumo.spring" // include Kukumo Spring integration
})
public class AppTestConfig {

}
