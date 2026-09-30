/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util.http;


import java.time.Instant;
import java.util.Objects;


/**
 * Token value stored in the shared cache.
 *
 * @param token      raw token value
 * @param expiration expiration instant, or {@code null} when it does not expire
 */
public record TokenCacheEntry(String token, Instant expiration) {

    /**
     * Validates the token value.
     */
    public TokenCacheEntry {
        Objects.requireNonNull(token, "token");
    }

    /**
     * Creates a cache entry that has no known expiration.
     *
     * @param token raw token value
     * @return a non-expiring cache entry
     */
    public static TokenCacheEntry withoutExpiration(
            String token
    ) {
        return new TokenCacheEntry(token, null);
    }

}
