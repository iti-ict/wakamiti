/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util.http.oauth;


import static es.iti.wakamiti.api.util.JsonUtils.json;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.util.JsonUtils;


/**
 * Provides Oauth2 services to the surrounding component.
 */
public final class Oauth2Provider {

    private static final int CLIENT_ERROR_STATUS = 400;
    /** JSON property containing the bearer token in a successful OAuth response. */
    public static final String ACCESS_TOKEN = "access_token";

    private final Oauth2ProviderConfig oauth2ProviderConfig = new Oauth2ProviderConfig();
    private AccessTokenRetriever retriever;

    /**
     * Creates a provider using the built-in HTTP token retriever and an empty,
     * mutable configuration.
     */
    public Oauth2Provider() {
        this.retriever = new DefaultAccessTokenRetriever();
    }

    /**
     * Returns the live configuration used for subsequent token requests.
     *
     * @return this provider's mutable OAuth configuration
     */
    public Oauth2ProviderConfig configuration() {
        return oauth2ProviderConfig;
    }

    /**
     * Replaces the token retrieval strategy, primarily for alternate
     * transports or deterministic testing.
     *
     * @param retriever the non-null strategy used when no cached token exists
     * @return this provider
     * @throws WakamitiException if {@code retriever} is {@code null}
     */
    public Oauth2Provider setRetriever(
            AccessTokenRetriever retriever
    ) {
        if (retriever == null) {
            throw new WakamitiException("Access token retriever is needed");
        }
        this.retriever = retriever;
        return this;
    }

    /**
     * Obtains an access token for the current configuration.
     * <p>
     * A cached token is returned when caching is enabled and a matching entry
     * exists. Otherwise configuration is validated, the retriever is invoked,
     * and the resulting token is stored in the shared cache.
     * </p>
     *
     * @return the raw access token
     * @throws WakamitiException if required OAuth parameters are absent or
     *                           retrieval fails
     */
    public String getAccessToken() {
        return oauth2ProviderConfig.findCachedToken()
                .orElseGet(() -> {
                    oauth2ProviderConfig.checkParameters();
                    String token = retriever.get(oauth2ProviderConfig);
                    return oauth2ProviderConfig.storeTokenAndGet(token);
                });
    }

    public interface AccessTokenRetriever {

        /**
         * Exchanges an OAuth configuration for an access token.
         *
         * @param config the validated request configuration
         * @return the raw access token returned by the authorization server
         */
        String get(
                Oauth2ProviderConfig config
        );

    }

    /**
     * Provides the Default Access Token Retriever functionality used by Wakamiti.
     */
    private static final class DefaultAccessTokenRetriever implements AccessTokenRetriever {

        public String get(
                Oauth2ProviderConfig config
        ) {
            String auth = Base64.getEncoder()
                    .encodeToString((config.clientId() + ":" + config.clientSecret()).getBytes());
            List<NameValuePair> formData = config.parameters().entrySet().stream()
                    .map(e -> new BasicNameValuePair(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());
            HttpUriRequest request = RequestBuilder.create(HttpPost.METHOD_NAME)
                    .setUri(config.url().toString())
                    .setHeader("Authorization", "Basic " + auth)
                    .setEntity(new UrlEncodedFormEntity(formData, StandardCharsets.UTF_8))
                    .build();

            CloseableHttpClient httpClient = HttpClients.createDefault();

            try {
                CloseableHttpResponse response = httpClient.execute(request);
                int status = response.getStatusLine().getStatusCode();
                String body = EntityUtils.toString(response.getEntity());
                if (status >= CLIENT_ERROR_STATUS) {
                    throw new IllegalStateException(status + (isBlank(body) ? "" : ". " + body));
                }
                String token = JsonUtils.readStringValue(json(body), ACCESS_TOKEN);
                httpClient.close();
                response.close();
                return token;
            } catch (Exception e) {
                throw new WakamitiException("Error retrieving oauth2 authentication", e);
            }
        }

    }

}
