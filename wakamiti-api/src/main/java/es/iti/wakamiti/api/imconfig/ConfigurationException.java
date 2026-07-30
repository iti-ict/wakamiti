/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.imconfig;


import java.io.Serial;


/**
 * Multi-purpose runtime exception for any error occurred during the creation of a
 * new configuration.
 */
public class ConfigurationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 7175876124782335084L;

    /**
     * Creates an exception that preserves the failure raised while loading or
     * validating configuration.
     *
     * @param throwable the underlying configuration failure
     */
    public ConfigurationException(
            Throwable throwable
    ) {
        super(throwable);
    }

    /**
     * Creates an exception with a message describing the invalid
     * configuration or failed operation.
     *
     * @param message the diagnostic message
     */
    public ConfigurationException(
            String message
    ) {
        super(message);
    }

    /**
     * Creates an exception with both contextual diagnostics and the underlying
     * failure.
     *
     * @param message   the diagnostic message
     * @param throwable the underlying configuration failure
     */
    public ConfigurationException(
            String message,
            Throwable throwable
    ) {
        super(message, throwable);
    }

}
