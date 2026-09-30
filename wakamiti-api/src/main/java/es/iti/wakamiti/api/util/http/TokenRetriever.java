/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util.http;


/**
 * Retrieves a token using a provider configuration and a request-specific
 * value.
 *
 * @param <C> the provider configuration type
 * @param <R> the request value required to retrieve a token
 */
@FunctionalInterface
public interface TokenRetriever<C extends TokenProviderConfig<C>, R> {

    /**
     * Retrieves a token.
     *
     * @param configuration the validated provider configuration
     * @param request       request-specific retrieval data
     * @return the retrieved token
     */
    String get(
            C configuration,
            R request
    );

}
