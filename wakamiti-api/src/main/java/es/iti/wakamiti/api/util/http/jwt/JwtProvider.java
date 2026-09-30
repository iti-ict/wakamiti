/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util.http.jwt;


import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.util.JsonUtils;
import es.iti.wakamiti.api.util.http.AbstractTokenProvider;
import es.iti.wakamiti.api.util.http.TokenCacheEntry;
import es.iti.wakamiti.api.util.http.TokenCacheKey;
import es.iti.wakamiti.api.util.http.TokenRetriever;


/** Retrieves JWT tokens and optionally reuses them until shortly before expiration. */
public final class JwtProvider extends AbstractTokenProvider<JwtProviderConfig, JwtProvider.Credentials> {

    private static final long EXPIRY_MARGIN_SECONDS = 30;

    /** Creates a JWT provider with an empty mutable configuration. */
    public JwtProvider() {
        super(new JwtProviderConfig(), null, "JWT retriever is needed");
    }

    /**
     * Replaces the strategy used to retrieve JWTs.
     *
     * @param retriever the non-null JWT retrieval strategy
     * @return this provider
     * @throws WakamitiException if {@code retriever} is {@code null}
     */
    public JwtProvider setRetriever(
            JwtRetriever retriever
    ) {
        if (retriever == null) {
            throw new WakamitiException("JWT retriever is needed");
        }
        setTokenRetriever(retriever);
        return this;
    }

    /**
     * Obtains a JWT with the supplied login credentials.
     *
     * @param username the login username
     * @param password the login password
     * @return the retrieved JWT
     */
    public String getToken(
            String username,
            String password
    ) {
        return getToken(new Credentials(username, password));
    }

    @Override
    public String getToken(
            Credentials credentials
    ) {
        return getCachedOrRetrieve(credentials);
    }

    @Override
    protected TokenCacheKey cacheKey(
            Credentials credentials
    ) {
        return new TokenCacheKey("jwt", new CacheKey(
                configuration().url().toExternalForm(),
                configuration().usernameField(),
                configuration().passwordField(),
                configuration().tokenPath(),
                Collections.unmodifiableMap(new LinkedHashMap<>(configuration().parameters())),
                credentials.username(),
                TokenCacheKey.fingerprint(credentials.password())
        ));
    }

    @Override
    protected Optional<TokenCacheEntry> cacheEntry(
            String token,
            Instant now
    ) {
        return expiration(token)
                .filter(expiry -> expiry.isAfter(now.plusSeconds(EXPIRY_MARGIN_SECONDS)))
                .map(expiry -> new TokenCacheEntry(token, expiry.minusSeconds(EXPIRY_MARGIN_SECONDS)));
    }

    private Optional<Instant> expiration(
            String token
    ) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return Optional.empty();
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Long seconds = JsonUtils.read(JsonUtils.json(payload), "exp", Long.class);
            return Optional.ofNullable(seconds).map(Instant::ofEpochSecond);
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    public interface JwtRetriever extends TokenRetriever<JwtProviderConfig, Credentials> {

    /**
     * Retrieves a JWT using the configured login endpoint and credentials.
     *
     * @param config   the validated JWT configuration
     * @param username the login username
     * @param password the login password
     * @return the retrieved JWT
     */
    String get(
                JwtProviderConfig config,
                String username,
                String password
        );

        @Override
        default String get(
                JwtProviderConfig config,
                Credentials credentials
        ) {
            return get(config, credentials.username(), credentials.password());
        }

    }

    /**
     * Request-specific credentials used to retrieve a JWT.
     *
     * @param username the login username
     * @param password the login password
     */
    public record Credentials(String username, String password) {

    }

    private record CacheKey(
            String url,
            String usernameField,
            String passwordField,
            String tokenPath,
            Map<String, String> parameters,
            String username,
            String passwordFingerprint
    ) {

    }

}
