/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util.http;


import com.fasterxml.jackson.databind.JsonNode;

import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


public interface HttpClientInterface<SELF extends HttpClientInterface<SELF>> {

    String AUTHORIZATION = "Authorization";

    @SuppressWarnings("unchecked")
    default SELF self() {
        return (SELF) this;
    }

    SELF queryParam(String name, Object value);

    default SELF queryParams(Map<String, Object> params) {
        params.forEach(this::queryParam);
        return self();
    }

    SELF pathParam(String name, Object value);

    default SELF pathParams(Map<String, Object> params) {
        params.forEach(this::pathParam);
        return self();
    }

    SELF header(String name, Object value);

    default SELF headers(Map<String, Object> params) {
        params.forEach(this::header);
        return self();
    }

    SELF body(String body);

    SELF basicAuth(String username, String password);

    SELF bearerAuth(String token);

    HttpResponse<Optional<JsonNode>> post(String uri);

    HttpResponse<Optional<JsonNode>> get(String uri);

    HttpResponse<Optional<JsonNode>> delete(String uri);

    HttpResponse<Optional<JsonNode>> put(String uri);

    HttpResponse<Optional<JsonNode>> patch(String uri);

    HttpResponse<Optional<JsonNode>> options(String uri);

    HttpResponse<Optional<JsonNode>> head(String uri);

    HttpResponse<Optional<JsonNode>> content(String uri);

    HttpResponse<Optional<JsonNode>> trace(String uri);

    CompletableFuture<HttpResponse<Optional<JsonNode>>> postAsync(String uri);

    CompletableFuture<HttpResponse<Optional<JsonNode>>> getAsync(String uri);

    CompletableFuture<HttpResponse<Optional<JsonNode>>> deleteAsync(String uri);

    CompletableFuture<HttpResponse<Optional<JsonNode>>> putAsync(String uri);

    CompletableFuture<HttpResponse<Optional<JsonNode>>> patchAsync(String uri);

    CompletableFuture<HttpResponse<Optional<JsonNode>>> optionsAsync(String uri);

    CompletableFuture<HttpResponse<Optional<JsonNode>>> headAsync(String uri);

    CompletableFuture<HttpResponse<Optional<JsonNode>>> contentAsync(String uri);

    CompletableFuture<HttpResponse<Optional<JsonNode>>> traceAsync(String uri);

}
