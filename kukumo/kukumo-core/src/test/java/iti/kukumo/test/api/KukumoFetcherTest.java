/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.kukumo.test.api;

import iti.commons.configurer.Configuration;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.KukumoFetcher;
import iti.kukumo.api.extensions.StepContributor;
import iti.kukumo.util.LocaleLoader;
import iti.kukumo.util.ResourceLoader;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static iti.kukumo.api.KukumoConfiguration.NON_REGISTERED_STEP_PROVIDERS;
import static org.junit.Assert.*;

public class KukumoFetcherTest {

    private static final Logger LOGGER = LoggerFactory.getLogger("iti.kukumo.test");

    public KukumoFetcherTest() {
        KukumoFetcher fetcher = new KukumoFetcher(LOGGER, Kukumo.defaultConfiguration()
                .appendFromPairs(KukumoConfiguration.RESOURCE_PATH, "./src/test/resources")) {
        };
        fetcher.loadGroovyClasses();
        loadContributors();
    }

    @Test
    public void testFetcher() throws
            IllegalAccessException,
            NoSuchMethodException,
            InvocationTargetException
    {
        StepContributor contributor = getTestContributor();

        Class cls = contributor.getClass();

        cls.getMethod("whatever", Integer.class, Integer.class)
                .invoke(contributor, 5, 6);
        cls.getMethod("something", String.class)
                .invoke(contributor, "ALGO");

        ResourceBundle res = ResourceBundle.getBundle("customs", LocaleLoader.forLanguage("es"),
                Thread.currentThread().getContextClassLoader());
        assertNotNull(res);
        LOGGER.debug("Result: {}", res.getBaseBundleName());
    }

    private void loadContributors() {
        Configuration configuration = Kukumo.defaultConfiguration()
                .appendFromMap(Map.of(NON_REGISTERED_STEP_PROVIDERS,
                        Arrays.asList("iti.kukumo.test.gherkin.KukumoSteps", "iti.kukumo.custom.CustomSteps"))
        );
        Kukumo.instance().newBackendFactory().createNonRunnableBackend(configuration);
    }

    private StepContributor getTestContributor() {
        return (StepContributor) Kukumo.contributors().allContributors()
                .get(StepContributor.class).stream()
                .filter(c -> c.info().equals("iti.kukumo.custom.CustomSteps")).findFirst()
                .get();
    }
}