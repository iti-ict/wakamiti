/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util.http;


import java.time.Instant;
import java.util.Optional;

import es.iti.wakamiti.api.WakamitiException;


/**
 * Base implementation for token providers with an injectable retriever.
 *
 * @param <C> the provider configuration type
 * @param <R> the request value required to obtain a token
 */
public abstract class AbstractTokenProvider<C extends TokenProviderConfig<C>, R>
        implements TokenProvider<C, R> {

    private final C configuration;
    private final String missingRetrieverMessage;
    private TokenRetriever<C, R> retriever;

    /**
     * Creates a provider with its configuration and optional initial retriever.
     *
     * @param configuration           the provider configuration
     * @param retriever               initial retriever, or {@code null}
     * @param missingRetrieverMessage error used when retrieval is attempted without a retriever
     */
    protected AbstractTokenProvider(
            C configuration,
            TokenRetriever<C, R> retriever,
            String missingRetrieverMessage
    ) {
        this.configuration = configuration;
        this.retriever = retriever;
        this.missingRetrieverMessage = missingRetrieverMessage;
    }

    @Override
    public C configuration() {
        return configuration;
    }

    /**
     * Replaces the strategy used to retrieve uncached tokens.
     *
     * @param retriever the non-null retrieval strategy
     */
    protected final void setTokenRetriever(
            TokenRetriever<C, R> retriever
    ) {
        if (retriever == null) {
            throw new WakamitiException(missingRetrieverMessage);
        }
        this.retriever = retriever;
    }

    /**
     * Validates the configuration and retrieves a token.
     *
     * @param request request-specific retrieval data
     * @return the retrieved token
     */
    protected final String retrieveToken(
            R request
    ) {
        configuration.checkParameters();
        return retrieveTokenUnchecked(request);
    }

    /**
     * Retrieves a token, reusing a matching cached value when configured.
     *
     * @param request request-specific retrieval data
     * @return a retrieved or cached token
     */
    protected final String getCachedOrRetrieve(
            R request
    ) {
        configuration.checkParameters();
        if (!configuration.cacheAuth()) {
            return retrieveTokenUnchecked(request);
        }

        TokenCacheKey key = cacheKey(request);
        return TokenCache.find(key).orElseGet(() -> {
            String token = retrieveTokenUnchecked(request);
            cacheEntry(token, Instant.now()).ifPresent(entry -> TokenCache.store(key, entry));
            return token;
        });
    }

    /**
     * Builds the cache identity for a request.
     *
     * @param request request-specific retrieval data
     * @return immutable cache identity
     */
    protected TokenCacheKey cacheKey(
            R request
    ) {
        return new TokenCacheKey(getClass().getName(), request == null ? Void.class : request);
    }

    /**
     * Converts a retrieved token into a cache entry. Returning an empty value
     * prevents the token from being cached.
     *
     * @param token retrieved token
     * @param now   time immediately after token retrieval
     * @return the cache entry, when the token may be cached
     */
    protected Optional<TokenCacheEntry> cacheEntry(
            String token,
            Instant now
    ) {
        return Optional.of(TokenCacheEntry.withoutExpiration(token));
    }

    private String retrieveTokenUnchecked(
            R request
    ) {
        if (retriever == null) {
            throw new WakamitiException(missingRetrieverMessage);
        }
        return retriever.get(configuration, request);
    }

}
