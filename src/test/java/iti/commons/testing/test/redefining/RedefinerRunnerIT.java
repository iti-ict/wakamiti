/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing.test.redefining;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import iti.commons.testing.cucumber.interceptor.ExtendedCucumber;
import iti.commons.testing.cucumber.interceptor.InterceptedBy;
import iti.commons.testing.cucumber.redefining.RedefiningInterceptor;
import iti.commons.testing.cucumber.redefining.RedefiningOptions;

@RunWith(ExtendedCucumber.class)
@CucumberOptions (
        strict = true,
        features = {
                "src/test/resources/features/sourceDefinitions.feature",
                "src/test/resources/features/targetDefinitions.feature"
        },
        glue = {
        		"iti.commons.testing.test.log",
        		"iti.commons.testing.test.rest", 
        		"iti.commons.testing.test.database"
        },
        monochrome = false,
        plugin = {
        		"pretty",
        		"html:target/cucumber/html-report",
        		"junit:target/cucumber/cucumber.xml",
        		"json:target/cucumber/cucumber.json"
        		}
)
@InterceptedBy(RedefiningInterceptor.class)
@RedefiningOptions(idTagPattern="@TestCase-(.*)", sourceTags="@Definition", targetTags="@Implementation")

public class RedefinerRunnerIT {

}
