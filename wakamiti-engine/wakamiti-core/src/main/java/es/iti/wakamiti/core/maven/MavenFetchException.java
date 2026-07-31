/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.maven;


import java.io.Serial;


/**
 * Simple runtime exception that wraps other errors occurred during a fetching operation
 */
public class MavenFetchException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Wraps an underlying resolver or transport failure.
     *
     * @param e original failure
     */
    public MavenFetchException(
            Throwable e
    ) {
        super(e);
    }

    /**
     * Adds fetch-operation context to an underlying failure.
     *
     * @param message diagnostic context
     * @param e       original failure
     */
    public MavenFetchException(
            String message,
            Throwable e
    ) {
        super(message, e);
    }

    /**
     * Creates a fetch failure without a lower-level cause.
     *
     * @param message diagnostic description
     */
    public MavenFetchException(
            String message
    ) {
        super(message);
    }

}
