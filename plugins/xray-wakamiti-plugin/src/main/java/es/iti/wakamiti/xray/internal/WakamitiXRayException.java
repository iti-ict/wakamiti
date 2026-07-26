/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.xray.internal;


import es.iti.wakamiti.api.WakamitiException;


public class WakamitiXRayException extends WakamitiException {

    private static final String MESSAGE = System.lineSeparator()
            + "Please resolve this issue in XRay or ignore it to continue.";

    public WakamitiXRayException(
            String message
    ) {
        super(message + MESSAGE);
    }

    public WakamitiXRayException(
            String message,
            Throwable cause
    ) {
        super(message + MESSAGE, cause);
    }

    public WakamitiXRayException(
            String message,
            Object... args
    ) {
        super(message + MESSAGE, args);
    }

}
