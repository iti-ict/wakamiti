package iti.kukumo.test.api;

import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.KukumoFetcher;
import iti.kukumo.api.extensions.StepContributor;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.*;

public class KukumoFetcherTest {

    private static final Logger LOGGER = LoggerFactory.getLogger("iti.kukumo.test");

    @Test
    public void testFetcher() throws URISyntaxException, ClassNotFoundException {
        KukumoFetcher fetcher = new KukumoFetcher(LOGGER, Kukumo.defaultConfiguration()
                .appendFromPairs(KukumoConfiguration.RESOURCE_PATH, "./src/test/resources")) {
        };
        fetcher.loadGroovyClasses();

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Class cls = cl.loadClass("iti.kukumo.custom.CustomSteps");
        assertTrue(StepContributor.class.isAssignableFrom(cls));

        URL url = cl.getResource("customs.properties");
        assertNotNull(url);
    }
}
