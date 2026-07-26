/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.maven;

/**
 * Simple runtime exception that wraps other errors occurred during a fetching operation
 */
public class MavenFetchException extends RuntimeException{

    private static final long serialVersionUID = 1L;


    public MavenFetchException(Throwable e) {
        super(e);
    }

    public MavenFetchException(String message, Throwable e) {
        super(message, e);
    }

    public MavenFetchException(String message) {
        super(message);
    }

}
