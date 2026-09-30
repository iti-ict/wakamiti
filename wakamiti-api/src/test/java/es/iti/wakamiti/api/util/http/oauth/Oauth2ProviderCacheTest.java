/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util.http.oauth;


import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Test;

import es.iti.wakamiti.api.util.http.TokenCache;


public class Oauth2ProviderCacheTest {

    @After
    public void clearCache() throws NoSuchFieldException, IllegalAccessException {
        Field field = TokenCache.class.getDeclaredField("CACHED_TOKENS");
        field.setAccessible(true);
        ((Map<?, ?>) field.get(null)).clear();
    }

    @Test
    public void testGetAccessTokenWithAnotherProviderUsesSharedCache() throws MalformedURLException {
        // prepare
        AtomicInteger calls = new AtomicInteger();
        Oauth2Provider first = configuredProvider("https://example.org/token", "client", "secret", "scope", calls);
        Oauth2Provider second = configuredProvider("https://example.org/token", "client", "secret", "scope", calls);

        // act
        String token = first.getAccessToken();
        String cachedToken = second.getAccessToken();

        // check
        assertThat(cachedToken).isEqualTo(token);
        assertThat(calls.get()).isEqualTo(1);
    }

    @Test
    public void testGetAccessTokenWithDifferentIdentityDoesNotReuseCachedToken() throws MalformedURLException {
        // prepare
        AtomicInteger calls = new AtomicInteger();
        Oauth2Provider endpoint = configuredProvider("https://example.org/one", "client", "secret", "scope", calls);
        Oauth2Provider client = configuredProvider("https://example.org/one", "another-client", "secret", "scope", calls);
        Oauth2Provider scope = configuredProvider("https://example.org/one", "client", "secret", "another-scope", calls);

        // act
        endpoint.getAccessToken();
        client.getAccessToken();
        scope.getAccessToken();

        // check
        assertThat(calls.get()).isEqualTo(3);
    }

    @Test
    public void testGetAccessTokenWhenCachingIsDisabledDoesNotStoreToken() throws MalformedURLException {
        // prepare
        AtomicInteger calls = new AtomicInteger();
        Oauth2Provider provider = configuredProvider("https://example.org/token", "client", "secret", "scope", calls);
        provider.configuration().cacheAuth(false);

        // act
        provider.getAccessToken();
        provider.getAccessToken();

        // check
        assertThat(calls.get()).isEqualTo(2);
    }

    private Oauth2Provider configuredProvider(
            String url,
            String clientId,
            String clientSecret,
            String scope,
            AtomicInteger calls
    ) throws MalformedURLException {
        Oauth2Provider provider = new Oauth2Provider();
        provider.configuration()
                .url(new URL(url))
                .clientId(clientId)
                .clientSecret(clientSecret)
                .type(GrantType.CLIENT_CREDENTIALS)
                .addParameter("scope", scope)
                .cacheAuth(true);
        provider.setRetriever(configuration -> "token-" + calls.incrementAndGet());
        return provider;
    }

}
