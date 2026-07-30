/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util.http.oauth;


import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import es.iti.wakamiti.api.WakamitiException;


/**
 * Stores the configuration used by the Oauth2 Provider Config component.
 */
public class Oauth2ProviderConfig {

    private static final String GRANT_TYPE = "grant_type";

    private static final Map<List<Object>, String> CACHED_TOKEN = new HashMap<>();
    private final Map<String, String> parameters = new LinkedHashMap<>();
    private boolean cacheAuth;
    private GrantType type;
    private URL url;
    private String clientId;
    private String clientSecret;

    /**
     * Finds a previously retrieved token matching the grant's identifying
     * parameters.
     *
     * @return the cached token when caching is enabled and a matching entry
     * exists; otherwise an empty optional
     */
    public Optional<String> findCachedToken() {
        return Optional.ofNullable(CACHED_TOKEN.get(getKey())).filter(x -> cacheAuth);
    }

    /**
     * Stores a token under the current grant parameters and returns it for use
     * in fluent retrieval code.
     *
     * @param token the raw access token
     * @return the same token
     */
    public String storeTokenAndGet(
            String token
    ) {
        CACHED_TOKEN.put(getKey(), token);
        return token;
    }

    /**
     * Returns the mutable form parameters sent to the token endpoint.
     *
     * @return parameters in insertion order
     */
    public Map<String, String> parameters() {
        return parameters;
    }

    /**
     * Adds or replaces a token-request form parameter.
     * <p>
     * Setting {@code grant_type} also initializes the typed grant when it has
     * not already been selected.
     * </p>
     *
     * @param name  the form field name
     * @param value the form field value
     * @return this configuration
     * @throws IllegalArgumentException if an unknown {@code grant_type} value
     *                                  is supplied
     */
    public Oauth2ProviderConfig addParameter(
            String name,
            String value
    ) {
        if (name.equals(GRANT_TYPE) && type == null) {
            type = GrantType.valueOf(value.toUpperCase());
        }
        parameters.put(name, value);
        return this;
    }

    /**
     * Enables or disables reuse of tokens held in the shared in-memory cache.
     *
     * @param cacheAuth {@code true} to reuse matching cached tokens
     * @return this configuration
     */
    public Oauth2ProviderConfig cacheAuth(
            boolean cacheAuth
    ) {
        this.cacheAuth = cacheAuth;
        return this;
    }

    /**
     * Selects the OAuth grant flow and initializes its {@code grant_type} form
     * parameter when absent.
     *
     * @param type the grant flow
     * @return this configuration
     */
    public Oauth2ProviderConfig type(
            GrantType type
    ) {
        parameters.putIfAbsent(GRANT_TYPE, type.name().toLowerCase());
        this.type = type;
        return this;
    }

    /**
     * Returns the authorization server's token endpoint.
     *
     * @return the endpoint URL, or {@code null} until configured
     */
    public URL url() {
        return url;
    }

    /**
     * Sets the authorization server's token endpoint.
     *
     * @param url the token endpoint URL
     * @return this configuration
     */
    public Oauth2ProviderConfig url(
            URL url
    ) {
        this.url = url;
        return this;
    }

    /**
     * Returns the OAuth client identifier.
     *
     * @return the client identifier, or {@code null} until configured
     */
    public String clientId() {
        return clientId;
    }

    /**
     * Sets the OAuth client identifier used for Basic authentication.
     *
     * @param clientId the client identifier
     * @return this configuration
     */
    public Oauth2ProviderConfig clientId(
            String clientId
    ) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Returns the OAuth client secret.
     *
     * @return the client secret, or {@code null} until configured
     */
    public String clientSecret() {
        return clientSecret;
    }

    /**
     * Sets the OAuth client secret used for Basic authentication.
     *
     * @param clientSecret the client secret
     * @return this configuration
     */
    public Oauth2ProviderConfig clientSecret(
            String clientSecret
    ) {
        this.clientSecret = clientSecret;
        return this;
    }

    /**
     * Verifies that the selected grant and endpoint credentials are complete.
     * Grant-specific form fields are checked in addition to the client
     * identifier, client secret, and token URL.
     *
     * @throws WakamitiException if any required value is absent or blank
     */
    public void checkParameters() {
        if (isNull(type)) {
            throw new WakamitiException("Missing oauth2 grant type.");
        }

        List<String> missing = type.requiredFields().stream()
                .filter(f -> !parameters().containsKey(f))
                .collect(Collectors.toList());

        if (!missing.isEmpty()) {
            throw new WakamitiException("Missing oauth2 required parameters for " + type + " grant type: " + missing);
        }

        if (isBlank(clientId)) {
            missing.add("clientId");
        }

        if (isBlank(clientSecret)) {
            missing.add("clientSecret");
        }

        if (isNull(url)) {
            missing.add("url");
        }

        if (!missing.isEmpty()) {
            throw new WakamitiException("Missing oauth2 configuration parameters: " + missing);
        }
    }

    private List<Object> getKey() {
        return parameters.entrySet().stream()
                .filter(e -> type.requiredFields().contains(e.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

}
