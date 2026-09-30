/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util.http;


import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Map;

import org.junit.After;
import org.junit.Test;


public class TokenCacheTest {

    @After
    public void clearCache() throws NoSuchFieldException, IllegalAccessException {
        Field field = TokenCache.class.getDeclaredField("CACHED_TOKENS");
        field.setAccessible(true);
        ((Map<?, ?>) field.get(null)).clear();
    }

    @Test
    public void testFindRemovesExpiredToken() {
        // prepare
        TokenCacheKey key = new TokenCacheKey("jwt", "identity");
        TokenCache.store(key, new TokenCacheEntry("token", Instant.now().minusSeconds(1)));

        // act and check
        assertThat(TokenCache.find(key)).isEmpty();
    }

    @Test
    public void testDifferentProviderNamespacesUseSeparateTokens() {
        // prepare
        TokenCacheKey jwtKey = new TokenCacheKey("jwt", "identity");
        TokenCacheKey oauthKey = new TokenCacheKey("oauth2", "identity");
        TokenCache.store(jwtKey, TokenCacheEntry.withoutExpiration("jwt-token"));
        TokenCache.store(oauthKey, TokenCacheEntry.withoutExpiration("oauth-token"));

        // act and check
        assertThat(TokenCache.find(jwtKey)).contains("jwt-token");
        assertThat(TokenCache.find(oauthKey)).contains("oauth-token");
    }

}
