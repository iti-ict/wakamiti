/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util.http;


import com.fasterxml.jackson.databind.JsonNode;
import es.iti.wakamiti.api.WakamitiException;
import org.apache.commons.codec.binary.Base64;
import org.junit.AfterClass;
import org.junit.Before;
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
import java.util.List;
import java.util.Optional;

import static es.iti.wakamiti.api.util.MapUtils.map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.Parameter.param;


public class HttpClientTest {

    private static final Integer PORT = 4321;
    private static final String BASE_URL = MessageFormat.format("https://localhost:{0}", PORT.toString());

    private static final ClientAndServer server = startClientAndServer(PORT);

    private TestApi abstractClient;

    @BeforeClass
    public static void setup() {
        ConfigurationProperties.logLevel("OFF");
        HttpsURLConnection.setDefaultSSLSocketFactory(new KeyStoreFactory(
                Configuration.configuration(),
                new MockServerLogger()).sslContext().getSocketFactory());
    }

    @AfterClass
    public static void shutdown() {
        server.close();
    }

    @Before
    public void beforeEach() throws MalformedURLException {
        abstractClient = new TestApi(new URL(BASE_URL));
        server.reset();
    }

    @Test
    public void testGetWhenResponseOKWithSuccess() {
        // prepare
        mockServer(
                request()
                        .withMethod("GET")
                        .withPath("/123/321")
                        .withQueryStringParameters(
                                param("param1", "value1"),
                                param("param2", "value2")
                        )
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withHeader("Accept", "application/json")
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"id\":\"123\",\"name\": \"something\"}")
        );

        // act
        java.net.http.HttpResponse<Optional<JsonNode>> response = abstractClient
                .pathParams(map("id", "123", "p", "321"))
                .queryParams(map("param1", "value1", "param2", "value2"))
                .get("/{id}/{p}");

        // check
        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isPresent().get()
                .hasToString("{\"id\":\"123\",\"name\":\"something\"}");
        assertThat(response.headers().map())
                .containsEntry("connection", List.of("keep-alive"))
                .containsEntry("content-length", List.of("32"))
                .containsEntry("content-type", List.of("application/json"));
    }

    @Test
    public void testPostWhenResponseOkWithSuccess() {
        // prepare
        mockServer(
                request()
                        .withMethod("POST")
                        .withPath("/123")
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withHeader("Accept", "application/json")
                        .withHeader("Authorization", basic("user", "pass"))
                        .withBody("{\"id\":\"123\",\"name\":\"something\"}")
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        java.net.http.HttpResponse<Optional<JsonNode>> response = abstractClient
                .basicAuth("user", "pass")
                .pathParam("id", "123")
                .body("{\"id\":\"123\",\"name\":\"something\"}")
                .post("{id}");

        // check
        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isNotPresent();
        assertThat(response.headers().map())
                .containsEntry("connection", List.of("keep-alive"))
                .containsEntry("content-length", List.of("0"))
                .containsEntry("content-type", List.of("application/json"));
    }

    @Test
    public void testPatchWhenResponseOkWithSuccess() {
        // prepare
        mockServer(
                request()
                        .withMethod("PATCH")
                        .withPath("/123")
                        .withQueryStringParameters(
                                param("param1", "value1")
                        )
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withHeader("Accept", "application/json")
                        .withHeader("Authorization", "bearer abc")
                        .withBody("{\"id\":\"123\",\"name\":\"something\"}")
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"id\":\"123\",\"name\": \"something\"}")
        );

        // act
        java.net.http.HttpResponse<Optional<JsonNode>> response = abstractClient
                .bearerAuth("abc")
                .pathParam("id", "123")
                .queryParam("param1", "value1")
                .body("{\"id\":\"123\",\"name\":\"something\"}")
                .patch("/{id}");

        // check
        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isPresent().get()
                .hasToString("{\"id\":\"123\",\"name\":\"something\"}");
        assertThat(response.headers().map())
                .containsEntry("connection", List.of("keep-alive"))
                .containsEntry("content-length", List.of("32"))
                .containsEntry("content-type", List.of("application/json"));
    }

    @Test
    public void testPutWhenResponseOkWithSuccess() {
        // prepare
        mockServer(
                request()
                        .withMethod("PUT")
                        .withPath("/123")
                        .withQueryStringParameters(
                                param("param1", "value1")
                        )
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withHeader("Accept", "application/json")
                        .withHeader("Authorization", "bearer abc")
                        .withBody("{\"id\":\"123\",\"name\":\"something\"}")
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"id\":\"123\",\"name\": \"something\"}")
        );

        // act
        java.net.http.HttpResponse<Optional<JsonNode>> response = abstractClient
                .bearerAuth("abc")
                .pathParam("id", "123")
                .queryParam("param1", "value1")
                .body("{\"id\":\"123\",\"name\":\"something\"}")
                .put("/{id}");

        // check
        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isPresent().get()
                .hasToString("{\"id\":\"123\",\"name\":\"something\"}");
        assertThat(response.headers().map())
                .containsEntry("connection", List.of("keep-alive"))
                .containsEntry("content-length", List.of("32"))
                .containsEntry("content-type", List.of("application/json"));

    }

    @Test
    public void testDeleteWhenResponseOkWithSuccess() {
        // prepare
        mockServer(
                request()
                        .withMethod("DELETE")
                        .withPath("/123")
                        .withQueryStringParameters(
                                param("param1", "value1")
                        )
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withHeader("Accept", "application/json")
                        .withHeader("Authorization", "bearer abc")
                ,
                response()
                        .withStatusCode(204)
                        .withContentType(MediaType.APPLICATION_JSON)
        );

        // act
        java.net.http.HttpResponse<Optional<JsonNode>> response = abstractClient
                .bearerAuth("abc")
                .pathParam("id", "123")
                .queryParam("param1", "value1")
                .delete("/{id}");

        // check
        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(204);
        assertThat(response.body()).isNotPresent();
        assertThat(response.headers().map())
                .containsEntry("connection", List.of("keep-alive"))
                .containsEntry("content-type", List.of("application/json"));
    }

    @Test
    public void testOptionsWhenResponseOkWithSuccess() {
        // prepare
        mockServer(
                request()
                        .withMethod("OPTIONS")
                        .withPath("/123")
                        .withQueryStringParameters(
                                param("param1", "value1")
                        )
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withHeader("Accept", "application/json")
                        .withHeader("Authorization", "bearer abc")
                        .withBody("{\"id\":\"123\",\"name\":\"something\"}")
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"id\":\"123\",\"name\": \"something\"}")
        );

        // act
        java.net.http.HttpResponse<Optional<JsonNode>> response = abstractClient
                .bearerAuth("abc")
                .pathParam("id", "123")
                .queryParam("param1", "value1")
                .body("{\"id\":\"123\",\"name\":\"something\"}")
                .options("/{id}");

        // check
        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isPresent().get()
                .hasToString("{\"id\":\"123\",\"name\":\"something\"}");
        assertThat(response.headers().map())
                .containsEntry("connection", List.of("keep-alive"))
                .containsEntry("content-length", List.of("32"))
                .containsEntry("content-type", List.of("application/json"));

    }

    @Test
    public void testHeadWhenResponseOkWithSuccess() {
        // prepare
        mockServer(
                request()
                        .withMethod("HEAD")
                        .withPath("/123")
                        .withQueryStringParameters(
                                param("param1", "value1")
                        )
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withHeader("Accept", "application/json")
                        .withHeader("Authorization", "bearer abc")
                        .withBody("{\"id\":\"123\",\"name\":\"something\"}")
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"id\":\"123\",\"name\": \"something\"}")
        );

        // act
        java.net.http.HttpResponse<Optional<JsonNode>> response = abstractClient
                .bearerAuth("abc")
                .pathParam("id", "123")
                .queryParam("param1", "value1")
                .body("{\"id\":\"123\",\"name\":\"something\"}")
                .head("/{id}");

        // check
        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isNotPresent();
        assertThat(response.headers().map())
                .containsEntry("connection", List.of("keep-alive"))
                .containsEntry("content-length", List.of("32"))
                .containsEntry("content-type", List.of("application/json"));

    }

    @Test
    public void testContentWhenResponseOkWithSuccess() {
        // prepare
        mockServer(
                request()
                        .withMethod("CONTENT")
                        .withPath("/123")
                        .withQueryStringParameters(
                                param("param1", "value1")
                        )
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withHeader("Accept", "application/json")
                        .withHeader("Authorization", "bearer abc")
                        .withBody("{\"id\":\"123\",\"name\":\"something\"}")
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"id\":\"123\",\"name\": \"something\"}")
        );

        // act
        java.net.http.HttpResponse<Optional<JsonNode>> response = abstractClient
                .bearerAuth("abc")
                .pathParam("id", "123")
                .queryParam("param1", "value1")
                .body("{\"id\":\"123\",\"name\":\"something\"}")
                .content("/{id}");

        // check
        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isPresent().get()
                .hasToString("{\"id\":\"123\",\"name\":\"something\"}");
        assertThat(response.headers().map())
                .containsEntry("connection", List.of("keep-alive"))
                .containsEntry("content-length", List.of("32"))
                .containsEntry("content-type", List.of("application/json"));

    }

    @Test
    public void testTraceWhenResponseOkWithSuccess() {
        // prepare
        mockServer(
                request()
                        .withMethod("TRACE")
                        .withPath("/123")
                        .withQueryStringParameters(
                                param("param1", "value1")
                        )
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withHeader("Accept", "application/json")
                        .withHeader("Authorization", "bearer abc")
                        .withBody("{\"id\":\"123\",\"name\":\"something\"}")
                ,
                response()
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody("{\"id\":\"123\",\"name\": \"something\"}"),
                Times.exactly(2)
        );

        // act
        java.net.http.HttpResponse<Optional<JsonNode>> response = abstractClient
                .bearerAuth("abc")
                .pathParam("id", "123")
                .queryParam("param1", "value1")
                .body("{\"id\":\"123\",\"name\":\"something\"}")
                .trace("/{id}");

        // check
        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isPresent().get()
                .hasToString("{\"id\":\"123\",\"name\":\"something\"}");
        assertThat(response.headers().map())
                .containsEntry("connection", List.of("keep-alive"))
                .containsEntry("content-length", List.of("32"))
                .containsEntry("content-type", List.of("application/json"));


        // act
        response = abstractClient.newRequest()
                .pathParam("id", "123")
                .queryParam("param1", "value1")
                .body("{\"id\":\"123\",\"name\":\"something\"}")
                .trace("/{id}");

        // check
        assertThat(response).isNotNull();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isPresent().get()
                .hasToString("{\"id\":\"123\",\"name\":\"something\"}");
        assertThat(response.headers().map())
                .containsEntry("connection", List.of("keep-alive"))
                .containsEntry("content-length", List.of("32"))
                .containsEntry("content-type", List.of("application/json"));

    }

    @Test(expected = WakamitiException.class)
    public void testGetWhenMissingParamsWithError() {
        // act
        try {
            abstractClient.get("/{id}");

            // check
        } catch (WakamitiException e) {
            assertThat(e).hasMessage("Cannot determine uri for path: /{id}")
                    .cause().isExactlyInstanceOf(NoSuchFieldException.class)
                    .hasMessage("Missing parameters [id]");
            throw e;
        }
    }

    @Test
    public void testGetWhenHttpErrorWithSuccess() {
        // act
        java.net.http.HttpResponse<?> response = abstractClient.get("/something");

        // check
        assertThat(response.statusCode()).isEqualTo(404);
    }

    private void mockServer(HttpRequest expected, HttpResponse response) {
        mockServer(expected, response, Times.once());
    }

    private void mockServer(HttpRequest expected, HttpResponse response, Times times) {
        server.when(expected, times).respond(response);
    }

    private String basic(String username, String password) {
        return "Basic " + Base64.encodeBase64String((username + ":" + password).getBytes());
    }

    private static class TestApi extends HttpClient<TestApi> {

        public TestApi(URL baseUrl) {
            super(baseUrl);
        }

    }
}
