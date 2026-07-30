/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.internal;


import es.iti.wakamiti.api.WakamitiException;


/**
 * Exception raised by the Wakamiti Azure Exception component.
 */
public class WakamitiAzureException extends WakamitiException {

    private static final String MESSAGE = System.lineSeparator()
            + "Please try to fix this problem in azure.";

    /**
     * Creates an Azure synchronization exception with remediation guidance.
     *
     * @param message problem description
     */
    public WakamitiAzureException(
            String message
    ) {
        super(message + MESSAGE);
    }

    /**
     * Creates an Azure synchronization exception retaining its root cause.
     *
     * @param message problem description
     * @param cause underlying API, mapping or transport failure
     */
    public WakamitiAzureException(
            String message,
            Throwable cause
    ) {
        super(message + MESSAGE, cause);
    }

    /**
     * Creates a formatted Azure synchronization exception.
     *
     * @param message message template understood by {@link WakamitiException}
     * @param args template arguments
     */
    public WakamitiAzureException(
            String message,
            Object... args
    ) {
        super(message + MESSAGE, args);
    }

}
