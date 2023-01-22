package iti.kukumo.rest;


import com.fasterxml.jackson.databind.JsonNode;
import io.restassured.RestAssured;
import iti.kukumo.api.KukumoException;
import iti.kukumo.api.plan.DataTable;
import iti.kukumo.api.plan.Document;
import iti.kukumo.api.util.JsonUtils;
import iti.kukumo.api.util.MatcherAssertion;
import iti.kukumo.rest.oauth.GrantType;
import iti.kukumo.rest.oauth.Oauth2ProviderConfig;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.matchers.Times;
import org.mockserver.model.*;
import org.mockserver.socket.tls.KeyStoreFactory;

import javax.net.ssl.HttpsURLConnection;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static iti.kukumo.rest.TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.Header.header;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.NottableString.not;
import static org.mockserver.model.Parameter.param;
import static org.mockserver.model.ParameterBody.params;
import static org.mockserver.model.RegexBody.regex;


@RunWith(MockitoJUnitRunner.class)
public class RestStepContributorTest {

    private static final Integer PORT = 1234;
    private static final String BASE_URL = MessageFormat.format("https://localhost:{0}", PORT.toString());
    private static final String TOKEN_PATH = "data/token";

    private static final ClientAndServer client = startClientAndServer(PORT);

    @Spy
    private RestStepContributor contributor;

    @BeforeClass
    public static void setup() {
        ConfigurationProperties.logLevel("OFF");
        HttpsURLConnection.setDefaultSSLSocketFactory(new KeyStoreFactory(
                new MockServerLogger()).sslContext().getSocketFactory());
    }

    @AfterClass
    public static void shutdown() {
        client.close();
    }

    @Before
    public void beforeEach() throws NoSuchFieldException, IllegalAccessException {
        RestAssured.reset();
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.config = RestAssured.config().multiPartConfig(
                RestAssured.config().getMultiPartConfig().defaultBoundary("asdf1234")
        );
        keys().clear();
        client.reset();
    }

    @Test
    public void testWithRequestParametersWithSuccess() throws MalformedURLException {
        // prepare
        mockServer(
                request()
                        .withPath("/users")
                        .withQueryStringParameters(
                                param("param1", "value1"),
                                param("param2", "value2")
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withHeaders(
                                header("vary", "Origin"),
                                header("vary", "Access-Control-Request-Method"),
                                header("vary", "Access-Control-Request-Headers")
                        )
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setRequestParameter("param1", "value1");
        contributor.setRequestParameter("param2", "value2");

        // act
        JsonNode result = (JsonNode) contributor.executeGetSubject();

        // check
        assertThat(result)
                .isNotNull()
                .isEqualTo(JsonUtils.json(Map.of(
                        "statusCode", 200,
                        "headers", Map.of(
                                "content-length", "0",
                                "connection", "keep-alive",
                                "vary", List.of(
                                        "Origin",
                                        "Access-Control-Request-Method",
                                        "Access-Control-Request-Headers"
                                )
                        ),
                        "body", "",
                        "statusLine", "HTTP/1.1 200 OK"
                )));
    }

    @Test
    public void testWithRequestParameterListWithSuccess() throws MalformedURLException {
        // prepare
        mockServer(
                request()
                        .withPath("/users")
                        .withQueryStringParameters(
                                param("param1", "value1"),
                                param("param2", "value2")
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setRequestParameters(dataTable("param1", "value1", "param2", "value2"));

        // act
        Object result = contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
    }

    @Test
    public void testWithQueryParametersWithSuccess() throws MalformedURLException {
        // prepare
        mockServer(
                request()
                        .withPath("/users")
                        .withQueryStringParameters(
                                param("param1", "value1"),
                                param("param2", "value2")
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setQueryParameter("param1", "value1");
        contributor.setQueryParameter("param2", "value2");

        // act
        Object result = contributor.executeGetQuery();

        // check
        assertThat(result).isNotNull();
    }

    @Test
    public void testWithQueryParameterListWithSuccess() throws MalformedURLException {
        // prepare
        mockServer(
                request()
                        .withPath("/users")
                        .withQueryStringParameters(
                                param("param1", "value1"),
                                param("param2", "value2")
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setQueryParameters(dataTable("param1", "value1", "param2", "value2"));

        // act
        Object result = contributor.executeGetQuery();

        // check
        assertThat(result).isNotNull();
    }

    @Test
    public void testWithPathParametersWithSuccess() throws MalformedURLException {
        // prepare
        mockServer(
                request()
                        .withPath("/users/10/list/4")
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users/{user}/list/{list}");
        contributor.setPathParameter("user", "10");
        contributor.setPathParameter("list", "4");

        // act
        Object result = contributor.executeGetQuery();

        // check
        assertThat(result).isNotNull();
    }

    @Test
    public void testWithPathParameterListWithSuccess() throws MalformedURLException {
        // prepare
        mockServer(
                request()
                        .withPath("/users/10/list/4")
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users/{user}/list/{list}");
        contributor.setPathParameters(dataTable("user", "10", "list", "4"));

        // act
        Object result = contributor.executeGetQuery();

        // check
        assertThat(result).isNotNull();
    }

    @Test
    public void testWithHeadersWithSuccess() throws MalformedURLException {
        // prepare
        mockServer(
                request()
                        .withPath("/users")
                        .withHeaders(
                                header("param1", "value1"),
                                header("param2", "value2")
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setHeader("param1", "value1");
        contributor.setHeader("param2", "value2");

        // act
        Object result = contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
    }

    @Test
    public void testWithHeaderListWithSuccess() throws MalformedURLException {
        // prepare
        mockServer(
                request()
                        .withPath("/users")
                        .withHeaders(
                                header("param1", "value1"),
                                header("param2", "value2")
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setHeaders(dataTable("param1", "value1", "param2", "value2"));

        // act
        Object result = contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
    }

    @Test(expected = SocketTimeoutException.class)
    public void testSetTimeoutWithError() throws MalformedURLException {
        // prepare
        mockServer(
                request()
                        .withPath("/users")
                ,
                response()
                        .withDelay(new Delay(TimeUnit.SECONDS, 5))
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setTimeoutInSecs(1);

        // act
        Object result = contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
    }

    @Test
    public void testSetBasicAuthWithSuccess() throws MalformedURLException {
        // prepare
        String token = Base64.getEncoder().encodeToString("username:password".getBytes());

        mockServer(
                request()
                        .withPath("/users")
                        .withHeader("Authorization", "Basic " + token)
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_XML)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setBasicAuth("username", "password");

        // act
        Object result = contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
    }

    @Test
    public void testSetBearerAuthClientWithSuccess() throws MalformedURLException, NoSuchFieldException, IllegalAccessException {
        // prepare
        String token = "1234567890";

        contributor.oauth2ProviderConfig.url(new URL(BASE_URL.concat("/token")));
        contributor.oauth2ProviderConfig.clientId("WEB_APP");
        contributor.oauth2ProviderConfig.clientSecret("ytv8923yy9234y96");

        mockServer(
                request()
                        .withPath("/token")
                        .withBody(
                                params(
                                        param("grant_type", "client_credentials")
                                )
                        )
                ,
                response(json(map("access_token", token)))
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(404)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setBearerAuthClient();

        // act
        contributor.executeGetSubject();

        // check
        assertThat(keys().values()).contains(token);
    }

    @Test
    public void testSetBearerAuthClientWhenScopeWithSuccess() throws MalformedURLException, NoSuchFieldException, IllegalAccessException {
        // prepare
        String token = "1234567890";

        contributor.oauth2ProviderConfig.url(new URL(BASE_URL.concat("/token")));
        contributor.oauth2ProviderConfig.clientId("WEB_APP");
        contributor.oauth2ProviderConfig.clientSecret("ytv8923yy9234y96");

        mockServer(
                request()
                        .withPath("/token")
                        .withBody(
                                params(
                                        param("grant_type", "client_credentials"),
                                        param("scope", "something")
                                )
                        )
                ,
                response(json(map("access_token", token)))
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(404)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setBearerAuthClient(dataTable("scope", "something"));

        // act
        contributor.executeGetSubject();

        // check
        assertThat(keys().values()).contains(token);
    }

    @Test
    public void testSetBearerAuthClientWhenCachedWithSuccess() throws MalformedURLException {
        // prepare
        String token = "1234567890";

        contributor.oauth2ProviderConfig.url(new URL(BASE_URL.concat("/token")));
        contributor.oauth2ProviderConfig.clientId("WEB_APP");
        contributor.oauth2ProviderConfig.clientSecret("ytv8923yy9234y96");
        contributor.oauth2ProviderConfig.cacheAuth(true);

        mockServer(
                request()
                        .withPath("/token")
                        .withBody(
                                params(
                                        param("grant_type", "client_credentials")
                                )
                        )
                ,
                response(json(map("access_token", token)))
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)

        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(404)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setBearerAuthClient();

        // act
        // If it calls the service more than once, it will throw an error
        contributor.executeGetSubject();
        contributor.executeGetSubject();

        // check
        verify(contributor, times(2)).retrieveOauthToken();
    }

    @Test
    public void testSetBearerAuthPasswordWithSuccess() throws MalformedURLException, NoSuchFieldException, IllegalAccessException {
        // prepare
        String token = "1234567890";

        contributor.oauth2ProviderConfig.url(new URL(BASE_URL.concat("/token")));
        contributor.oauth2ProviderConfig.clientId("WEB_APP");
        contributor.oauth2ProviderConfig.clientSecret("ytv8923yy9234y96");

        mockServer(
                request()
                        .withPath("/token")
                        .withBody(
                                params(
                                        param("grant_type", "password"),
                                        param("username", "username"),
                                        param("password", "password")
                                )
                        )
                ,
                response(json(map("access_token", token)))
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(404)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setBearerAuthPassword("username", "password");

        // act
        contributor.executeGetSubject();

        // check
        assertThat(keys().values()).contains(token);
    }

    @Test
    public void testSetBearerAuthPasswordWhenScopeWithSuccess() throws MalformedURLException, NoSuchFieldException, IllegalAccessException {
        // prepare
        String token = "1234567890";

        contributor.oauth2ProviderConfig.url(new URL(BASE_URL.concat("/token")));
        contributor.oauth2ProviderConfig.clientId("WEB_APP");
        contributor.oauth2ProviderConfig.clientSecret("ytv8923yy9234y96");

        mockServer(
                request()
                        .withPath("/token")
                        .withBody(
                                params(
                                        param("grant_type", "password"),
                                        param("username", "username"),
                                        param("password", "password"),
                                        param("scope", "something")
                                )
                        )
                ,
                response(json(map("access_token", token)))
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(404)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setBearerAuthPassword("username", "password", dataTable("scope", "something"));

        // act
        contributor.executeGetSubject();

        // check
        assertThat(keys().values()).contains(token);
    }

    @Test
    public void testSetBearerAuthPasswordWhenCachedWithSuccess() throws MalformedURLException {
        // prepare
        String token = "1234567890";

        contributor.oauth2ProviderConfig.url(new URL(BASE_URL.concat("/token")));
        contributor.oauth2ProviderConfig.clientId("WEB_APP");
        contributor.oauth2ProviderConfig.clientSecret("ytv8923yy9234y96");
        contributor.oauth2ProviderConfig.cacheAuth(true);

        mockServer(
                request()
                        .withPath("/token")
                        .withBody(
                                params(
                                        param("grant_type", "password"),
                                        param("username", "username"),
                                        param("password", "password")
                                )
                        )
                ,
                response(json(map("access_token", token)))
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(404)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setBearerAuthPassword("username", "password");

        // act
        // If it calls the service more than once, it will throw an error
        contributor.executeGetSubject();
        contributor.executeGetSubject();

        // check
        verify(contributor, times(2)).retrieveOauthToken();
    }

    @Test
    public void testSetNoneAuthWithSuccess() throws MalformedURLException {
        // prepare
        String token = "1234567890";

        contributor.oauth2ProviderConfig.url(new URL(BASE_URL.concat("/token")));
        contributor.oauth2ProviderConfig.clientId("WEB_APP");
        contributor.oauth2ProviderConfig.clientSecret("ytv8923yy9234y96");

        mockServer(
                request()
                        .withPath("/users")
                        .withHeader(
                                header(not("Authorization"))
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setBearerAuth(token);

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setNoneAuth(); // auth must be overridden

        // act
        Object result = contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
    }

    @Test(expected = KukumoException.class)
    public void testSetBearerAuthPasswordWhenNoGrantTypeConfigWithError() throws MalformedURLException {
        // prepare
        contributor.oauth2ProviderConfig.clientId("WEB_APP");
        contributor.oauth2ProviderConfig.clientSecret("ytv8923yy9234y96");
        contributor.oauth2ProviderConfig.url(new URL(BASE_URL.concat("/token")));
        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(404)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setBearerDefault();

        // act
        contributor.executeGetSubject();

        // check
        // An error should be thrown
    }

    @Test(expected = KukumoException.class)
    public void testSetBearerAuthPasswordWhenNoUrlConfigWithError() throws MalformedURLException {
        // prepare
        contributor.oauth2ProviderConfig.clientId("WEB_APP");
        contributor.oauth2ProviderConfig.clientSecret("ytv8923yy9234y96");
        contributor.setBearerAuthPassword("username", "password");
        contributor.setBaseURL(new URL(BASE_URL));

        // act
        contributor.executeGetSubject();

        // check
        // An error should be thrown
    }

    @Test(expected = KukumoException.class)
    public void testSetBearerAuthPasswordWhenNoClientIdConfigWithError() throws MalformedURLException {
        // prepare
        contributor.oauth2ProviderConfig.url(new URL(BASE_URL.concat("/token")));
        contributor.oauth2ProviderConfig.clientSecret("ytv8923yy9234y96");
        contributor.setBearerAuthPassword("username", "password");
        contributor.setBaseURL(new URL(BASE_URL));

        // act
        contributor.executeGetSubject();

        // check
        // An error should be thrown
    }

    @Test(expected = KukumoException.class)
    public void testSetBearerAuthPasswordWhenNoClientSecretConfigWithError() throws MalformedURLException {
        // prepare
        contributor.oauth2ProviderConfig.url(new URL(BASE_URL.concat("/token")));
        contributor.oauth2ProviderConfig.clientId("WEB_APP");
        contributor.setBearerAuthPassword("username", "password");
        contributor.setBaseURL(new URL(BASE_URL));

        // act
        contributor.executeGetSubject();

        // check
        // An error should be thrown
    }

    @Test(expected = KukumoException.class)
    public void testSetBearerAuthPasswordWhenNoRequiredParamConfigWithError() throws MalformedURLException {
        // prepare
        contributor.oauth2ProviderConfig.url(new URL(BASE_URL.concat("/token")));
        contributor.oauth2ProviderConfig.clientId("WEB_APP");
        contributor.oauth2ProviderConfig.type(GrantType.PASSWORD);
        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(404)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setBearerDefault();

        // act
        contributor.executeGetSubject();

        // check
        // An error should be thrown
    }

    @Test(expected = AssertionError.class)
    public void testSetBearerAuthPasswordWhenCodeErrorWithError() throws MalformedURLException {
        // prepare
        contributor.oauth2ProviderConfig.url(new URL(BASE_URL.concat("/token")));
        contributor.oauth2ProviderConfig.clientId("WEB_APP");
        contributor.oauth2ProviderConfig.clientSecret("ytv8923yy9234y96");

        mockServer(
                request()
                        .withPath("/token")
                ,
                response()
                        .withStatusCode(400)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setBearerAuthPassword("username", "password");

        // act
        contributor.executeGetSubject();

        // check
        // An error should be thrown
    }

    @Test(expected = AssertionError.class)
    public void testSetBearerAuthPasswordWhenTokenMissingWithError() throws MalformedURLException {
        // prepare
        contributor.oauth2ProviderConfig.url(new URL(BASE_URL.concat("/token")));
        contributor.oauth2ProviderConfig.clientId("WEB_APP");
        contributor.oauth2ProviderConfig.clientSecret("ytv8923yy9234y96");

        mockServer(
                request()
                        .withPath("/token")
                ,
                response(json(map("other", "123")))
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setBearerAuthPassword("username", "password");

        // act
        contributor.executeGetSubject();

        // check
        // An error should be thrown
    }

    @Test
    public void testWhenAuthHeaderWithSuccess() throws MalformedURLException {
        // prepare
        String token = "1234567890";

        mockServer(
                request()
                        .withPath("/users/10")
                        .withHeader("Authorization", "Bearer " + token)
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setBearerAuthFile(file(TOKEN_PATH));
        contributor.setSubject("10");

        // act
        Object result = contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
    }

    @Test
    public void testSetAttachedFileWithSuccess() throws MalformedURLException {
        // prepare
        mockServer(
                request()
                        .withPath("/users")
                        .withHeader("Content-Type",
                                MediaType.MULTIPART_FORM_DATA + "; boundary="
                                        + RestAssured.config().getMultiPartConfig().defaultBoundary())
                        .withBody(
                                attached(
                                        file(
                                                RestAssured.config().getMultiPartConfig().defaultSubtype(),
                                                MediaType.TEXT_PLAIN.toString(),
                                                RestAssured.config().getMultiPartConfig().defaultControlName(),
                                                "Test content"
                                        ),
                                        file(
                                                RestAssured.config().getMultiPartConfig().defaultSubtype(),
                                                MediaType.TEXT_PLAIN.toString(),
                                                RestAssured.config().getMultiPartConfig().defaultControlName(),
                                                "Test content 2"
                                        )
                                )
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setAttachedFile("file", new Document("Test content"));
        contributor.setAttachedFile("file", new Document("Test content 2"));

        // act
        Object result = contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
    }

    @Test
    public void testSetAttachedFileWhenContentTypeWithSuccess() throws MalformedURLException {
        // prepare
        mockServer(
                request()
                        .withPath("/users")
                        .withHeader("Content-Type",
                                MediaType.MULTIPART_FORM_DATA + "; boundary="
                                        + RestAssured.config().getMultiPartConfig().defaultBoundary())
                        .withBody(
                                attached(
                                        file(
                                                RestAssured.config().getMultiPartConfig().defaultSubtype(),
                                                MediaType.APPLICATION_JSON.toString(),
                                                "fichero",
                                                json(map("user", "Pepe"))
                                        )
                                )
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setAttachedFile("fichero",
                new Document(json(map("user", "Pepe")), "json"));

        // act
        Object result = contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
    }

    @Test
    public void testSetAttachedFileWhenSubtypeWithSuccess() throws MalformedURLException {
        // prepare
        mockServer(
                request()
                        .withPath("/users")
                        .withHeader("Content-Type",
                                "multipart/mixed; boundary="
                                        + RestAssured.config().getMultiPartConfig().defaultBoundary())
                        .withBody(
                                regex(".+")
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setMultipartSubtype("mixed");
        contributor.setAttachedFile("file", new Document("Test content"));

        // act
        Object result = contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
    }

    @Test
    public void testSetAttachedFileWhenFileWithSuccess() throws MalformedURLException {
        // prepare
        mockServer(
                request()
                        .withPath("/users")
                        .withHeader("Content-Type",
                                MediaType.MULTIPART_FORM_DATA
                                        + "; boundary=" + RestAssured.config().getMultiPartConfig().defaultBoundary())
                        .withBody(
                                attached(
                                        file(
                                                RestAssured.config().getMultiPartConfig().defaultSubtype(),
                                                MediaType.TEXT_PLAIN.toString(),
                                                RestAssured.config().getMultiPartConfig().defaultControlName(),
                                                "1234567890"
                                        )
                                )
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setAttachedFile("file", file(TOKEN_PATH));

        // act
        Object result = contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
    }

    @Test
    public void testWhenDeleteSubjectWithSuccess() throws MalformedURLException {
        // prepare
        mockServer(
                request()
                        .withMethod("DELETE")
                        .withPath("/users/10")
                ,
                response()
                        .withStatusCode(204)
                        .withContentType(MediaType.APPLICATION_XML)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(204)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setSubject("10");

        // act
        Object result = contributor.executeDeleteSubject();

        // check
        assertThat(result).isNotNull();
    }

    @Test
    public void testWhenPutSubjectDocumentWithSuccess() throws MalformedURLException {
        // prepare
        mockServer(
                request()
                        .withMethod("PUT")
                        .withPath("/users/10")
                        .withBody(json(map("user", "Pepe")))
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setSubject("10");

        // act
        Object result = contributor.executePutSubjectUsingDocument(new Document(json(map("user", "Pepe"))));

        // check
        assertThat(result).isNotNull();
    }

    @Test
    public void testWhenPutSubjectFileWithSuccess() throws MalformedURLException {
        // prepare
        mockServer(
                request()
                        .withMethod("PUT")
                        .withPath("/users/10")
                        .withBody("1234567890")
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setSubject("10");

        // act
        Object result = contributor.executePutSubjectUsingFile(file(TOKEN_PATH));

        // check
        assertThat(result).isNotNull();
    }

    @Test
    public void testWhenPutSubjectAndParamsWithSuccess() throws MalformedURLException {
        // prepare
        mockServer(
                request()
                        .withMethod("PUT")
                        .withPath("/users/10")
                        .withQueryStringParameter("param1", "value1")
                        .withBody(
                                Not.not(regex(".+"))
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setSubject("10");
        contributor.setRequestParameter("param1", "value1");

        // act
        Object result = contributor.executePutSubject();

        // check
        assertThat(result).isNotNull();
    }

    @Test
    public void testWhenPutSubjectWithSuccess() throws MalformedURLException {
        // prepare
        mockServer(
                request()
                        .withMethod("PUT")
                        .withPath("/users/10")
                        .withBody(
                                Not.not(regex(".+"))
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setSubject("10");

        // act
        Object result = contributor.executePutSubject();

        // check
        assertThat(result).isNotNull();
    }

    @Test
    public void testWhenPatchSubjectDocumentWithSuccess() throws MalformedURLException {
        // prepare
        mockServer(
                request()
                        .withMethod("PATCH")
                        .withPath("/users/10")
                        .withBody(json(map("user", "Pepe")))
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setSubject("10");

        // act
        Object result = contributor
                .executePatchSubjectUsingDocument(new Document(json(map("user", "Pepe"))));

        // check
        assertThat(result).isNotNull();
    }

    @Test
    public void testWhenPatchSubjectFileWithSuccess() throws MalformedURLException {
        // prepare
        mockServer(
                request()
                        .withMethod("PATCH")
                        .withPath("/users/10")
                        .withBody("1234567890")
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setSubject("10");

        // act
        Object result = contributor.executePatchSubjectUsingFile(file(TOKEN_PATH));

        // check
        assertThat(result).isNotNull();
    }

    @Test
    public void testWhenPatchSubjectAndParamsWithSuccess() throws MalformedURLException {
        // prepare
        mockServer(
                request()
                        .withMethod("PATCH")
                        .withPath("/users/10")
                        .withQueryStringParameter("param1", "value1")
                        .withBody(
                                Not.not(regex(".+"))
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setSubject("10");
        contributor.setRequestParameter("param1", "value1");

        // act
        Object result = contributor.executePatchSubject();

        // check
        assertThat(result).isNotNull();
    }

    @Test
    public void testWhenPatchSubjectWithSuccess() throws MalformedURLException {
        // prepare
        mockServer(
                request()
                        .withMethod("PATCH")
                        .withPath("/users/10")
                        .withBody(
                                Not.not(regex(".+"))
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setSubject("10");

        // act
        Object result = contributor.executePatchSubject();

        // check
        assertThat(result).isNotNull();
    }

    @Test
    public void testWhenPostSubjectDocumentWithSuccess() throws MalformedURLException {
        // prepare
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/users/10")
                        .withBody(json(map("user", "Pepe")))
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setSubject("10");

        // act
        Object result = contributor
                .executePostSubjectUsingDocument(new Document(json(map("user", "Pepe"))));

        // check
        assertThat(result).isNotNull();
    }

    @Test
    public void testWhenPostSubjectFileWithSuccess() throws MalformedURLException {
        // prepare
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/users/10")
                        .withBody("1234567890")
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setSubject("10");

        // act
        Object result = contributor.executePostSubjectUsingFile(file(TOKEN_PATH));

        // check
        assertThat(result).isNotNull();
    }

    @Test
    public void testWhenPostSubjectAndParamWithSuccess() throws MalformedURLException {
        // prepare
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/users/10")
                        .withBody(
                                params(param("param1", "value1"))
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setSubject("10");
        contributor.setRequestParameter("param1", "value1");

        // act
        Object result = contributor.executePostSubject();

        // check
        assertThat(result).isNotNull();
    }

    @Test
    public void testWhenPostSubjectWithSuccess() throws MalformedURLException {
        // prepare
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/users/10")
                        .withBody(
                                Not.not(regex(".+"))
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setSubject("10");

        // act
        Object result = contributor.executePostSubject();

        // check
        assertThat(result).isNotNull();
    }

    @Test
    public void testWhenPostDataDocumentWithSuccess() throws MalformedURLException {
        // prepare
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/users/10")
                        .withBody(json(map("user", "Pepe")))
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setSubject("10");

        // act
        Object result = contributor.executePostDataUsingDocument(new Document(json(map("user", "Pepe"))));

        // check
        assertThat(result).isNotNull();
    }

    @Test
    public void testWhenPostDataFileWithSuccess() throws MalformedURLException {
        // prepare
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/users/10")
                        .withBody("1234567890")
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setSubject("10");

        // act
        Object result = contributor.executePostDataUsingFile(file(TOKEN_PATH));

        // check
        assertThat(result).isNotNull();
    }

    @Test
    public void testWhenPostDataAndParamWithSuccess() throws MalformedURLException {
        // prepare
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/users/10")
                        .withBody(
                                params(param("param1", "value1"))
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setSubject("10");
        contributor.setRequestParameter("param1", "value1");

        // act
        Object result = contributor.executePostData();

        // check
        assertThat(result).isNotNull();
    }

    @Test
    public void testWhenPostDataWithSuccess() throws MalformedURLException {
        // prepare
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/users/10")
                        .withBody(
                                Not.not(regex(".+"))
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setFailureHttpCodeAssertion(new MatcherAssertion<>(equalTo(200)));
        contributor.setBaseURL(new URL(BASE_URL));
        contributor.setService("/users");
        contributor.setSubject("10");

        // act
        Object result = contributor.executePostData();

        // check
        assertThat(result).isNotNull();
    }

    @Test(expected = KukumoException.class)
    public void testWhenResponseIsNullWithError() {
        contributor.assertHttpCode(new MatcherAssertion<>(equalTo(200)));
    }


    private void mockServer(HttpRequest expected, HttpResponse response) {
        client.when(expected, Times.once()).respond(response);
    }

    private DataTable dataTable(String... data) {
        List<String[]> result = new LinkedList<>();
        result.add(new String[]{"name", "value"});
        for (int i = 0; i < data.length; i = i + 2) {
            result.add(new String[]{data[i], data[i + 1]});
        }
        return new DataTable(result.toArray(new String[0][0]));
    }

    @SuppressWarnings("unchecked")
    private Map<List<String>, String> keys() throws NoSuchFieldException, IllegalAccessException {
        Field field = Oauth2ProviderConfig.class.getDeclaredField("cachedToken");
        field.setAccessible(true);
        return ((Map<List<String>, String>) field.get(null));
    }

}
