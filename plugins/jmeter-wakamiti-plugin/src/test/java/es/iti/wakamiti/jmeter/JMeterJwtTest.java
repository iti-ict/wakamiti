/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.jmeter;


import static es.iti.wakamiti.jmeter.TestUtil.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;

import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.util.http.jwt.JwtProviderConfig;


public class JMeterJwtTest {

    private static ClientAndServer client;

    private JMeterSupport support;

    @BeforeClass
    public static void setupServer() {
        ConfigurationProperties.logLevel("OFF");
        client = startClientAndServer();
    }

    @AfterClass
    public static void teardownServer() {
        client.close();
    }

    @Before
    public void setup() {
        client.reset();
        support = new JMeterSupport();
    }

    @Test
    public void testRetrieveJwtTokenWithSuccess() throws MalformedURLException {
        JwtProviderConfig config = configuration();
        config.usernameField("email").passwordField("secret").tokenPath("data.accessToken")
                .addParameter("tenant", "wakamiti");
        Map<String, String> body = new LinkedHashMap<>();
        body.put("tenant", "wakamiti");
        body.put("email", "user@example.org");
        body.put("secret", "password");
        client.when(request().withMethod("POST").withPath("/login")
                        .withHeader("Content-Type", "application/json.*")
                        .withBody(json(body)))
                .respond(response(json(Map.of("data", Map.of("accessToken", "jwt-token"))))
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON));

        assertThat(support.retrieveJwtToken(config, "user@example.org", "password")).isEqualTo("jwt-token");
    }

    @Test
    public void testJwtAuthenticationWithoutUrlFails() {
        assertThatThrownBy(() -> support.jwtProvider.getToken("user@example.org", "password"))
                .isInstanceOf(WakamitiException.class)
                .hasMessage("Missing JWT configuration parameter: url");
    }

    @Test
    public void testJwtAuthenticationWithRejectedLoginFails() throws MalformedURLException {
        client.when(request().withMethod("POST").withPath("/login"))
                .respond(response().withStatusCode(401));

        JwtProviderConfig config = configuration();

        assertThatThrownBy(() -> support.retrieveJwtToken(config, "user@example.org", "password"))
                .isInstanceOf(WakamitiException.class)
                .hasMessage("Error retrieving JWT authentication: login endpoint returned HTTP 401");
    }

    @Test
    public void testJwtAuthenticationWithoutTokenFails() throws MalformedURLException {
        client.when(request().withMethod("POST").withPath("/login"))
                .respond(response(json(Map.of("data", Map.of())))
                        .withStatusCode(200)
                        .withContentType(MediaType.APPLICATION_JSON));

        JwtProviderConfig config = configuration();

        assertThatThrownBy(() -> support.retrieveJwtToken(config, "user@example.org", "password"))
                .isInstanceOf(WakamitiException.class)
                .hasMessage("Error retrieving JWT authentication: token not found at path 'token'");
    }

    @Test
    public void testJwtAuthenticationWithInvalidJsonFails() throws MalformedURLException {
        client.when(request().withMethod("POST").withPath("/login"))
                .respond(response("not-json").withStatusCode(200).withContentType(MediaType.APPLICATION_JSON));

        JwtProviderConfig config = configuration();

        assertThatThrownBy(() -> support.retrieveJwtToken(config, "user@example.org", "password"))
                .isInstanceOf(WakamitiException.class)
                .hasMessage("Error retrieving JWT authentication response");
    }

    private JwtProviderConfig configuration() throws MalformedURLException {
        return new JwtProviderConfig().url(new URL("http://localhost:" + client.getLocalPort() + "/login"));
    }

}
