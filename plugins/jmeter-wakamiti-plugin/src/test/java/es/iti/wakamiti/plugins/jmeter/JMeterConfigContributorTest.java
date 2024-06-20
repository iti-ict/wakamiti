/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.plugins.jmeter;


import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.util.MatcherAssertion;
import imconfig.Configuration;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import us.abstracta.jmeter.javadsl.core.listeners.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class JMeterConfigContributorTest {

    private static String path;

    private final JMeterConfigContributor configContributor = new JMeterConfigContributor();
    @Spy
    private JMeterStepContributor contributor;

    @BeforeClass
    public static void setup() throws IOException {
        path = new File(".").getCanonicalPath();
    }

    @Test
    public void testWhenConfigDefaultWithSuccess() throws MalformedURLException {
        // prepare
        Configuration configuration = configContributor.defaultConfiguration();

        // act
        configContributor.configurer().configure(contributor, configuration);

        // check
        verify(contributor).setBaseURL(new URL("http://localhost:8080"));
        verify(contributor).setContentType("APPLICATION_JSON");
        verify(contributor).setHttpCodeAssertion(argThat(m ->
                m.description().equals(new MatcherAssertion<>(Matchers.lessThan(500)).description())
        ));
        verify(contributor).setTimeout(Duration.ofMillis(60000));
        verify(contributor, times(0)).cookiesEnabled();
        verify(contributor, times(0)).cacheEnabled();
        assertThat(field(contributor.httpDefaults, "downloadEmbeddedResources", Boolean.class)).isFalse();
        verify(contributor, times(0)).resourcesMatching(any());
        verify(contributor, times(0)).resourcesNotMatching(any());

        verify(contributor, times(0)).setProxy(any(), any(), any());
        verify(contributor, times(0)).setProxy(any());

        verify(contributor, times(0)).setBasicAuth(any(), any());

        assertThat(contributor.oauth2Provider.configuration().clientId()).isNull();
        assertThat(contributor.oauth2Provider.configuration().clientSecret()).isNull();
        assertThat(contributor.oauth2Provider.configuration().url()).isNull();
        assertThat(contributor.oauth2Provider.configuration().parameters()).isEmpty();
        contributor.oauth2Provider.configuration().storeTokenAndGet("abc");
        assertThat(contributor.oauth2Provider.configuration().findCachedToken()).isEmpty();

        assertThat(field(contributor.httpDefaults, "followRedirects", Boolean.class)).isTrue();

        assertThat(get(contributor.reporters, DslViewResultsTree.class)).isNotPresent();
        assertThat(get(contributor.reporters, JtlWriter.class)).isPresent()
                .map(x -> field(x, "jtlFile", String.class))
                .contains(path + "/wakamiti.jtl".replace("/", File.separator));
        assertThat(get(contributor.reporters, HtmlReporter.class)).isNotPresent();
        assertThat(get(contributor.reporters, InfluxDbBackendListener.class)).isNotPresent();
        assertThat(get(contributor.reporters, GraphiteBackendListener.class)).isNotPresent();
    }

    @Test
    public void testWhenProxyUrlWithSuccess() throws MalformedURLException {
        // prepare
        Configuration configuration = configContributor.defaultConfiguration()
                .appendFromPairs(
                        "jmeter.proxy.url", "http://prox:8080/api"
                );

        // act
        configContributor.configurer().configure(contributor, configuration);

        // check
        verify(contributor).setProxy(new URL("http://prox:8080/api"));
    }

    @Test
    public void testWhenConfigFileWithSuccess() throws MalformedURLException {
        // prepare
        Configuration configuration = configContributor.defaultConfiguration()
                .appendFromPath(Path.of("src/test/resources/test_config.yaml"))
                .inner("wakamiti");

        // act
        configContributor.configurer().configure(contributor, configuration);

        // check
        verify(contributor).setBaseURL(new URL("http://localhost:8080/api"));
        verify(contributor).setContentType("APPLICATION_XML");
        verify(contributor).setHttpCodeAssertion(argThat(m ->
                m.description().equals(new MatcherAssertion<>(Matchers.lessThan(300)).description())
        ));
        verify(contributor).setTimeout(Duration.ofMillis(10000));
        verify(contributor).cookiesEnabled();
        verify(contributor).cacheEnabled();
        assertThat(field(contributor.httpDefaults, "downloadEmbeddedResources", Boolean.class)).isTrue();
        verify(contributor).resourcesMatching(".*");

        verify(contributor).setProxy(new URL("http://prox:8080/api"), "abc", "s3cr3t");

        verify(contributor).setBasicAuth("pepe", "1234asdf");

        assertThat(contributor.oauth2Provider.configuration().clientId()).isEqualTo("WEB");
        assertThat(contributor.oauth2Provider.configuration().clientSecret()).isEqualTo("s3cr3t");
        assertThat(contributor.oauth2Provider.configuration().url()).isEqualTo(new URL("http://localhost:8080/token"));
        assertThat(contributor.oauth2Provider.configuration().parameters())
                .containsEntry("grant_type", "password")
                .containsEntry("username", "pepe")
                .containsEntry("password", "1234asdf")
                .containsEntry("scope", "something");
        contributor.oauth2Provider.configuration().storeTokenAndGet("abc");
        assertThat(contributor.oauth2Provider.configuration().findCachedToken()).contains("abc");

        assertThat(field(contributor.httpDefaults, "followRedirects", Boolean.class)).isFalse();

        assertThat(get(contributor.reporters, DslViewResultsTree.class)).isPresent();
        assertThat(get(contributor.reporters, JtlWriter.class)).isPresent()
                .map(x -> field(x, "jtlFile", String.class))
                .contains(path + "/target/wakamiti.jtl".replace("/", File.separator));
        assertThat(get(contributor.reporters, HtmlReporter.class)).isPresent();
        assertThat(get(contributor.reporters, InfluxDbBackendListener.class)).isPresent();
        assertThat(get(contributor.reporters, GraphiteBackendListener.class)).isPresent();
    }

    @Test(expected = WakamitiException.class)
    public void testWhenProxyWithoutUrlWithError() {
        // prepare
        Configuration configuration = configContributor.defaultConfiguration()
                .appendFromPairs(
                        "jmeter.proxy.username", "abc"
                );

        // act
        try {
            configContributor.configurer().configure(contributor, configuration);

            // check
        } catch (WakamitiException e) {
            assertThat(e.getMessage()).isEqualTo("Proxy url is needed.");
            throw e;
        }
    }

    @Test(expected = WakamitiException.class)
    public void testWhenAuthWithoutUsernameWithError() {
        // prepare
        Configuration configuration = configContributor.defaultConfiguration()
                .appendFromPairs(
                        "jmeter.auth.password", "1234asdf"
                );

        // act
        try {
            configContributor.configurer().configure(contributor, configuration);

            // check
        } catch (WakamitiException e) {
            assertThat(e.getMessage()).isEqualTo("Auth username and password are needed.");
            throw e;
        }
    }

    @Test(expected = WakamitiException.class)
    public void testWhenAuthWithoutPasswordWithError() {
        // prepare
        Configuration configuration = configContributor.defaultConfiguration()
                .appendFromPairs(
                        "jmeter.auth.username", "pepe"
                );

        // act
        try {
            configContributor.configurer().configure(contributor, configuration);

            // check
        } catch (WakamitiException e) {
            assertThat(e.getMessage()).isEqualTo("Auth username and password are needed.");
            throw e;
        }
    }

    @Test(expected = WakamitiException.class)
    public void testWhenInfluxWithoutUrlWithError() {
        // prepare
        Configuration configuration = configContributor.defaultConfiguration()
                .appendFromPairs(
                        "jmeter.report.influx.token", "s3cr3t1"
                );

        // act
        try {
            configContributor.configurer().configure(contributor, configuration);

            // check
        } catch (WakamitiException e) {
            assertThat(e.getMessage()).isEqualTo("Influx url is needed.");
            throw e;
        }
    }

    @Test(expected = WakamitiException.class)
    public void testWhenGraphiteWithoutUrlWithError() {
        // prepare
        Configuration configuration = configContributor.defaultConfiguration()
                .appendFromPairs(
                        "jmeter.report.graphite.prefix", "pre-"
                );

        // act
        try {
            configContributor.configurer().configure(contributor, configuration);

            // check
        } catch (WakamitiException e) {
            assertThat(e.getMessage()).isEqualTo("Graphite url is needed.");
            throw e;
        }
    }

    private <T> T field(Object o, String f, Class<T> t) {
        try {
            Field field = o.getClass().getDeclaredField(f);
            field.setAccessible(true);
            return t.cast(field.get(o));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new WakamitiException("Error reading field '{}'", f, e);
        }
    }

    private <T> Optional<T> get(List<?> list, Class<T> t) {
        return list.stream().filter(o -> t.isAssignableFrom(o.getClass())).map(t::cast).findFirst();
    }

}
