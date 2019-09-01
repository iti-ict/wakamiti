package iti.kukumo.spring.junit;

import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.springframework.test.context.TestContextManager;

import iti.kukumo.api.Kukumo;
import iti.kukumo.junit.KukumoJUnitRunner;


public class KukumoSpringJUnitRunner extends KukumoJUnitRunner {

    private static final Logger LOGGER = Kukumo.LOGGER;
    private final TestContextManager testContextManager;

    public KukumoSpringJUnitRunner(Class<?> configurationClass) throws InitializationError {
        super(configurationClass);
        this.testContextManager = createTestContextManager(configurationClass);
        try {
            this.testContextManager.prepareTestInstance(this);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            LOGGER.debug(e.getMessage(),e);
        }
    }

    protected TestContextManager createTestContextManager(Class<?> clazz) {
        return new TestContextManager(clazz);
    }

    protected final TestContextManager getTestContextManager() {
        return this.testContextManager;
    }


}
