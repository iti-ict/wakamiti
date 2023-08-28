package es.iti.wakamiti.rest;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.plan.DataTable;
import es.iti.wakamiti.api.plan.Document;
import es.iti.wakamiti.api.util.JsonUtils;
import es.iti.wakamiti.api.util.MatcherAssertion;
import es.iti.wakamiti.api.util.XmlUtils;
import es.iti.wakamiti.rest.oauth.GrantType;
import es.iti.wakamiti.rest.oauth.Oauth2ProviderConfig;
import io.restassured.RestAssured;
import org.apache.xmlbeans.XmlObject;
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
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static es.iti.wakamiti.rest.TestUtil.*;
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
    private static final String TOKEN_PATH = "data/token.txt";

    private static final ClientAndServer client = startClientAndServer(PORT);

    private final RestConfigContributor configurator = new RestConfigContributor();
    @Spy
    private RestStepContributor contributor;

    @BeforeClass
    public static void setup() {
        ConfigurationProperties.logLevel("DEBUG");
        HttpsURLConnection.setDefaultSSLSocketFactory(new KeyStoreFactory(
                new MockServerLogger()).sslContext().getSocketFactory());
    }

    @AfterClass
    public static void shutdown() {
        client.close();
    }

    @Before
    public void beforeEach() throws NoSuchFieldException, IllegalAccessException {
        configurator.configurer().configure(contributor, configurator.defaultConfiguration().appendFromPairs(
                RestConfigContributor.BASE_URL, BASE_URL
        ));
        RestAssured.config = RestAssured.config().multiPartConfig(
                RestAssured.config().getMultiPartConfig().defaultBoundary("asdf1234")
        );
        keys().clear();
        client.reset();
    }


    @Test
    public void testWhenDefaultsWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withPath("/")
                        .withHeader(
                                header("Content-Type", String.format("%s.*", MediaType.APPLICATION_JSON))
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        JsonNode result = (JsonNode) contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testSetContentTypeWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withPath("/")
                        .withHeader(
                                header("Content-Type", String.format("%s.*", MediaType.APPLICATION_XML))
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        contributor.setContentType("XML");
        JsonNode result = (JsonNode) contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testJsonResponseWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withPath("/")
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"name\":\"Susan\",\"ape1\":\"Martin\"}")
                        .withHeaders(
                                header("vary", "Origin"),
                                header("vary", "Access-Control-Request-Method"),
                                header("vary", "Access-Control-Request-Headers")
                        )
        );

        // act
        JsonNode result = (JsonNode) contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
        assertThat(JsonUtils.readStringValue(result, "body.name")).isEqualTo("Susan");
    }

    @Test
    public void testJsonResponseWhenNullBodyWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withPath("/")
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        JsonNode result = (JsonNode) contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
        assertThat(JsonUtils.readStringValue(result, "body")).isNull();
    }

    @Test
    public void testXmlResponseWithSuccess() {
        // prepare
        mockServer(
                request()
                        .withPath("/")
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_XML)
                        .withBody("<item><name>Susan</name><ape1>Martin</ape1></item>")
                        .withHeaders(
                                header("vary", "Origin"),
                                header("vary", "Access-Control-Request-Method"),
                                header("vary", "Access-Control-Request-Headers")
                        )
        );

        // act
        XmlObject result = (XmlObject) contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
        assertThat(XmlUtils.readStringValue(result, "statusCode")).isEqualTo("200");
        assertThat(XmlUtils.readStringValue(result, "body.item.name")).isEqualTo("Susan");
    }

    @Test
    public void testXmlResponseWhenNullBodyWithSuccess() {
        // prepare
        mockServer(
                request()
                        .withPath("/")
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_XML)
        );

        // act
        XmlObject result = (XmlObject) contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
        assertThat(XmlUtils.readStringValue(result, "statusCode")).isEqualTo("200");
        assertThat(XmlUtils.readStringValue(result, "body")).isNull();
    }

    @Test
    public void testTextResponseWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withPath("/")
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.TEXT_PLAIN)
                        .withBody("5567")
                        .withHeaders(
                                header("vary", "Origin"),
                                header("vary", "Access-Control-Request-Method"),
                                header("vary", "Access-Control-Request-Headers")
                        )
        );

        // act
        JsonNode result = (JsonNode) contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
        assertThat(JsonUtils.readStringValue(result, "body")).isEqualTo("5567");
    }

    @Test
    public void testTextResponseWhenNullBodyWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withPath("/")
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.TEXT_PLAIN)
        );

        // act
        JsonNode result = (JsonNode) contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
        assertThat(JsonUtils.readStringValue(result, "body")).isNull();
    }

    @Test
    public void testRequestParametersWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withPath("/")
                        .withQueryStringParameters(
                                param("param1", "value1"),
                                param("param2", "value2")
                        )
                ,
                response()
                        .withStatusCode(200)
        );

        // act
        contributor.setRequestParameter("param1", "value1");
        contributor.setRequestParameter("param2", "value2");
        JsonNode result = (JsonNode) contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testRequestParameterListWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withPath("/")
                        .withQueryStringParameters(
                                param("param1", "value1"),
                                param("param2", "value2")
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        contributor.setRequestParameters(dataTable("param1", "value1", "param2", "value2"));
        JsonNode result = (JsonNode) contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testQueryParametersWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withPath("/")
                        .withQueryStringParameters(
                                param("param1", "value1"),
                                param("param2", "value2")
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        contributor.setQueryParameter("param1", "value1");
        contributor.setQueryParameter("param2", "value2");
        JsonNode result = (JsonNode) contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testQueryParameterListWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withPath("/")
                        .withQueryStringParameters(
                                param("param1", "value1"),
                                param("param2", "value2")
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        contributor.setQueryParameters(dataTable("param1", "value1", "param2", "value2"));
        JsonNode result = (JsonNode) contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testPathParametersWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withPath("/users/10/list/4")
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        contributor.setService("users/{user}/list/{list}");
        contributor.setPathParameter("user", "10");
        contributor.setPathParameter("list", "4");
        JsonNode result = (JsonNode) contributor.executeGetQuery();

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testPathParameterListWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withPath("/users/10/list/4")
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        contributor.setService("/users/{user}/list/{list}");
        contributor.setPathParameters(dataTable("user", "10", "list", "4"));
        JsonNode result = (JsonNode) contributor.executeGetQuery();

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test(expected = WakamitiException.class)
    public void testPathParameterListWhenIncorrectColumnsWithError() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withPath("/users/10/list/4")
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        contributor.setService("/users/{user}/list/{list}");
        contributor.setPathParameters(new DataTable(new String[][]{
                new String[] { "column1" }, new String[] { "value1" }
        }));
        contributor.executeGetQuery();

        // check
        // An error should be thrown
    }

    @Test
    public void testHeadersWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withPath("/")
                        .withHeaders(
                                header("param1", "value1"),
                                header("param2", "value2")
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        contributor.setHeader("param1", "value1");
        contributor.setHeader("param2", "value2");
        JsonNode result = (JsonNode) contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testHeaderListWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withPath("/")
                        .withHeaders(
                                header("param1", "value1"),
                                header("param2", "value2")
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        contributor.setHeaders(dataTable("param1", "value1", "param2", "value2"));
        JsonNode result = (JsonNode) contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test(expected = WakamitiException.class)
    public void testHeaderListWhenIncorrectColumnsWithError() {
        // act
        contributor.setHeaders(
                new DataTable(new String[][]{
                        new String[] { "column1" }, new String[] { "value1" }
                })
        );
        contributor.executeGetSubject();

        // check
        // An error should be thrown
    }

    @Test(expected = SocketTimeoutException.class)
    public void testSetTimeoutWithError() {
        // prepare
        mockServer(
                request()
                        .withPath("/")
                ,
                response()
                        .withDelay(new Delay(TimeUnit.SECONDS, 5))
        );

        // act
        contributor.setTimeoutInSecs(1);
        contributor.executeGetSubject();

        // check
        // An error should be thrown
    }

    @Test
    public void testSetBasicAuthWithSuccess() {
        // prepare
        String token = Base64.getEncoder().encodeToString("username:password".getBytes());

        mockServer(
                request()
                        .withPath("/")
                        .withHeader("Authorization", "Basic " + token)
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_XML)
        );

        // act
        contributor.setBasicAuth("username", "password");
        XmlObject result = (XmlObject) contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
        assertThat(XmlUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testSetBearerAuthClientWithSuccess()
            throws NoSuchFieldException, IllegalAccessException, JsonProcessingException, MalformedURLException {
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

        // act
        contributor.setBearerAuthClient();
        JsonNode result = (JsonNode) contributor.executeGetSubject();

        // check
        assertThat(keys()).containsValue(token);
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("404");
    }

    @Test
    public void testSetBearerAuthClientWhenScopeWithSuccess()
            throws MalformedURLException, NoSuchFieldException, IllegalAccessException, JsonProcessingException {
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

        // act
        contributor.setBearerAuthClient(dataTable("scope", "something"));
        JsonNode result = (JsonNode) contributor.executeGetSubject();

        // check
        assertThat(keys()).containsValue(token);
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("404");
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

        // act
        contributor.setBearerAuthClient();
        // If it calls the service more than once, it will throw an error
        contributor.executeGetSubject();
        contributor.executeGetSubject();

        // check
        verify(contributor, times(2)).retrieveOauthToken();
    }

    @Test
    public void testSetBearerAuthPasswordWithSuccess()
            throws MalformedURLException, NoSuchFieldException, IllegalAccessException, JsonProcessingException {
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

        // act
        contributor.setBearerAuthPassword("username", "password");
        JsonNode result = (JsonNode) contributor.executeGetSubject();

        // check
        assertThat(keys()).containsValue(token);
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("404");
    }

    @Test
    public void testSetBearerAuthPasswordWhenScopeWithSuccess()
            throws MalformedURLException, NoSuchFieldException, IllegalAccessException, JsonProcessingException {
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

        // act
        contributor.setBearerAuthPassword("username", "password", dataTable("scope", "something"));
        JsonNode result = (JsonNode) contributor.executeGetSubject();

        // check
        assertThat(keys()).containsValue(token);
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("404");
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

        // act
        contributor.setBearerAuthPassword("username", "password");
        // If it calls the service more than once, it will throw an error
        contributor.executeGetSubject();
        contributor.executeGetSubject();

        // check
        verify(contributor, times(2)).retrieveOauthToken();
    }

    @Test
    public void testSetNoneAuthWithSuccess() throws MalformedURLException, JsonProcessingException {
        // prepare
        String token = "1234567890";

        contributor.oauth2ProviderConfig.url(new URL(BASE_URL.concat("/token")));
        contributor.oauth2ProviderConfig.clientId("WEB_APP");
        contributor.oauth2ProviderConfig.clientSecret("ytv8923yy9234y96");

        mockServer(
                request()
                        .withPath("/")
                        .withHeader(
                                header(not("Authorization"))
                        )
                ,
                response()
                        .withStatusCode(401)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        //contributor.setBearerAuth(token);
        contributor.setHeader("Authorization", "loquesea");

        // act
        contributor.setNoneAuth(); // auth must be overridden
        JsonNode result = (JsonNode) contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("401");
    }

    @Test(expected = WakamitiException.class)
    public void testSetBearerAuthPasswordWhenNoGrantTypeConfigWithError() throws MalformedURLException {
        // prepare
        contributor.oauth2ProviderConfig.clientId("WEB_APP");
        contributor.oauth2ProviderConfig.clientSecret("ytv8923yy9234y96");
        contributor.oauth2ProviderConfig.url(new URL(BASE_URL.concat("/token")));

        // act
        contributor.setBearerDefault();
        contributor.executeGetSubject();

        // check
        // An error should be thrown
    }

    @Test(expected = WakamitiException.class)
    public void testSetBearerAuthPasswordWhenNoUrlConfigWithError() {
        // prepare
        contributor.oauth2ProviderConfig.clientId("WEB_APP");
        contributor.oauth2ProviderConfig.clientSecret("ytv8923yy9234y96");

        // act
        contributor.setBearerAuthPassword("username", "password");
        contributor.executeGetSubject();

        // check
        // An error should be thrown
    }

    @Test(expected = WakamitiException.class)
    public void testSetBearerAuthPasswordWhenNoClientIdConfigWithError() throws MalformedURLException {
        // prepare
        contributor.oauth2ProviderConfig.url(new URL(BASE_URL.concat("/token")));
        contributor.oauth2ProviderConfig.clientSecret("ytv8923yy9234y96");

        // act
        contributor.setBearerAuthPassword("username", "password");
        contributor.executeGetSubject();

        // check
        // An error should be thrown
    }

    @Test(expected = WakamitiException.class)
    public void testSetBearerAuthPasswordWhenNoClientSecretConfigWithError() throws MalformedURLException {
        // prepare
        contributor.oauth2ProviderConfig.url(new URL(BASE_URL.concat("/token")));
        contributor.oauth2ProviderConfig.clientId("WEB_APP");

        // act
        contributor.setBearerAuthPassword("username", "password");
        contributor.executeGetSubject();

        // check
        // An error should be thrown
    }

    @Test(expected = WakamitiException.class)
    public void testSetBearerAuthPasswordWhenNoRequiredParamConfigWithError() throws MalformedURLException {
        // prepare
        contributor.oauth2ProviderConfig.url(new URL(BASE_URL.concat("/token")));
        contributor.oauth2ProviderConfig.clientId("WEB_APP");
        contributor.oauth2ProviderConfig.type(GrantType.PASSWORD);

        // act
        contributor.setBearerDefault();
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

        // act
        contributor.setBearerAuthPassword("username", "password");
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

        contributor.setBearerAuthPassword("username", "password");

        // act
        contributor.executeGetSubject();

        // check
        // An error should be thrown
    }

    @Test
    public void testWhenAuthHeaderWithSuccess() throws JsonProcessingException {
        // prepare
        String token = "1234567890";

        mockServer(
                request()
                        .withPath("/")
                        .withHeader("Authorization", "Bearer " + token)
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        contributor.setBearerAuthFile(file(TOKEN_PATH));
        JsonNode result = (JsonNode) contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testSetAttachedFileWithSuccess() throws IOException {
        // prepare
        mockServer(
                request()
                        .withPath("/")
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
                        .withBody(
                                regex(".*file\\.txt.*")
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        contributor.setAttachedFile("file", new Document("Test content"));
        contributor.setAttachedFile("file", new Document("Test content 2"));
        JsonNode result = (JsonNode) contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testSetAttachedFileWhenContentTypeWithSuccess() throws IOException {
        // prepare
        mockServer(
                request()
                        .withPath("/")
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
                        .withBody(
                                regex(".*file\\.json.*")
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        contributor.setAttachedFile("fichero",
                new Document(json(map("user", "Pepe")), "json"));
        JsonNode result = (JsonNode) contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testSetAttachedFileWhenSubtypeWithSuccess() throws IOException {
        // prepare
        mockServer(
                request()
                        .withPath("/")
                        .withHeader("Content-Type",
                                "multipart/mixed; boundary="
                                        + RestAssured.config().getMultiPartConfig().defaultBoundary())
                        .withBody(
                                regex(".*")
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        contributor.setMultipartSubtype("mixed");
        contributor.setAttachedFile("file", new Document("Test content"));
        JsonNode result = (JsonNode) contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testSetAttachedFileWhenFilenameWithSuccess() throws IOException {
        // prepare
        mockServer(
                request()
                        .withPath("/")
                        .withHeader("Content-Type",
                                MediaType.MULTIPART_FORM_DATA + "; boundary="
                                        + RestAssured.config().getMultiPartConfig().defaultBoundary())
                        .withBody(
                                regex(".*fichero\\.txt.*")
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        contributor.setFilename("fichero");
        contributor.setAttachedFile("file", new Document("Test content"));
        JsonNode result = (JsonNode) contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testSetAttachedFileWhenFileWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withPath("/")
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
                        .withBody(
                                regex(".*token\\.txt.*")
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        contributor.setAttachedFile("file", file(TOKEN_PATH));
        JsonNode result = (JsonNode) contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test(expected = WakamitiException.class)
    public void testSetAttachedFileWhenFileNotFoundWithError() {
        // act
        contributor.setAttachedFile("file", new File("file.tmp"));

        // check
        // An error should be thrown
    }

    @Test
    public void testWithFormParametersWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withPath("/")
                        .withHeader(
                                header("Content-Type",
                                        String.format("%s.*", MediaType.APPLICATION_FORM_URLENCODED))
                        )
                        .withQueryStringParameters(
                                param("param1", "value1"),
                                param("param2", "value2")
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        contributor.setFormParameter("param1", "value1");
        contributor.setFormParameter("param2", "value2");
        JsonNode result = (JsonNode) contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testWithFormParametersListWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withPath("/")
                        .withHeader(
                                header("Content-Type",
                                        String.format("%s.*", MediaType.APPLICATION_FORM_URLENCODED))
                        )
                        .withQueryStringParameters(
                                param("param1", "value1"),
                                param("param2", "value2")
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        contributor.setFormParameters(dataTable("param1", "value1", "param2", "value2"));
        JsonNode result = (JsonNode) contributor.executeGetSubject();

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testDeleteSubjectWithSuccess() {
        // prepare
        mockServer(
                request()
                        .withMethod("DELETE")
                        .withPath("/10")
                ,
                response()
                        .withStatusCode(204)
                        .withContentType(MediaType.APPLICATION_XML)
        );

        // act
        contributor.setSubject("/10");
        XmlObject result = (XmlObject) contributor.executeDeleteSubject();

        // check
        assertThat(result).isNotNull();
        assertThat(XmlUtils.readStringValue(result, "statusCode")).isEqualTo("204");
    }

    @Test
    public void testDeleteDataWhenDocumentWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withMethod("DELETE")
                        .withBody(json(map("user", "Pepe")))
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        JsonNode result = (JsonNode) contributor
                .executeDeleteDataUsingDocument(new Document(json(map("user", "Pepe"))));

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testDeleteDataWhenFileWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withMethod("DELETE")
                        .withBody("1234567890")
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        JsonNode result = (JsonNode) contributor.executeDeleteDataUsingFile(file(TOKEN_PATH));

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testWhenPutSubjectDocumentWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withMethod("PUT")
                        .withPath("/10")
                        .withBody(json(map("user", "Pepe")))
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        contributor.setSubject("10");
        JsonNode result = (JsonNode) contributor.executePutSubjectUsingDocument(
                new Document(json(map("user", "Pepe"))));

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testWhenPutSubjectFileWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withMethod("PUT")
                        .withPath("/10")
                        .withBody("1234567890")
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setSubject("10");

        // act
        JsonNode result = (JsonNode) contributor.executePutSubjectUsingFile(file(TOKEN_PATH));

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testWhenPutSubjectAndParamsWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withMethod("PUT")
                        .withPath("/10")
                        .withQueryStringParameter("param1", "value1")
                        .withBody(
                                Not.not(regex(".+"))
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setSubject("10");
        contributor.setRequestParameter("param1", "value1");

        // act
        JsonNode result = (JsonNode) contributor.executePutSubject();

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testWhenPutSubjectWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withMethod("PUT")
                        .withPath("/10")
                        .withBody(
                                Not.not(regex(".+"))
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setSubject("10");

        // act
        JsonNode result = (JsonNode) contributor.executePutSubject();

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testWhenPatchSubjectDocumentWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withMethod("PATCH")
                        .withPath("/10")
                        .withBody(json(map("user", "Pepe")))
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        contributor.setSubject("10");
        JsonNode result = (JsonNode) contributor
                .executePatchSubjectUsingDocument(new Document(json(map("user", "Pepe"))));

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testWhenPatchSubjectFileWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withMethod("PATCH")
                        .withPath("/10")
                        .withBody("1234567890")
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        contributor.setSubject("10");
        JsonNode result = (JsonNode) contributor.executePatchSubjectUsingFile(file(TOKEN_PATH));

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testWhenPatchSubjectAndParamsWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withMethod("PATCH")
                        .withPath("/10")
                        .withQueryStringParameter("param1", "value1")
                        .withBody(
                                Not.not(regex(".+"))
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        contributor.setSubject("10");
        contributor.setRequestParameter("param1", "value1");
        JsonNode result = (JsonNode) contributor.executePatchSubject();

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testWhenPatchSubjectWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withMethod("PATCH")
                        .withPath("/10")
                        .withBody(
                                Not.not(regex(".+"))
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );


        // act
        contributor.setSubject("10");
        JsonNode result = (JsonNode) contributor.executePatchSubject();

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testWhenPostSubjectDocumentWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/10")
                        .withBody(json(map("user", "Pepe")))
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );


        // act
        contributor.setSubject("10");
        JsonNode result = (JsonNode) contributor
                .executePostSubjectUsingDocument(new Document(json(map("user", "Pepe"))));

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testWhenPostSubjectFileWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/10")
                        .withBody("1234567890")
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        contributor.setSubject("10");

        // act
        JsonNode result = (JsonNode) contributor.executePostSubjectUsingFile(file(TOKEN_PATH));

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testWhenPostSubjectAndParamWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/10")
                        .withBody(
                                params(param("param1", "value1"))
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        contributor.setSubject("10");
        contributor.setRequestParameter("param1", "value1");
        JsonNode result = (JsonNode) contributor.executePostSubject();

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testWhenPostSubjectWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/10")
                        .withBody(
                                Not.not(regex(".+"))
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        contributor.setSubject("10");
        JsonNode result = (JsonNode) contributor.executePostSubject();

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testWhenPostDataDocumentWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/10")
                        .withBody(json(map("user", "Pepe")))
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        contributor.setSubject("10");
        JsonNode result = (JsonNode) contributor
                .executePostDataUsingDocument(new Document(json(map("user", "Pepe"))));

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testWhenPostDataFileWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/10")
                        .withBody("1234567890")
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        contributor.setSubject("10");
        JsonNode result = (JsonNode) contributor.executePostDataUsingFile(file(TOKEN_PATH));

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testWhenDeleteDataFileWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withMethod("DELETE")
                        .withPath("/10")
                        .withBody("1234567890")
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        contributor.setSubject("10");
        JsonNode result = (JsonNode) contributor.executeDeleteDataUsingFile(file(TOKEN_PATH));

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testWhenPostDataAndParamWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/10")
                        .withBody(
                                params(param("param1", "value1"))
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        contributor.setSubject("10");
        contributor.setRequestParameter("param1", "value1");
        JsonNode result = (JsonNode) contributor.executePostData();

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test
    public void testWhenPostDataWithSuccess() throws JsonProcessingException {
        // prepare
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/10")
                        .withBody(
                                Not.not(regex(".+"))
                        )
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        contributor.setSubject("10");
        JsonNode result = (JsonNode) contributor.executePostData();

        // check
        assertThat(result).isNotNull();
        assertThat(JsonUtils.readStringValue(result, "statusCode")).isEqualTo("200");
    }

    @Test(expected = WakamitiException.class)
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
