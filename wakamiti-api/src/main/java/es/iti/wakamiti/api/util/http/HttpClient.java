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
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static es.iti.wakamiti.api.util.JsonUtils.json;
import static es.iti.wakamiti.api.util.MapUtils.entry;
import static es.iti.wakamiti.api.util.MapUtils.map;
import static es.iti.wakamiti.api.util.PathUtil.encodeURI;
import static es.iti.wakamiti.api.util.StringUtils.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.join;


public abstract class HttpClient<SELF extends HttpClient<SELF>> implements HttpClientInterface<SELF>, Serializable, Cloneable {

    private static final Logger LOGGER = WakamitiLogger.forClass(WakamitiAPI.class);
    private static final long serialVersionUID = 6128016096756071380L;

    private static final Map.Entry<java.net.http.HttpClient.Version, String> HTTP_1_1 =
            entry(java.net.http.HttpClient.Version.HTTP_1_1, "HTTP/1.1");
    private static final java.net.http.HttpClient CLIENT = java.net.http.HttpClient.newBuilder()
            .executor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 10))
            .version(HTTP_1_1.getKey())
            .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    private final URL baseUrl;
    protected transient JsonNode body;
    protected transient Map<String, Object> finalQueryParams = new LinkedHashMap<>();
    protected transient Map<String, Object> finalPathParams = new LinkedHashMap<>();
    protected transient Map<String, Object> finalHeaders = new LinkedHashMap<>();
    protected transient Map<String, Object> queryParams = new LinkedHashMap<>();
    protected transient Map<String, Object> pathParams = new LinkedHashMap<>();
    protected transient Map<String, Object> headers = new LinkedHashMap<>();
    private transient Consumer<HttpResponse<Optional<JsonNode>>> postCall = response -> {
    };

    public HttpClient(URL baseUrl) {
        this.baseUrl = baseUrl;
    }

    public SELF postCall(Consumer<HttpResponse<Optional<JsonNode>>> postCall) {
        this.postCall = postCall;
        return self();
    }

    @Override
    public SELF queryParam(String name, Object value) {
        queryParams.put(name, value);
        return self();
    }

    @Override
    public SELF pathParam(String name, Object value) {
        pathParams.put(name, value);
        return self();
    }

    @Override
    public SELF header(String name, Object value) {
        headers.put(name, value);
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
        this.body = Optional.ofNullable(body).filter(StringUtils::isNotBlank).map(JsonUtils::json).orElse(null);
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
                                .map(JsonNode::toString)
                                .map(HttpRequest.BodyPublishers::ofString)
                                .orElse(HttpRequest.BodyPublishers.noBody()));
        headers(map("Content-Type", "application/json", "Accept", "application/json"));
        headers.forEach((k, v) -> builder.header(k, Objects.toString(v)));
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

    protected HttpRequest buildPost(String uri) {
        return buildRequest("POST", uri);
    }

    protected HttpRequest buildGet(String uri) {
        return buildRequest("GET", uri);
    }

    protected HttpRequest buildPut(String uri) {
        return buildRequest("PUT", uri);
    }

    protected HttpRequest buildPatch(String uri) {
        return buildRequest("PATCH", uri);
    }

    protected HttpRequest buildDelete(String uri) {
        return buildRequest("DELETE", uri);
    }

    protected HttpRequest buildOptions(String uri) {
        return buildRequest("OPTIONS", uri);
    }

    protected HttpRequest buildHead(String uri) {
        return buildRequest("HEAD", uri);
    }

    protected HttpRequest buildContent(String uri) {
        return buildRequest("CONTENT", uri);
    }

    protected HttpRequest buildTrace(String uri) {
        return buildRequest("TRACE", uri);
    }

    @Override
    public HttpResponse<Optional<JsonNode>> post(String uri) {
        return send(buildPost(uri));
    }

    @Override
    public HttpResponse<Optional<JsonNode>> get(String uri) {
        return send(buildGet(uri));
    }

    @Override
    public HttpResponse<Optional<JsonNode>> put(String uri) {
        return send(buildPut(uri));
    }

    @Override
    public HttpResponse<Optional<JsonNode>> patch(String uri) {
        return send(buildPatch(uri));
    }

    @Override
    public HttpResponse<Optional<JsonNode>> delete(String uri) {
        return send(buildDelete(uri));
    }

    @Override
    public HttpResponse<Optional<JsonNode>> options(String uri) {
        return send(buildOptions(uri));
    }

    @Override
    public HttpResponse<Optional<JsonNode>> head(String uri) {
        return send(buildHead(uri));
    }

    @Override
    public HttpResponse<Optional<JsonNode>> content(String uri) {
        return send(buildContent(uri));
    }

    @Override
    public HttpResponse<Optional<JsonNode>> trace(String uri) {
        return send(buildTrace(uri));
    }

    protected HttpResponse<Optional<JsonNode>> send(HttpRequest request) {
        try {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("HTTP call => {} ", stringify(request));
            }
            HttpResponse<Optional<JsonNode>> response = CLIENT.send(request, asJSON());
            postCall.accept(response);
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("HTTP response => {}", stringify(response));
            }
            return response;
        } catch (IOException e) {
            throw new WakamitiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WakamitiException(e);
        }
    }

    @Override
    public CompletableFuture<HttpResponse<Optional<JsonNode>>> postAsync(String uri) {
        return sendAsync(buildPost(uri));
    }

    @Override
    public CompletableFuture<HttpResponse<Optional<JsonNode>>> getAsync(String uri) {
        return sendAsync(buildGet(uri));
    }

    @Override
    public CompletableFuture<HttpResponse<Optional<JsonNode>>> putAsync(String uri) {
        return sendAsync(buildPut(uri));
    }

    @Override
    public CompletableFuture<HttpResponse<Optional<JsonNode>>> patchAsync(String uri) {
        return sendAsync(buildPatch(uri));
    }

    @Override
    public CompletableFuture<HttpResponse<Optional<JsonNode>>> deleteAsync(String uri) {
        return sendAsync(buildDelete(uri));
    }

    @Override
    public CompletableFuture<HttpResponse<Optional<JsonNode>>> optionsAsync(String uri) {
        return sendAsync(buildOptions(uri));
    }

    @Override
    public CompletableFuture<HttpResponse<Optional<JsonNode>>> headAsync(String uri) {
        return sendAsync(buildHead(uri));
    }

    @Override
    public CompletableFuture<HttpResponse<Optional<JsonNode>>> contentAsync(String uri) {
        return sendAsync(buildContent(uri));
    }

    @Override
    public CompletableFuture<HttpResponse<Optional<JsonNode>>> traceAsync(String uri) {
        return sendAsync(buildTrace(uri));
    }

    private CompletableFuture<HttpResponse<Optional<JsonNode>>> sendAsync(HttpRequest request) {
        CompletableFuture<HttpResponse<Optional<JsonNode>>> completable = CLIENT.sendAsync(request, asJSON());
        completable.thenAccept(postCall);
        return completable;
    }

    private String stringify(HttpRequest request) {
        return System.lineSeparator() +
                "Request method:\t" + request.method() + System.lineSeparator() +
                "Request URI:\t" + request.uri() + System.lineSeparator() +
                "Query params:\t" + stringify(queryParams) + System.lineSeparator() +
                "Path params:\t" + stringify(pathParams) + System.lineSeparator() +
                "Headers:\t\t" + stringify(headers) + System.lineSeparator() +
                "Body:\t\t\t" +
                Optional.ofNullable(body).map(JsonNode::toPrettyString).map(j -> System.lineSeparator() + j)
                        .orElse("<none>");
    }

    private String stringify(Map<String, Object> params) {
        if (params.isEmpty()) {
            return "<none>";
        } else {
            return join(params.entrySet(), System.lineSeparator() + "\t\t\t\t");
        }
    }

    private String stringify(HttpResponse<Optional<JsonNode>> response) {
        return System.lineSeparator() +
                HTTP_1_1.getValue() + " " + response.statusCode() + System.lineSeparator() +
                response.headers().map().entrySet().stream()
                        .map(e -> e.getKey() + ": " + join(e.getValue(), "; "))
                        .collect(Collectors.joining(System.lineSeparator())) +
                response.body().map(JsonNode::toPrettyString)
                        .map(str -> System.lineSeparator() + System.lineSeparator() + str).orElse("");
    }

    protected SELF newRequest() {
        this.body = null;
        this.queryParams.clear();
        this.pathParams.clear();
        this.headers.clear();
        return copy();
    }

    private HttpResponse.BodyHandler<Optional<JsonNode>> asJSON() {
        return responseInfo -> HttpResponse.BodySubscribers.mapping(
                HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8), str -> {
                    try {
                        return Optional.of(json(str));
                    } catch (Exception e) {
                        if (!isBlank(str)) {
                            LOGGER.error("Error parsing message: {}", str, e);
                        }
                        return Optional.empty();
                    }
                });
    }

    public SELF copy() {
        SELF self = SerializationUtils.clone(self()).postCall(this.postCall);
        if (self.headers == null) self.headers = new LinkedHashMap<>();
        if (self.pathParams == null) self.pathParams = new LinkedHashMap<>();
        if (self.queryParams == null) self.queryParams = new LinkedHashMap<>();
        if (self.finalHeaders == null) self.finalHeaders = new LinkedHashMap<>();
        if (self.finalPathParams == null) self.finalPathParams = new LinkedHashMap<>();
        if (self.finalQueryParams == null) self.finalQueryParams = new LinkedHashMap<>();
        self.headers(this.headers);
        self.pathParams(this.pathParams);
        self.queryParams(this.queryParams);
        Optional.ofNullable(body).ifPresent(b -> self.body(b.toString()));
        self.finalHeaders.putAll(this.finalHeaders);
        self.finalPathParams.putAll(this.finalPathParams);
        self.finalQueryParams.putAll(this.finalQueryParams);
        return self;
    }
}
