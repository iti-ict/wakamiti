package es.iti.wakamiti.rest;

import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.datatypes.Assertion;
import es.iti.wakamiti.api.util.MatcherAssertion;
import imconfig.Configuration;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RestConfigContributorTest {
    private final RestConfigContributor configContributor = new RestConfigContributor();
    @Spy
    private RestStepContributor contributor;


    @Test
    @SuppressWarnings("unchecked")
    public void testDefaultConfig() {
        configContributor.configurer().configure(contributor, Configuration.factory().empty());

        verify(contributor, times(0)).setBaseURL(any());
        verify(contributor, times(0)).setContentType(any());
        verify(contributor, times(0)).setHttpCodeAssertion(any(Assertion.class));
        verify(contributor, times(0)).setTimeout(any());

        verify(contributor, times(0)).setMultipartSubtype(any());
        assertThat(RestAssured.config.getMultiPartConfig().defaultSubtype()).isEqualTo("form-data");
        assertThat(RestAssured.config.getRedirectConfig().followsRedirects()).isTrue();
        assertThat(RestAssured.config.getRedirectConfig().allowsCircularRedirects()).isFalse();
        assertThat(RestAssured.config.getRedirectConfig().rejectRelativeRedirects()).isFalse();
        assertThat(RestAssured.config.getRedirectConfig().maxRedirects()).isEqualTo(100);
    }

    @Test
    public void testFileConfig() throws MalformedURLException {
        Configuration configuration = Configuration.factory()
                .fromPath(Path.of("src/test/resources/test_config.yaml"))
                .inner("wakamiti");
        configContributor.configurer().configure(contributor, configuration);

        verify(contributor).setBaseURL(new URL("http://localhost:8080/api"));
        verify(contributor).setContentType("XML");
        verify(contributor).setHttpCodeAssertion(argThat(m ->
                m.description().equals(new MatcherAssertion<>(Matchers.lessThan(999)).description())));
        verify(contributor).setTimeout(Duration.ofMillis(10000));

        assertThat(contributor.oauth2Provider.configuration().clientId()).isEqualTo("WEB");
        assertThat(contributor.oauth2Provider.configuration().clientSecret()).isEqualTo("dhg3h89ec8h");
        assertThat(contributor.oauth2Provider.configuration().url()).isEqualTo(new URL("http://localhost:8080/token"));
        assertThat(contributor.oauth2Provider.configuration().parameters())
                .containsEntry("grant_type", "password")
                .containsEntry("username", "pepe")
                .containsEntry("password", "1234asdf")
                .containsEntry("scope", "something");

        verify(contributor).setMultipartSubtype("digest");
        assertThat(RestAssured.config.getRedirectConfig().followsRedirects()).isFalse();
        assertThat(RestAssured.config.getRedirectConfig().allowsCircularRedirects()).isTrue();
        assertThat(RestAssured.config.getRedirectConfig().rejectRelativeRedirects()).isTrue();
        assertThat(RestAssured.config.getRedirectConfig().maxRedirects()).isEqualTo(5);
    }

    @Test(expected = WakamitiException.class)
    public void testFileConfigWithError() {
        Configuration configuration = Configuration.factory().fromPairs("rest.config.multipart.subtype", "other");
        configContributor.configurer().configure(contributor, configuration);
    }
}
