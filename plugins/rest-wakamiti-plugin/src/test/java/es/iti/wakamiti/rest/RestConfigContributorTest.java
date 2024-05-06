package es.iti.wakamiti.rest;

import es.iti.wakamiti.api.auth.oauth.Oauth2ProviderConfig;
import imconfig.Configuration;
import io.restassured.RestAssured;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.datatypes.Assertion;
import es.iti.wakamiti.api.util.MatcherAssertion;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RestConfigContributorTest {
    private final RestConfigContributor configContributor = new RestConfigContributor();
    @Spy
    private RestStepContributor contributor;
    @Spy
    private Oauth2ProviderConfig oauth2Provider;

    @Before
    public void setup() {
        contributor.oauth2ProviderConfig = oauth2Provider;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDefaultConfig() {
        configContributor.configurer().configure(contributor, Configuration.factory().empty());

        verify(contributor, times(0)).setBaseURL(any());
        verify(contributor, times(0)).setContentType(any());
        verify(contributor, times(0)).setFailureHttpCodeAssertion(any(Assertion.class));
        verify(contributor, times(0)).setTimeout(any());

        verify(oauth2Provider, times(0)).clientId(any());
        verify(oauth2Provider, times(0)).clientSecret(any());
        verify(oauth2Provider, times(0)).url(any());
        verify(oauth2Provider, times(0)).cacheAuth(anyBoolean());
        verify(oauth2Provider, times(0)).addParameter(any(), any());

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
        verify(contributor).setFailureHttpCodeAssertion(argThat(m ->
                m.description().equals(new MatcherAssertion<>(Matchers.lessThan(999)).description())));
        verify(contributor).setTimeout(Duration.ofMillis(10000));

        assertThat(oauth2Provider.clientId()).isEqualTo("WEB");
        assertThat(oauth2Provider.clientSecret()).isEqualTo("dhg3h89ec8h");
        assertThat(oauth2Provider.url()).isEqualTo(new URL("http://localhost:8080/token"));

        verify(oauth2Provider).clientId("WEB");
        verify(oauth2Provider).clientSecret("dhg3h89ec8h");
        verify(oauth2Provider).url(new URL("http://localhost:8080/token"));
        verify(oauth2Provider).cacheAuth(true);
        verify(oauth2Provider).addParameter("grant_type", "password");
        verify(oauth2Provider).addParameter("username", "pepe");
        verify(oauth2Provider).addParameter("password", "1234asdf");
        verify(oauth2Provider).addParameter("scope", "something");

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
