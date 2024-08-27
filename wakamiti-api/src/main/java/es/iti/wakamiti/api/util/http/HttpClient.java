/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util.http;


import com.fasterxml.jackson.databind.JsonNode;
import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.util.JsonUtils;
import es.iti.wakamiti.api.util.WakamitiLogger;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static es.iti.wakamiti.api.util.JsonUtils.json;
import static es.iti.wakamiti.api.util.MapUtils.entry;
import static es.iti.wakamiti.api.util.MapUtils.map;
import static es.iti.wakamiti.api.util.PathUtil.encodeURI;
import static es.iti.wakamiti.api.util.StringUtils.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.join;


public abstract class HttpClient<SELF extends HttpClient<SELF>> implements HttpClientInterface<SELF> {

    private static final Logger LOGGER = WakamitiLogger.forClass(WakamitiAPI.class);

    private static final Map.Entry<java.net.http.HttpClient.Version, String> HTTP_1_1 =
            entry(java.net.http.HttpClient.Version.HTTP_1_1, "HTTP/1.1");
    private static final java.net.http.HttpClient CLIENT = java.net.http.HttpClient.newBuilder()
            .version(HTTP_1_1.getKey())
            .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    private final URL baseUrl;
    protected String body;
    protected Map<String, String> finalQueryParams = new LinkedHashMap<>();
    protected Map<String, String> finalPathParams = new LinkedHashMap<>();
    protected Map<String, String> finalHeaders = new LinkedHashMap<>();
    protected Map<String, String> queryParams = new LinkedHashMap<>();
    protected Map<String, String> pathParams = new LinkedHashMap<>();
    protected Map<String, String> headers = new LinkedHashMap<>();

    public HttpClient(URL baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public SELF queryParam(String name, Object value) {
        queryParams.put(name, value.toString());
        return self();
    }

    @Override
    public SELF pathParam(String name, Object value) {
        pathParams.put(name, value.toString());
        return self();
    }

    @Override
    public SELF header(String name, Object value) {
        headers.put(name, value.toString());
        return self();
    }

    public SELF basicAuth(String username, String password) {
        finalHeaders.put(AUTHORIZATION, "Basic " +
                Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));
        return self();
    }

    public SELF bearerAuth(String token) {
        finalHeaders.put(AUTHORIZATION, "Bearer " + token);
        return self();
    }

    @Override
    public SELF body(String body) {
        this.body = body;
        return self();
    }

    private HttpRequest buildRequest(String method, String path) {
        this.pathParams.putAll(finalPathParams);
        this.queryParams.putAll(finalQueryParams);
        this.headers.putAll(finalHeaders);
        URI uri = uri(path);

        if (!queryParams.isEmpty()) {
            uri = URI.create(uri.toASCIIString() + "?" + join(queryParams.entrySet(), "&"));
        }
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .method(method,
                        Optional.ofNullable(body)
                                .map(JsonUtils::json)
                                .map(JsonNode::toString)
                                .map(HttpRequest.BodyPublishers::ofString)
                                .orElse(HttpRequest.BodyPublishers.noBody()));
        headers(map("Content-Type", "application/json", "Accept", "application/json"));
        headers.forEach(builder::header);
        return builder.build();
    }

    private URI uri(String path) {
        try {
            path = format(path, pathParams);
            String encoded = encodeURI(path.startsWith("/") ? path : "/" + path);
            return URI.create(baseUrl.toString().replaceAll("/$", "") + encoded);
        } catch (NoSuchFieldException e) {
            throw new WakamitiException("Cannot determine uri for path: {}", path, e);
        }
    }

    @Override
    public Optional<JsonNode> post(String uri) {
        return send(buildRequest("POST", uri));
    }

    @Override
    public Optional<JsonNode> get(String uri) {
        return send(buildRequest("GET", uri));
    }

    @Override
    public Optional<JsonNode> put(String uri) {
        return send(buildRequest("PUT", uri));
    }

    @Override
    public Optional<JsonNode> patch(String uri) {
        return send(buildRequest("PATCH", uri));
    }

    @Override
    public Optional<JsonNode> delete(String uri) {
        return send(buildRequest("DELETE", uri));
    }

    @Override
    public Optional<JsonNode> options(String uri) {
        return send(buildRequest("OPTIONS", uri));
    }

    @Override
    public Optional<JsonNode> head(String uri) {
        return send(buildRequest("HEAD", uri));
    }

    @Override
    public Optional<JsonNode> content(String uri) {
        return send(buildRequest("CONTENT", uri));
    }

    @Override
    public Optional<JsonNode> trace(String uri) {
        return send(buildRequest("TRACE", uri));
    }

    private Optional<JsonNode> send(HttpRequest request) {
        try {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Azure call => {} ", stringify(request));
            }
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Azure response => {}", stringify(response));
            }
            if (response.statusCode() >= 400) {
                throw new WakamitiException("The Azure API returned a non-OK response");
            }
            return Optional.ofNullable(response.body()).filter(StringUtils::isNotBlank).map(JsonUtils::json);
        } catch (IOException e) {
            throw new WakamitiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WakamitiException(e);
        }
    }

    private String stringify(HttpRequest request) {
        return new StringBuilder(System.lineSeparator())
                .append("Request method:\t").append(request.method()).append(System.lineSeparator())
                .append("Request URI:\t").append(request.uri()).append(System.lineSeparator())
                .append("Query params:\t").append(stringify(queryParams)).append(System.lineSeparator())
                .append("Path params:\t").append(stringify(pathParams)).append(System.lineSeparator())
                .append("Headers:\t\t").append(stringify(headers)).append(System.lineSeparator())
                .append("Body:\t\t\t").append(
                        Optional.ofNullable(body).map(this::prettify).map(j -> System.lineSeparator() + j)
                                .orElse("<none>")
                ).toString();
    }

    private String stringify(Map<String, String> params) {
        if (params.isEmpty()) {
            return "<none>";
        } else {
            return join(params.entrySet(), System.lineSeparator() + "\t\t\t\t");
        }
    }

    private String stringify(HttpResponse<String> response) {
        return new StringBuilder(System.lineSeparator())
                .append(HTTP_1_1.getValue()).append(" ").append(response.statusCode()).append(System.lineSeparator())
                .append(
                        response.headers().map().entrySet().stream()
                                .map(e -> e.getKey() + ": " + join(e.getValue(), "; "))
                                .collect(Collectors.joining(System.lineSeparator()))
                ).append(
                        body(response).map(this::prettify)
                                .map(str -> System.lineSeparator() + System.lineSeparator() + str).orElse("")
                ).toString();
    }

    private Optional<String> body(HttpResponse<String> response) {
        String body = response.body();
        if (!isBlank(body)) {
            return Optional.of(body);
        } else {
            return Optional.empty();
        }
    }

    private String prettify(String body) {
        try {
            body = json(body).toPrettyString();
        } catch (RuntimeException ignored) {
        }
        return body;
    }

    protected SELF newRequest() {
        this.body = null;
        this.queryParams.clear();
        this.pathParams.clear();
        this.headers.clear();
        return self();
    }

}
