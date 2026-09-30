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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/** Shared process-wide storage for token providers. */
public final class TokenCache {

    private static final ConcurrentMap<TokenCacheKey, TokenCacheEntry> CACHED_TOKENS = new ConcurrentHashMap<>();

    private TokenCache() {
    }

    /**
     * Finds a valid cached token for the supplied key.
     *
     * @param key cache identity
     * @return the cached token when present and valid
     */
    public static Optional<String> find(
            TokenCacheKey key
    ) {
        TokenCacheEntry entry = CACHED_TOKENS.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (isExpired(entry, Instant.now())) {
            CACHED_TOKENS.remove(key, entry);
            return Optional.empty();
        }
        return Optional.of(entry.token());
    }

    /**
     * Stores a token under the supplied key.
     *
     * @param key   cache identity
     * @param entry token value and optional expiration
     */
    public static void store(
            TokenCacheKey key,
            TokenCacheEntry entry
    ) {
        CACHED_TOKENS.put(key, entry);
    }

    private static boolean isExpired(
            TokenCacheEntry entry,
            Instant now
    ) {
        return entry.expiration() != null && !entry.expiration().isAfter(now);
    }

}
