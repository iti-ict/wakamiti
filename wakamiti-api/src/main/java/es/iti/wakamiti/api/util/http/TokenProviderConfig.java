/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util.http;


import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Base configuration shared by token providers.
 *
 * @param <C> the concrete configuration type
 */
public abstract class TokenProviderConfig<C extends TokenProviderConfig<C>> {

    private final Map<String, String> parameters = new LinkedHashMap<>();
    private URL url;
    private boolean cacheAuth;

    /**
     * Returns the endpoint used to retrieve a token.
     *
     * @return the endpoint URL, or {@code null} until configured
     */
    public URL url() {
        return url;
    }

    /**
     * Sets the endpoint used to retrieve a token.
     *
     * @param url the endpoint URL
     * @return this configuration
     */
    public C url(
            URL url
    ) {
        this.url = url;
        return self();
    }

    /**
     * Returns additional request parameters in insertion order.
     *
     * @return the mutable request parameters
     */
    public Map<String, String> parameters() {
        return parameters;
    }

    /**
     * Adds or replaces an additional request parameter.
     *
     * @param name  the parameter name
     * @param value the parameter value
     * @return this configuration
     */
    public C addParameter(
            String name,
            String value
    ) {
        parameters.put(name, value);
        return self();
    }

    /**
     * Indicates whether this provider may reuse a matching cached token.
     *
     * @return {@code true} when token caching is enabled
     */
    public boolean cacheAuth() {
        return cacheAuth;
    }

    /**
     * Enables or disables token caching.
     *
     * @param cacheAuth {@code true} to enable token caching
     * @return this configuration
     */
    public C cacheAuth(
            boolean cacheAuth
    ) {
        this.cacheAuth = cacheAuth;
        return self();
    }

    /**
     * Validates the configuration required by the concrete provider.
     */
    public abstract void checkParameters();

    /**
     * Returns this object with its concrete fluent type.
     *
     * @return this configuration
     */
    protected abstract C self();

}
