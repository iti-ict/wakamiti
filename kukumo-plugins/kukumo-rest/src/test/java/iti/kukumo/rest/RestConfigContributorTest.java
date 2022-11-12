package iti.kukumo.rest;

import imconfig.Configuration;
import io.restassured.RestAssured;
import iti.kukumo.api.KukumoException;
import iti.kukumo.api.datatypes.Assertion;
import iti.kukumo.api.util.MatcherAssertion;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class RestConfigContributorTest {
    private final RestConfigContributor configContributor = new RestConfigContributor();
    @Spy
    private RestStepContributor contributor;

    @Test
    public void testDefaultConfig() {
        configContributor.configurer().configure(contributor, Configuration.factory().empty());

        verify(contributor, times(0)).setBaseURL(any());
        verify(contributor, times(0)).setContentType(any());
        verify(contributor, times(0)).setFailureHttpCodeAssertion(any(Assertion.class));

        assertThat(contributor.oauth2ProviderConfiguration.clientId()).isNull();
        assertThat(contributor.oauth2ProviderConfiguration.clientSecret()).isNull();
        assertThat(contributor.oauth2ProviderConfiguration.url()).isNull();
    }

    @Test
    public void testFileConfig() throws MalformedURLException {
        Configuration configuration = Configuration.factory()
                .fromPath(Path.of("src/test/resources/test_config.yaml"))
                .inner("kukumo");
        configContributor.configurer().configure(contributor, configuration);

        verify(contributor).setBaseURL(new URL("http://localhost:8080/api"));
        verify(contributor).setContentType("XML");
        verify(contributor).setFailureHttpCodeAssertion(argThat(m ->
                m.description().equals(new MatcherAssertion<>(Matchers.lessThan(999)).description())));

        assertThat(contributor.oauth2ProviderConfiguration.clientId()).isEqualTo("WEB");
        assertThat(contributor.oauth2ProviderConfiguration.clientSecret()).isEqualTo("dhg3h89ec8h");
        assertThat(contributor.oauth2ProviderConfiguration.url()).isEqualTo(new URL("http://localhost:8080/token"));
        assertThat(contributor.oauth2ProviderConfiguration.redirectUri()).isEqualTo(new URL("http://localhost:8080"));


//        assertThat(RestAssured.config.getMultiPartConfig().defaultControlName()).isEqualTo("other");
        assertThat(RestAssured.config.getMultiPartConfig().defaultSubtype()).isEqualTo("digest");
        assertThat(RestAssured.config.getRedirectConfig().followsRedirects()).isFalse();
        assertThat(RestAssured.config.getRedirectConfig().allowsCircularRedirects()).isTrue();
        assertThat(RestAssured.config.getRedirectConfig().rejectRelativeRedirects()).isTrue();
        assertThat(RestAssured.config.getRedirectConfig().maxRedirects()).isEqualTo(5);
    }

    @Test(expected = KukumoException.class)
    public void testFileConfigWithError() {
        Configuration configuration = Configuration.factory().fromPairs("rest.config.multipart.subtype", "other");
        configContributor.configurer().configure(contributor, configuration);
    }
}
