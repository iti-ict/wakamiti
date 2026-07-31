/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.xray.internal;


import es.iti.wakamiti.api.WakamitiException;


/**
 * Exception raised by the Wakamiti XRay Exception component.
 */
public class WakamitiXRayException extends WakamitiException {

    private static final String MESSAGE = System.lineSeparator()
            + "Please resolve this issue in XRay or ignore it to continue.";

    /**
     * Creates an Xray synchronization exception with remediation guidance appended.
     *
     * @param message description of the remote inconsistency or failed operation
     */
    public WakamitiXRayException(
            String message
    ) {
        super(message + MESSAGE);
    }

    /**
     * Creates an Xray synchronization exception that retains its original cause.
     *
     * @param message description of the remote inconsistency or failed operation
     * @param cause exception that caused the synchronization failure
     */
    public WakamitiXRayException(
            String message,
            Throwable cause
    ) {
        super(message + MESSAGE, cause);
    }

    /**
     * Creates a formatted Xray synchronization exception.
     * <p>
     * Formatting arguments are delegated to {@link WakamitiException}; the standard
     * instruction for resolving or ignoring the Xray issue is appended first.
     *
     * @param message message template describing the failure
     * @param args values referenced by the message template
     */
    public WakamitiXRayException(
            String message,
            Object... args
    ) {
        super(message + MESSAGE, args);
    }

}
