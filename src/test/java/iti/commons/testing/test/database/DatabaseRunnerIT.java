/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing.test.database;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions (
        strict = true,
        features = "src/test/resources/features/databaseTest.feature",
        glue = {"iti.commons.testing.test.database"},
        monochrome = true,
        plugin = {
        		"pretty",
        		"html:target/cucumber/html-report",
        		"junit:target/cucumber/cucumber.xml",
        		"json:target/cucumber/cucumber.json"
       }
)
public class DatabaseRunnerIT {

}
