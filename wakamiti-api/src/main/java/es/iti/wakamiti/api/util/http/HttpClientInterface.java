/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util.http;


import java.io.Serializable;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.JsonNode;


/**
 * Defines the contract implemented by Http Client Interface.
 *
 * @param <SELF> the concrete client type
 */
public interface HttpClientInterface<SELF extends HttpClientInterface<SELF>> extends Serializable {

    /** Standard HTTP header used by the authentication helper methods. */
    String AUTHORIZATION = "Authorization";

    /**
     * Returns this client with its concrete fluent API type.
     *
     * @return this instance cast to {@code SELF}
     */
    @SuppressWarnings("unchecked")
    default SELF self() {
        return (SELF) this;
    }

    /**
     * Adds or replaces a query-string parameter for subsequent requests.
     *
     * @param name  the parameter name
     * @param value the value to encode in the query string
     * @return this client
     */
    SELF queryParam(
            String name,
            Object value
    );

    /**
     * Adds all supplied query-string parameters.
     *
     * @param params parameter names and values
     * @return this client
     */
    default SELF queryParams(
            Map<String, Object> params
    ) {
        params.forEach(this::queryParam);
        return self();
    }

    /**
     * Defines a value used to replace a named placeholder in request paths.
     *
     * @param name  the placeholder name
     * @param value the replacement value
     * @return this client
     */
    SELF pathParam(
            String name,
            Object value
    );

    /**
     * Defines multiple path-placeholder replacements.
     *
     * @param params placeholder names and replacement values
     * @return this client
     */
    default SELF pathParams(
            Map<String, Object> params
    ) {
        params.forEach(this::pathParam);
        return self();
    }

    /**
     * Adds or replaces an HTTP request header.
     *
     * @param name  the header name
     * @param value the header value
     * @return this client
     */
    SELF header(
            String name,
            Object value
    );

    /**
     * Adds all supplied HTTP request headers.
     *
     * @param params header names and values
     * @return this client
     */
    default SELF headers(
            Map<String, Object> params
    ) {
        params.forEach(this::header);
        return self();
    }

    /**
     * Sets the JSON request entity from its textual representation.
     *
     * @param body JSON text, or blank text to clear the current body
     * @return this client
     */
    SELF body(
            String body
    );

    /**
     * Configures an HTTP Basic authorization header.
     *
     * @param username the credential user name
     * @param password the credential password
     * @return this client
     */
    SELF basicAuth(
            String username,
            String password
    );

    /**
     * Configures an OAuth-style Bearer authorization header.
     *
     * @param token the raw bearer token, without the scheme prefix
     * @return this client
     */
    SELF bearerAuth(
            String token
    );

    /**
     * Sends a synchronous HTTP {@code POST} request.
     *
     * @param uri the absolute URI or path relative to the client's base URL
     * @return the response and its optional JSON body
     */
    HttpResponse<Optional<JsonNode>> post(
            String uri
    );

    /**
     * Sends a synchronous HTTP {@code GET} request.
     *
     * @param uri the absolute URI or path relative to the client's base URL
     * @return the response and its optional JSON body
     */
    HttpResponse<Optional<JsonNode>> get(
            String uri
    );

    /**
     * Sends a synchronous HTTP {@code DELETE} request.
     *
     * @param uri the absolute URI or path relative to the client's base URL
     * @return the response and its optional JSON body
     */
    HttpResponse<Optional<JsonNode>> delete(
            String uri
    );

    /**
     * Sends a synchronous HTTP {@code PUT} request.
     *
     * @param uri the absolute URI or path relative to the client's base URL
     * @return the response and its optional JSON body
     */
    HttpResponse<Optional<JsonNode>> put(
            String uri
    );

    /**
     * Sends a synchronous HTTP {@code PATCH} request.
     *
     * @param uri the absolute URI or path relative to the client's base URL
     * @return the response and its optional JSON body
     */
    HttpResponse<Optional<JsonNode>> patch(
            String uri
    );

    /**
     * Sends a synchronous HTTP {@code OPTIONS} request.
     *
     * @param uri the absolute URI or path relative to the client's base URL
     * @return the response and its optional JSON body
     */
    HttpResponse<Optional<JsonNode>> options(
            String uri
    );

    /**
     * Sends a synchronous HTTP {@code HEAD} request.
     *
     * @param uri the absolute URI or path relative to the client's base URL
     * @return the response; its JSON body is normally empty
     */
    HttpResponse<Optional<JsonNode>> head(
            String uri
    );

    /**
     * Sends a synchronous request using the non-standard {@code CONTENT}
     * method.
     *
     * @param uri the absolute URI or path relative to the client's base URL
     * @return the response and its optional JSON body
     */
    HttpResponse<Optional<JsonNode>> content(
            String uri
    );

    /**
     * Sends a synchronous HTTP {@code TRACE} request.
     *
     * @param uri the absolute URI or path relative to the client's base URL
     * @return the response and its optional JSON body
     */
    HttpResponse<Optional<JsonNode>> trace(
            String uri
    );

    /**
     * Sends an HTTP {@code POST} request asynchronously.
     *
     * @param uri the absolute URI or path relative to the client's base URL
     * @return a future completed with the response and optional JSON body
     */
    CompletableFuture<HttpResponse<Optional<JsonNode>>> postAsync(
            String uri
    );

    /**
     * Sends an HTTP {@code GET} request asynchronously.
     *
     * @param uri the absolute URI or path relative to the client's base URL
     * @return a future completed with the response and optional JSON body
     */
    CompletableFuture<HttpResponse<Optional<JsonNode>>> getAsync(
            String uri
    );

    /**
     * Sends an HTTP {@code DELETE} request asynchronously.
     *
     * @param uri the absolute URI or path relative to the client's base URL
     * @return a future completed with the response and optional JSON body
     */
    CompletableFuture<HttpResponse<Optional<JsonNode>>> deleteAsync(
            String uri
    );

    /**
     * Sends an HTTP {@code PUT} request asynchronously.
     *
     * @param uri the absolute URI or path relative to the client's base URL
     * @return a future completed with the response and optional JSON body
     */
    CompletableFuture<HttpResponse<Optional<JsonNode>>> putAsync(
            String uri
    );

    /**
     * Sends an HTTP {@code PATCH} request asynchronously.
     *
     * @param uri the absolute URI or path relative to the client's base URL
     * @return a future completed with the response and optional JSON body
     */
    CompletableFuture<HttpResponse<Optional<JsonNode>>> patchAsync(
            String uri
    );

    /**
     * Sends an HTTP {@code OPTIONS} request asynchronously.
     *
     * @param uri the absolute URI or path relative to the client's base URL
     * @return a future completed with the response and optional JSON body
     */
    CompletableFuture<HttpResponse<Optional<JsonNode>>> optionsAsync(
            String uri
    );

    /**
     * Sends an HTTP {@code HEAD} request asynchronously.
     *
     * @param uri the absolute URI or path relative to the client's base URL
     * @return a future completed with the response
     */
    CompletableFuture<HttpResponse<Optional<JsonNode>>> headAsync(
            String uri
    );

    /**
     * Sends a non-standard {@code CONTENT} request asynchronously.
     *
     * @param uri the absolute URI or path relative to the client's base URL
     * @return a future completed with the response and optional JSON body
     */
    CompletableFuture<HttpResponse<Optional<JsonNode>>> contentAsync(
            String uri
    );

    /**
     * Sends an HTTP {@code TRACE} request asynchronously.
     *
     * @param uri the absolute URI or path relative to the client's base URL
     * @return a future completed with the response and optional JSON body
     */
    CompletableFuture<HttpResponse<Optional<JsonNode>>> traceAsync(
            String uri
    );

}
