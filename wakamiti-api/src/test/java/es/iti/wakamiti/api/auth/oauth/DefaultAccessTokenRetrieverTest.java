/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.auth.oauth;


import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.util.http.oauth.GrantType;
import es.iti.wakamiti.api.util.http.oauth.Oauth2Provider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.configuration.Configuration;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.matchers.Times;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;
import org.mockserver.socket.tls.KeyStoreFactory;

import javax.net.ssl.HttpsURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Base64;

import static es.iti.wakamiti.api.util.JsonUtils.json;
import static es.iti.wakamiti.api.util.MapUtils.map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.Parameter.param;
import static org.mockserver.model.ParameterBody.params;

public class DefaultAccessTokenRetrieverTest {

    private static final Integer PORT = 4321;
    private static final String BASE_URL = MessageFormat.format("https://localhost:{0,number,#}", PORT);

    private static final ClientAndServer client = startClientAndServer(PORT);

    @BeforeClass
    public static void setup() {
        ConfigurationProperties.logLevel("OFF");
        HttpsURLConnection.setDefaultSSLSocketFactory(new KeyStoreFactory(
                Configuration.configuration(),
                new MockServerLogger()).sslContext().getSocketFactory());
    }

    @AfterClass
    public static void shutdown() {
        client.close();
    }

    @After
    public void tearDown() {
        client.reset();
    }

    @Test
    public void testGetWhenClientCredentialsTypeWithSuccess() throws MalformedURLException {
        // prepare
        String token = "1234567890";
        String clientId = "SOMETHING";
        String clientSecret = "s3cr3t";

        String basic = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
        mockServer(
                request()
                        .withPath("/token")
                        .withHeader("Authorization", "Basic " + basic)
                        .withBody(
                                params(
                                        param("grant_type", "client_credentials")
                                )
                        )
                ,
                response(json(map("access_token", token)).toString())
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        Oauth2Provider provider = new Oauth2Provider();
        provider.configuration()
                .url(new URL(BASE_URL + "/token"))
                .clientId(clientId)
                .clientSecret(clientSecret)
                .type(GrantType.CLIENT_CREDENTIALS);

        // act
        String result = provider.getAccessToken();

        // check
        assertThat(result).isNotNull().isEqualTo(token);
    }

    @Test
    public void testGetWhenPasswordTypeWithSuccess() throws MalformedURLException {
        // prepare
        String token = "1234567890";
        String clientId = "SOMETHING";
        String clientSecret = "s3cr3t";
        String username = "username";
        String password = "password";

        String basic = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
        mockServer(
                request()
                        .withPath("/token")
                        .withHeader("Authorization", "Basic " + basic)
                        .withBody(
                                params(
                                        param("grant_type", "password"),
                                        param(username, username),
                                        param(password, password)
                                )
                        )
                ,
                response(json(map("access_token", token)).toString())
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        Oauth2Provider provider = new Oauth2Provider();
        provider.configuration()
                .url(new URL(BASE_URL + "/token"))
                .clientId(clientId)
                .clientSecret(clientSecret)
                .addParameter("grant_type", "password")
                .addParameter(username, username)
                .addParameter(password, password);

        // act
        String result = provider.getAccessToken();

        // check
        assertThat(result).isNotNull().isEqualTo(token);
    }

    @Test
    public void testGetWhenCachedTokenWithSuccess() throws MalformedURLException {
        // prepare
        String token = "1234567890";
        String clientId = "SOMETHING";
        String clientSecret = "s3cr3t";

        String basic = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
        mockServer(
                request()
                        .withPath("/token")
                        .withHeader("Authorization", "Basic " + basic)
                        .withBody(
                                params(
                                        param("grant_type", "client_credentials")
                                )
                        )
                ,
                response(json(map("access_token", token)).toString())
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        Oauth2Provider provider = new Oauth2Provider();
        provider.configuration()
                .url(new URL(BASE_URL + "/token"))
                .clientId(clientId)
                .clientSecret(clientSecret)
                .type(GrantType.CLIENT_CREDENTIALS)
                .cacheAuth(true);

        // act
        provider.getAccessToken();
        String result = provider.getAccessToken();

        // check
        assertThat(result).isNotNull().isEqualTo(token);
    }

    @Test(expected = WakamitiException.class)
    public void testGetWhenMissingTypeWithError() throws MalformedURLException {
        // prepare
        String clientId = "SOMETHING";
        String clientSecret = "s3cr3t";

        Oauth2Provider provider = new Oauth2Provider();
        provider.configuration()
                .url(new URL(BASE_URL + "/token"))
                .clientId(clientId)
                .clientSecret(clientSecret);

        // act
        try {
            provider.getAccessToken();
        } catch (WakamitiException e) {
            // check
            assertThat(e).hasMessage("Missing oauth2 grant type.");
            throw e;
        }
    }

    @Test(expected = WakamitiException.class)
    public void testGetWhenMissingClientParametersWithError() {
        // prepare
        Oauth2Provider provider = new Oauth2Provider();
        provider.configuration()
                .type(GrantType.CLIENT_CREDENTIALS);

        // act
        try {
            provider.getAccessToken();
        } catch (WakamitiException e) {
            // check
            assertThat(e).hasMessage("Missing oauth2 configuration parameters: [clientId, clientSecret, url]");
            throw e;
        }
    }

    @Test(expected = WakamitiException.class)
    public void testGetWhenMissingPasswordTypeParametersWithError() throws MalformedURLException {
        // prepare
        String clientId = "SOMETHING";
        String clientSecret = "s3cr3t";

        Oauth2Provider provider = new Oauth2Provider();
        provider.configuration()
                .url(new URL(BASE_URL + "/token"))
                .clientId(clientId)
                .clientSecret(clientSecret)
                .type(GrantType.PASSWORD);

        // act
        try {
            provider.getAccessToken();
        } catch (WakamitiException e) {
            // check
            assertThat(e).hasMessage("Missing oauth2 required parameters for PASSWORD grant type: [username, password]");
            throw e;
        }
    }

    @Test(expected = WakamitiException.class)
    public void testGetWithStatusError() throws MalformedURLException {
        // prepare
        String clientId = "SOMETHING";
        String clientSecret = "s3cr3t";

        String basic = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
        mockServer(
                request()
                        .withPath("/token")
                        .withHeader("Authorization", "Basic " + basic)
                        .withBody(
                                params(
                                        param("grant_type", "client_credentials")
                                )
                        )
                ,
                response().withStatusCode(404)
        );

        Oauth2Provider provider = new Oauth2Provider();
        provider.configuration()
                .url(new URL(BASE_URL + "/token"))
                .clientId(clientId)
                .clientSecret(clientSecret)
                .type(GrantType.CLIENT_CREDENTIALS);

        // act
        try {
            provider.getAccessToken();
        } catch (WakamitiException e) {
            // check
            assertThat(e).hasMessage("Error retrieving oauth2 authentication")
                    .getCause().isExactlyInstanceOf(IllegalStateException.class)
                    .hasMessage("404");
            throw e;
        }
    }

    @Test(expected = WakamitiException.class)
    public void testGetWithError() throws MalformedURLException {
        // prepare
        String clientId = "SOMETHING";
        String clientSecret = "s3cr3t";

        String basic = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
        mockServer(
                request()
                        .withPath("/token")
                        .withHeader("Authorization", "Basic " + basic)
                        .withBody(
                                params(
                                        param("grant_type", "client_credentials")
                                )
                        )
                ,
                response("A text message")
                        .withStatusCode(400)
                        .withContentType(MediaType.TEXT_PLAIN)
        );

        Oauth2Provider provider = new Oauth2Provider();
        provider.configuration()
                .url(new URL(BASE_URL + "/token"))
                .clientId(clientId)
                .clientSecret(clientSecret)
                .type(GrantType.CLIENT_CREDENTIALS);

        // act
        try {
            provider.getAccessToken();
        } catch (WakamitiException e) {
            // check
            assertThat(e).hasMessage("Error retrieving oauth2 authentication")
                    .getCause().isExactlyInstanceOf(IllegalStateException.class)
                    .hasMessage("400. A text message");
            throw e;
        }
    }

    @Test
    public void testSetRetrieverWithSuccess() throws MalformedURLException {
        // prepare
        String token = "1234567890";
        String clientId = "SOMETHING";
        String clientSecret = "s3cr3t";


        Oauth2Provider provider = new Oauth2Provider();
        provider.configuration()
                .url(new URL(BASE_URL + "/token"))
                .clientId(clientId)
                .clientSecret(clientSecret)
                .type(GrantType.CLIENT_CREDENTIALS);

        // act
        provider.setRetriever(config -> token);
        String result = provider.getAccessToken();

        // check
        assertThat(result).isNotNull().isEqualTo(token);
    }

    @Test(expected = WakamitiException.class)
    public void testSetRetrieverWhenNullWithError() {
        // prepare
        Oauth2Provider provider = new Oauth2Provider();

        // act
        try {
            provider.setRetriever(null);
        } catch (WakamitiException e) {
            // check
            assertThat(e).hasMessage("Access token retriever is needed");
            throw e;
        }
    }

    private void mockServer(HttpRequest expected, HttpResponse response) {
        client.when(expected, Times.once()).respond(response);
    }
}
