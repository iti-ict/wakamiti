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


/**
 * Abstract base class for interacting with Azure APIs.
 * Provides common functionality for configuring API requests, handling authentication,
 * and paginated responses.
 *
 * @param <SELF> A self-referential type parameter to support method chaining in subclasses.
 */
public abstract class BaseApi<SELF extends HttpClient<SELF>> extends HttpClient<SELF> {

    protected static final String API_VERSION = "api-version";
    protected static final String ORGANIZATION = "organization";
    protected static final String PROJECT = "project";

    private static final String CONTINUATION_HEADER = "x-ms-continuationtoken";

    /**
     * Constructs a new instance of the API client with the specified base URL.
     *
     * @param baseUrl The base URL for the Azure API.
     */
    protected BaseApi(URL baseUrl) {
        super(baseUrl);
        postCall(response -> {
            if (response.statusCode() >= 400) {
                throw new WakamitiException("The Azure API returned a non-OK response");
            }
        });
    }

    /**
     * Sets the API version as a query parameter for requests.
     *
     * @param version The API version to use.
     * @return The current instance for method chaining.
     */
    public SELF version(String version) {
        this.finalQueryParams.put(API_VERSION, version);
        return self();
    }

    /**
     * Sets the organization path parameter for requests.
     *
     * @param organization The organization name.
     * @return The current instance for method chaining.
     */
    public SELF organization(String organization) {
        this.finalPathParams.put(ORGANIZATION, organization);
        return self();
    }

    /**
     * Sets the project path parameter for requests.
     *
     * @param project The project name.
     * @return The current instance for method chaining.
     */
    public SELF projectBase(String project) {
        this.finalPathParams.put(PROJECT, project);
        return self();
    }

    /**
     * Configures the client to use token-based authentication.
     *
     * @param token The authentication token.
     * @return The current instance for method chaining.
     */
    public SELF tokenAuth(String token) {
        return basicAuth("", token);
    }

    /**
     * Builds the base project-specific API path.
     *
     * @return The project path with placeholders for organization and project.
     */
    protected String projectBase() {
        return String.format("/{%s}/{%s}/_apis", ORGANIZATION, PROJECT);
    }

    /**
     * Retrieves all pages of results from a paginated API endpoint.
     *
     * @param <T>    The type of the items in the result.
     * @param uri    The API endpoint URI.
     * @param mapper A function to map the JSON response to a list of items.
     * @return A stream of all items across pages.
     */
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

    /**
     * Retrieves all pages of results from a paginated API endpoint, using a JSON path type reference.
     *
     * @param <T>  The type of the items in the result.
     * @param uri  The API endpoint URI.
     * @param type The type reference for mapping the JSON response.
     * @return A stream of all items across pages.
     */
    protected <T> Stream<T> getAllPages(String uri, TypeRef<List<T>> type) {
        return getAllPages(uri, json -> read(json, "$.value", type));
    }

    /**
     * Executes a WIQL query against the Azure API and retrieves the matching work items.
     *
     * @param query The query to execute.
     * @return An optional ArrayNode containing the matching work items.
     */
    protected Optional<ArrayNode> doQuery(Query query) {
        return newRequest()
                .body(format("{\"query\": \"{}\"}", query))
                .post(projectBase() + "/wit/wiql")
                .body()
                .map(json -> read(json, "$.workItems", ArrayNode.class))
                .filter(a -> !a.isEmpty());
    }

}
