/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util.http.jwt;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Test;

import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.util.http.TokenCache;


public class JwtProviderTest {

    @After
    public void clearCache() throws NoSuchFieldException, IllegalAccessException {
        Field field = TokenCache.class.getDeclaredField("CACHED_TOKENS");
        field.setAccessible(true);
        ((Map<?, ?>) field.get(null)).clear();
    }

    @Test
    public void testGetTokenWhenCachedUsesValidToken() throws MalformedURLException {
        // prepare
        AtomicInteger calls = new AtomicInteger();
        JwtProvider provider = configuredProvider(true)
                .setRetriever((configuration, username, password) -> {
                    calls.incrementAndGet();
                    return jwt(Instant.now().plusSeconds(120).getEpochSecond());
                });

        // act
        JwtProvider.Credentials credentials = new JwtProvider.Credentials("username", "password");
        String first = provider.getToken(credentials);
        String second = provider.getToken(credentials);

        // check
        assertThat(second).isEqualTo(first);
        assertThat(calls.get()).isEqualTo(1);
    }

    @Test
    public void testGetTokenWhenExpiredDoesNotCacheToken() throws MalformedURLException {
        // prepare
        AtomicInteger calls = new AtomicInteger();
        JwtProvider provider = configuredProvider(true)
                .setRetriever((configuration, username, password) -> {
                    calls.incrementAndGet();
                    return jwt(Instant.now().minusSeconds(1).getEpochSecond());
                });

        // act
        provider.getToken("username", "password");
        provider.getToken("username", "password");

        // check
        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    public void testGetTokenWhenCloseToExpirationDoesNotCacheToken() throws MalformedURLException {
        // prepare
        AtomicInteger calls = new AtomicInteger();
        JwtProvider provider = configuredProvider(true)
                .setRetriever((configuration, username, password) -> {
                    calls.incrementAndGet();
                    return jwt(Instant.now().plusSeconds(29).getEpochSecond());
                });

        // act
        provider.getToken("username", "password");
        provider.getToken("username", "password");

        // check
        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    public void testGetTokenWithoutExpirationDoesNotCacheToken() throws MalformedURLException {
        // prepare
        AtomicInteger calls = new AtomicInteger();
        JwtProvider provider = configuredProvider(true)
                .setRetriever((configuration, username, password) -> {
                    calls.incrementAndGet();
                    return "not-a-jwt";
                });

        // act
        provider.getToken("username", "password");
        provider.getToken("username", "password");

        // check
        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    public void testGetTokenWithDifferentCredentialsUsesDifferentCacheEntries() throws MalformedURLException {
        // prepare
        AtomicInteger calls = new AtomicInteger();
        JwtProvider provider = configuredProvider(true)
                .setRetriever((configuration, username, password) -> {
                    calls.incrementAndGet();
                    return jwt(Instant.now().plusSeconds(120).getEpochSecond());
                });

        // act
        provider.getToken("first", "password");
        provider.getToken("second", "password");

        // check
        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    public void testGetTokenWithDifferentPasswordsUsesDifferentCacheEntries() throws MalformedURLException {
        // prepare
        AtomicInteger calls = new AtomicInteger();
        JwtProvider provider = configuredProvider(true)
                .setRetriever((configuration, username, password) -> {
                    calls.incrementAndGet();
                    return jwt(Instant.now().plusSeconds(120).getEpochSecond());
                });

        // act
        provider.getToken("username", "first-password");
        provider.getToken("username", "second-password");

        // check
        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    public void testGetTokenWithAnotherProviderUsesSharedCache() throws MalformedURLException {
        // prepare
        AtomicInteger calls = new AtomicInteger();
        JwtProvider first = configuredProvider(true)
                .setRetriever((configuration, username, password) -> {
                    calls.incrementAndGet();
                    return jwt(Instant.now().plusSeconds(120).getEpochSecond());
                });
        JwtProvider second = configuredProvider(true)
                .setRetriever((configuration, username, password) -> {
                    calls.incrementAndGet();
                    return jwt(Instant.now().plusSeconds(120).getEpochSecond());
                });

        // act
        String token = first.getToken("username", "password");
        String cachedToken = second.getToken("username", "password");

        // check
        assertThat(cachedToken).isEqualTo(token);
        assertThat(calls.get()).isEqualTo(1);
    }

    @Test
    public void testGetTokenWhenCachingIsDisabledDoesNotStoreToken() throws MalformedURLException {
        // prepare
        AtomicInteger calls = new AtomicInteger();
        JwtProvider provider = configuredProvider(false)
                .setRetriever((configuration, username, password) -> {
                    calls.incrementAndGet();
                    return jwt(Instant.now().plusSeconds(120).getEpochSecond());
                });

        // act
        provider.getToken("username", "password");
        provider.getToken("username", "password");

        // check
        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    public void testGetTokenWhenRetrieverIsMissingFails() throws MalformedURLException {
        // prepare
        JwtProvider provider = configuredProvider(false);

        // act and check
        assertThatThrownBy(() -> provider.getToken("username", "password"))
                .isInstanceOf(WakamitiException.class)
                .hasMessage("JWT retriever is needed");
    }

    private JwtProvider configuredProvider(
            boolean cached
    ) throws MalformedURLException {
        JwtProvider provider = new JwtProvider();
        provider.configuration()
                .url(new URL("https://example.org/login"))
                .addParameter("tenant", "wakamiti")
                .cacheAuth(cached);
        return provider;
    }

    private String jwt(
            long expiration
    ) {
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String header = encoder.encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
        String payload = encoder.encodeToString(("{\"exp\":" + expiration + "}").getBytes(StandardCharsets.UTF_8));
        return header + "." + payload + ".signature";
    }

}
