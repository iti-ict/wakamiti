/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.exception;

import static es.iti.wakamiti.database.jdbc.LogUtils.message;

public class SQLRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -3879393806890615797L;

    public SQLRuntimeException(Throwable e) {
        super(e);
    }

    public SQLRuntimeException(String message) {
        super(message);
    }

    public SQLRuntimeException(String message, Object... args) {
        super(message(message, args));
    }

    public SQLRuntimeException(String message, Throwable e) {
        super(message, e);
    }

}
