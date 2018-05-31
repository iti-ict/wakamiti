/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing.test.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cucumber.api.java.After;
import cucumber.api.java.Before;

public class LogSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogSteps.class);

    @Before(order=1)
    public void before() {
        LOGGER.info("------- START SCENARIO --------------------------------------------------");
    }

    @After(order=1)
    public void after() {
        LOGGER.info("------- END SCENARIO ----------------------------------------------------");
    }
}
