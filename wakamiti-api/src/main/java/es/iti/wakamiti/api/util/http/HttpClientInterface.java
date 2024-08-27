/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util.http;


import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;
import java.util.Optional;


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

    Optional<JsonNode> post(String uri);

    Optional<JsonNode> get(String uri);

    Optional<JsonNode> delete(String uri);

    Optional<JsonNode> put(String uri);

    Optional<JsonNode> patch(String uri);

    Optional<JsonNode> options(String uri);

    Optional<JsonNode> head(String uri);

    Optional<JsonNode> content(String uri);

    Optional<JsonNode> trace(String uri);

}
