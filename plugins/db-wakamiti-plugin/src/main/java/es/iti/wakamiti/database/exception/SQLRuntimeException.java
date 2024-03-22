/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.exception;


import static es.iti.wakamiti.database.jdbc.LogUtils.message;


/**
 * Exception indicating a runtime error in SQL operations.
 */
public class SQLRuntimeException extends RuntimeException {

    /**
     * Constructs a SQLRuntimeException with the specified cause.
     *
     * @param e The cause of the exception.
     */
    public SQLRuntimeException(Throwable e) {
        super(e);
    }

    /**
     * Constructs a SQLRuntimeException with the specified detail message.
     *
     * @param message The detail message.
     */
    public SQLRuntimeException(String message) {
        super(message);
    }

    /**
     * Constructs a SQLRuntimeException with the specified detail message formatted using the provided arguments.
     *
     * @param message The detail message format string.
     * @param args    Arguments referenced by the format specifiers in the message string.
     */
    public SQLRuntimeException(String message, Object... args) {
        super(message(message, args));
    }

    /**
     * Constructs a SQLRuntimeException with the specified detail message and cause.
     *
     * @param message The detail message.
     * @param e       The cause of the exception.
     */
    public SQLRuntimeException(String message, Throwable e) {
        super(message, e);
    }

}
