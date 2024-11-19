package es.iti.wakamiti.xray.test;

import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.core.JsonPlanSerializer;
import es.iti.wakamiti.xray.XRaySynchronizer;
import es.iti.wakamiti.xray.XrayConfigContributor;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;

public class TestXRayReporter {

    @Test
    public void test() throws IOException {
        XRaySynchronizer reporter = new XRaySynchronizer();
        configure(reporter, "wakamiti.yaml");

        Event event = event(Event.PLAN_CREATED, new JsonPlanSerializer().read(resource("wakamiti.json")));
        reporter.eventReceived(event);

    }

    private void configure(XRaySynchronizer reporter, String resource) {
        XrayConfigContributor xrayConfig = new XrayConfigContributor();
        Configuration config = WakamitiConfiguration.DEFAULTS
                .append(xrayConfig.defaultConfiguration())
                .append(Configuration.factory()
                        .fromResource(resource, classLoader())
                        .inner("wakamiti"));
        xrayConfig.configurer().configure(reporter, config);
    }

    private ClassLoader classLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    private InputStream resource(String resource) {
        return classLoader().getResourceAsStream(resource);
    }

    private Event event(String type, Object data) {
        return new Event(type, Instant.now(), data);
    }

}
