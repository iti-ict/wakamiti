package iti.kukumo.spring.junit;

import java.util.List;

import org.junit.runners.model.InitializationError;
import org.springframework.test.context.TestContextManager;

import iti.kukumo.api.Kukumo;
import iti.kukumo.api.event.Event;
import iti.kukumo.api.extensions.EventObserver;
import iti.kukumo.junit.JUnitPlanNodeRunner;
import iti.kukumo.junit.KukumoJUnitRunner;


public class KukumoSpringJUnitRunner extends KukumoJUnitRunner implements EventObserver {

    private final TestContextManager testContextManager;

    public KukumoSpringJUnitRunner(Class<?> configurationClass) throws InitializationError {
        super(configurationClass);
        this.testContextManager = createTestContextManager(configurationClass);
        try {
            this.testContextManager.prepareTestInstance(this);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Kukumo.addEventDispatcherObserver(this);
    }

    protected TestContextManager createTestContextManager(Class<?> clazz) {
        return new TestContextManager(clazz);
    }

    protected final TestContextManager getTestContextManager() {
        return this.testContextManager;
    }

    @Override
    public boolean acceptType(String eventType) {
        return eventType.equals(Event.BEFORE_RUN_BACKEND_STEP);
    }

    @Override
    public void eventReceived(Event<?> event) {
        try {
          //  getTestContextManager().prepareTestInstance(event.data());
        } catch (Exception e) {
            LOGGER.error("Problem preparing Spring test instante",e);
        }
    }

}
