/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.internal;


import es.iti.wakamiti.api.WakamitiException;


public class WakamitiAzureException extends WakamitiException {

    private static final String MESSAGE = System.lineSeparator()
            + "Please try to fix this problem in azure.";

    public WakamitiAzureException(String message) {
        super(message + MESSAGE);
    }

    public WakamitiAzureException(String message, Throwable cause) {
        super(message + MESSAGE, cause);
    }

    public WakamitiAzureException(String message, Object... args) {
        super(message + MESSAGE, args);
    }

}
