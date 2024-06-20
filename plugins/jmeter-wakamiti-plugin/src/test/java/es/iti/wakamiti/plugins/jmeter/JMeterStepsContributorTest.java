/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.plugins.jmeter;


import imconfig.ConfigurationFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

import static es.iti.wakamiti.plugins.jmeter.JMeterConfigContributor.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class JMeterStepsContributorTest {

//    private static final Integer PORT = 4321;
//    private static final String BASE_URL = MessageFormat.format("https://localhost:{0}", PORT.toString());
//    private static final String TOKEN_PATH = "data/token.txt";

    //    private static final ClientAndServer client = startClientAndServer(PORT);
//
    private final JMeterConfigContributor configurator = new JMeterConfigContributor();
    @Spy
    private JMeterStepContributor contributor;

//    @BeforeClass
//    public static void setup() {
//        ConfigurationProperties.logLevel("OFF");
//        HttpsURLConnection.setDefaultSSLSocketFactory(
//                new KeyStoreFactory(Configuration.configuration(), new MockServerLogger())
//                        .sslContext().getSocketFactory());
//    }
//
//    @AfterClass
//    public static void shutdown() {
//        client.close();
//    }
//
//    @Before
//    public void beforeEach() throws NoSuchFieldException, IllegalAccessException {
//        configurator.configurer().configure(contributor, configurator.defaultConfiguration().appendFromPairs(
//                JMeterConfigContributor.BASE_URL, BASE_URL/*,
//                JMeterConfigContributor.JTL_PATH, "target/wakamiti.jtl"*/
//        ));
//        keys().clear();
//        client.reset();
//    }

    @Test
    public void testWhenConfigDefaultsWithSuccess() throws MalformedURLException {
        // prepare
        configurator.configurer().configure(contributor, configurator.defaultConfiguration());

        // check
        verify(contributor).setBaseURL(new URL("http://localhost:8080"));
        verify(contributor).setContentType("APPLICATION_JSON");
        verify(contributor).setTimeout(Duration.ofMillis(60000));

//        verify(contributor, times(0)).oauth2Provider.configuration().url(any());
//        verify(contributor, times(0)).oauth2Provider.configuration().clientId(any());
//        verify(contributor, times(0)).oauth2Provider.configuration().clientSecret(any());
//        verify(contributor).oauth2Provider.configuration().cacheAuth(false);
//        verify(contributor, times(0)).oauth2Provider.configuration().addParameter(any(), any());

//        verify(contributor, times(0)).setResultTree();
//        verify(contributor).setJtlWriter(any());
//        verify(contributor, Mockito.times(0)).setHtmlReporter(any());
//        verify(contributor, times(0)).setInfluxListener(any());
//        verify(contributor, times(0)).setGraphiteListener(any());
    }

    @Test
    public void testWhenConfigCustomWithSuccess() throws MalformedURLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        // prepare
        configurator.configurer().configure(contributor, ConfigurationFactory.instance().fromPairs(
                BASE_URL, "http://other",
                CONTENT_TYPE, "APPLICATION_XML",
                TIMEOUT, "10000",
                OAUTH2_URL, "http://oauth",
                OAUTH2_CLIENT_ID, "WEB",
                OAUTH2_CLIENT_SECRET, "s3cr3t",
                OAUTH2_DEFAULT_PARAMETERS + ".scope", "TEST",
                OAUTH2_CACHED, Boolean.TRUE.toString(),
                TREE_ENABLED, Boolean.TRUE.toString(),
                JTL_PATH, "other.jtl",
                HTML_PATH, "other.html",
                INFLUX_BASE + "." + URL, "http://influx",
                INFLUX_BASE + "." + TOKEN, "s3cr3t1",
                INFLUX_BASE + "." + TITLE, "title",
                INFLUX_BASE + "." + APPLICATION, "app",
                INFLUX_BASE + "." + MEASUREMENT, "measure",
                INFLUX_BASE + "." + SAMPLERS_REGEX, ".*",
                INFLUX_BASE + "." + TAGS + ".key", "value",
                INFLUX_BASE + "." + PERCENTILES, "1.1",
                INFLUX_BASE + "." + PERCENTILES, "2.4",
                GRAPHITE_BASE + "." + URL, "graph:123",
                GRAPHITE_BASE + "." + PREFIX, "pre-"
        ));

        // check
        verify(contributor).setBaseURL(new URL("http://other"));
        verify(contributor).setContentType("APPLICATION_XML");
        verify(contributor).setTimeout(Duration.ofMillis(10000));

//        verify(contributor).oauth2Provider.configuration().url(new URL("http://oauth"));
//        verify(contributor).oauth2Provider.configuration().clientId("WEB");
//        verify(contributor).oauth2Provider.configuration().clientSecret("s3cr3t");
//        verify(contributor).oauth2Provider.configuration().cacheAuth(true);
//        verify(contributor).oauth2Provider.configuration().addParameter("scope", "TEST");

//        verify(contributor).setResultTree();
//        ArgumentCaptor<JtlWriter> jtlArgument = ArgumentCaptor.forClass(JtlWriter.class);
//        verify(contributor).setJtlWriter(jtlArgument.capture());
//        assertThat(jtlArgument.getValue().buildTestElement().getPropertyAsString("filename"))
//                .isEqualTo("other.jtl");
//        ArgumentCaptor<HtmlReporter> htmlArgument = ArgumentCaptor.forClass(HtmlReporter.class);
//        verify(contributor).setHtmlReporter(htmlArgument.capture());
//        assertThat(htmlArgument.getValue().buildTestElement().getPropertyAsString("filename"))
//                .isEqualTo("other.html");
//        ArgumentCaptor<InfluxDbBackendListener> influxArgument = ArgumentCaptor.forClass(InfluxDbBackendListener.class);
//        verify(contributor).setInfluxListener(influxArgument.capture());
//        Arguments args = invoke(influxArgument.getValue(), "buildListenerArguments", Arguments.class);
//        assertThat(args.getPropertyAsString("influxdbUrl")).isEqualTo("http://influx");
//        assertThat(args.getPropertyAsString("influxdbToken")).isEqualTo("s3cr3t1");
//        assertThat(args.getPropertyAsString("testTitle")).isEqualTo("title");
//        assertThat(args.getPropertyAsString("application")).isEqualTo("app");
//        assertThat(args.getPropertyAsString("measurement")).isEqualTo("measure");
//        assertThat(args.getPropertyAsString("samplersRegex")).isEqualTo(".*");
//        assertThat(args.getPropertyAsString("percentiles")).isEqualTo("1.1;2.4");
//        assertThat(args.getPropertyAsString("TAG_key")).isEqualTo("value");
//        ArgumentCaptor<GraphiteBackendListener> graphiteArgument = ArgumentCaptor.forClass(GraphiteBackendListener.class);
//        verify(contributor).setGraphiteListener(graphiteArgument.capture());
//        args = invoke(graphiteArgument.getValue(), "buildListenerArguments", Arguments.class);
//        assertThat(args.getPropertyAsString("graphiteHost")).isEqualTo("graph");
//        assertThat(args.getPropertyAsString("graphitePort")).isEqualTo("123");
//        assertThat(args.getPropertyAsString("rootMetricsPrefix")).isEqualTo("pre-");
    }

//    /**
//     * @see JMeterStepContributor#setRequest(String, String)
//     * @see JMeterStepContributor#executeSimple(Integer)
//     * @see JMeterStepContributor#assertLongMetric(Metric, Assertion)
//     */
//    @Test
//    public void testWhenDefaultsAndSmokeWithSuccess() throws IOException {
//        // prepare
//        mockServer(
//                request()
//                        .withPath("/")
//                        .withHeader(
//                                header("Content-Type", String.format("%s.*", MediaType.APPLICATION_JSON))
//                        )
//                , once(),
//                response()
//                        .withStatusCode(200)
//                        .withContentType(MediaType.APPLICATION_JSON)
//        );
//
//        // act
//        contributor.setRequest("GET", "/");
//        contributor.executeSimple(1);
//
//        // check
//        contributor.assertLongMetric(stats -> stats.errors().total(), new MatcherAssertion<>(equalTo(0L)));
//    }
//
//    @Test
//    public void testWhenDefaultsAndSmokeAndJsonExtractWithSuccess() throws IOException {
//        // prepare
//        mockServer(
//                request()
//                        .withPath("/")
//                        .withHeader(
//                                header("Content-Type", String.format("%s.*", MediaType.APPLICATION_JSON))
//                        )
//                , once(),
//                response()
//                        .withStatusCode(200)
//                        .withContentType(MediaType.APPLICATION_JSON)
//        );
//
//        // act
//        contributor.setRequest("GET", "/");
//        contributor.executeSimple(1);
//
//        // check
//        contributor.assertLongMetric(stats -> stats.errors().total(), new MatcherAssertion<>(equalTo(0L)));
//    }
//
//    private void mockServer(HttpRequest expected, Times times, HttpResponse response) {
//        client.when(expected, times).respond(response);
//    }
//
//    private DataTable dataTable(String... data) {
//        List<String[]> result = new LinkedList<>();
//        result.add(new String[]{"name", "value"});
//        for (int i = 0; i < data.length; i = i + 2) {
//            result.add(new String[]{data[i], data[i + 1]});
//        }
//        return new DataTable(result.toArray(new String[0][0]));
//    }
//
//    @SuppressWarnings("unchecked")
//    private Map<List<String>, String> keys() throws NoSuchFieldException, IllegalAccessException {
//        Field field = Oauth2ProviderConfig.class.getDeclaredField("cachedToken");
//        field.setAccessible(true);
//        return ((Map<List<String>, String>) field.get(null));
//    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(Object o, String name, Class<T> type) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method m = o.getClass().getDeclaredMethod(name, type);
        m.setAccessible(true);
        return (T) m.invoke(o);
    }
}
