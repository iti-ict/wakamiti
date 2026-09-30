/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util.http;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;


/**
 * Immutable identity of a token cache entry.
 *
 * @param provider identifies the provider type that owns the key
 * @param identity provider-specific immutable cache identity
 */
public record TokenCacheKey(String provider, Object identity) {

    /**
     * Validates the key components.
     */
    public TokenCacheKey {
        Objects.requireNonNull(provider, "provider");
        Objects.requireNonNull(identity, "identity");
    }

    /**
     * Returns a SHA-256 fingerprint suitable for including a secret in a cache
     * key without retaining its clear-text value.
     *
     * @param value value to fingerprint, or {@code null}
     * @return the hexadecimal fingerprint, or {@code null} when input is null
     */
    public static String fingerprint(
            String value
    ) {
        if (value == null) {
            return null;
        }
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

}
