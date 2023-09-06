/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.rest.oauth;


import es.iti.wakamiti.api.WakamitiException;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;


public class Oauth2ProviderConfig {

    private static final String GRANT_TYPE = "grant_type";

    private static final Map<List<Object>, String> cachedToken = new HashMap<>();
    private final Map<String, String> parameters = new LinkedHashMap<>();
    private boolean cacheAuth;
    private GrantType type;
    private URL url;
    private String clientId;
    private String clientSecret;

    public Optional<String> findCachedToken() {
        return Optional.ofNullable(cachedToken.get(getKey())).filter(x -> cacheAuth);
    }

    public String storeTokenAndGet(String token) {
        cachedToken.put(getKey(), token);
        return token;
    }

    public Map<String, String> parameters() {
        return parameters;
    }

    public Oauth2ProviderConfig addParameter(String name, String value) {
        if (name.equals(GRANT_TYPE) && type == null) {
            type = GrantType.valueOf(value.toUpperCase());
        }
        parameters.put(name, value);
        return this;
    }

    public Oauth2ProviderConfig cacheAuth(boolean cacheAuth) {
        this.cacheAuth = cacheAuth;
        return this;
    }

    public Oauth2ProviderConfig type(GrantType type) {
        parameters.putIfAbsent(GRANT_TYPE, type.name().toLowerCase());
        this.type = type;
        return this;
    }

    public URL url() {
        return url;
    }

    public Oauth2ProviderConfig url(URL url) {
        this.url = url;
        return this;
    }

    public String clientId() {
        return clientId;
    }

    public Oauth2ProviderConfig clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public String clientSecret() {
        return clientSecret;
    }

    public Oauth2ProviderConfig clientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

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
