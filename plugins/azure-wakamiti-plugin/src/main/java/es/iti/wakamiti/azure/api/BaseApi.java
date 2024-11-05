/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jayway.jsonpath.TypeRef;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.util.http.HttpClient;
import es.iti.wakamiti.azure.api.model.query.Query;

import java.net.URL;
import java.net.http.HttpResponse;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static es.iti.wakamiti.api.util.JsonUtils.read;
import static es.iti.wakamiti.api.util.StringUtils.format;


public abstract class BaseApi<SELF extends HttpClient<SELF>> extends HttpClient<SELF> {

    protected static final String API_VERSION = "api-version";
    protected static final String ORGANIZATION = "organization";
    protected static final String PROJECT = "project";

    private static final String CONTINUATION_HEADER = "x-ms-continuationtoken";

    private static final String ORGANIZATION_BASE = String.format("/{%s}/_apis", ORGANIZATION);
    private static final String PROJECT_BASE = String.format("/{%s}/{%s}/_apis", ORGANIZATION, PROJECT);

    public BaseApi(URL baseUrl) {
        super(baseUrl);
        postCall(response -> {
            if (response.statusCode() >= 400) {
                throw new WakamitiException("The Azure API returned a non-OK response");
            }
        });
    }

    public SELF version(String version) {
        this.finalQueryParams.put(API_VERSION, version);
        return self();
    }

    public SELF organization(String organization) {
        this.finalPathParams.put(ORGANIZATION, organization);
        return self();
    }

    public SELF project(String project) {
        this.finalPathParams.put(PROJECT, project);
        return self();
    }

    public SELF tokenAuth(String token) {
        return basicAuth("", token);
    }

    protected String organization() {
        return ORGANIZATION_BASE;
    }

    protected String project() {
        return PROJECT_BASE;
    }

    protected <T> Stream<T> getAllPages(String uri, Function<JsonNode, List<T>> mapper) {
        final Function<HttpResponse<Optional<JsonNode>>, Optional<List<T>>> listGetter = response ->
                response.body().map(mapper);

        HttpResponse<Optional<JsonNode>> response = get(uri);
        List<T> pages = listGetter.apply(response).orElseGet(LinkedList::new);

        while (pages.isEmpty() && response.headers().firstValue(CONTINUATION_HEADER).isPresent()) {
            response = queryParam("continuationToken", response.headers().firstValue(CONTINUATION_HEADER).get())
                    .get(uri);
            listGetter.apply(response).ifPresent(pages::addAll);
        }

        return pages.stream();
    }

    protected <T> Stream<T> getAllPages(String uri, TypeRef<List<T>> type) {
        return getAllPages(uri, json -> read(json, "$.value", type));
    }

    protected Stream<JsonNode> getAllPages(String uri) {
        return getAllPages(uri, new TypeRef<>() {});
    }

    protected Optional<ArrayNode> doQuery(Query query) {
        return newRequest()
                .body(format("{\"query\": \"{}\"}", query))
                .post(project() + "/wit/wiql")
                .body()
                .map(json -> read(json, "$.workItems", ArrayNode.class))
                .filter(a -> !a.isEmpty());
    }

}
