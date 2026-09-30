/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util.http;


/**
 * Provides tokens using a mutable configuration and request-specific data.
 *
 * @param <C> the provider configuration type
 * @param <R> the request value required to obtain a token
 */
public interface TokenProvider<C extends TokenProviderConfig<C>, R> {

    /**
     * Returns the live configuration used by this provider.
     *
     * @return this provider's mutable configuration
     */
    C configuration();

    /**
     * Obtains a token for the supplied request.
     *
     * @param request request-specific retrieval data
     * @return the retrieved token
     */
    String getToken(
            R request
    );

}
