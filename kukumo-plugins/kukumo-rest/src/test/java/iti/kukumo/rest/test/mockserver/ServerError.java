/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.kukumo.rest.test.mockserver;

import java.time.LocalDateTime;

public class ServerError {

    private final String exception;
    private final String message;
    private final LocalDateTime timestamp;


    public ServerError(String exception, String message, LocalDateTime timestamp) {
        this.exception = exception;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getException() {
        return exception;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}