/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing.test.rest;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions (
        strict = true,
        features = "src/test/resources/features/restTest.feature",
        glue = {
        		"iti.commons.testing.test.log",
        		"iti.commons.testing.test.rest"
        },
        monochrome = false,
        plugin = {
        		"html:target/cucumber/html-report",
        		"junit:target/cucumber/junit.xml",
        		"json:target/cucumber/cucumber.json"
       }
        
)
public class RestRunnerIT {

}
