/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api;


import java.util.Arrays;

import static es.iti.wakamiti.api.util.StringUtils.format;


/**
 * An exception class specific to Wakamiti, providing additional features for
 * formatting messages and handling throwable.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class WakamitiException extends RuntimeException {

    private static final long serialVersionUID = 3126782976719868151L;


    public WakamitiException() {
        super();
    }

    /**
     * Constructs a new WakamitiException with the specified detail message and cause.
     *
     * @param message The detail message (which is saved for later retrieval by the
     *                {@link #getMessage()} method).
     * @param throwable The cause (which is saved for later retrieval by the
     *                  {@link #getCause()} method). (A null value is permitted, and
     *                  indicates that the cause is nonexistent or unknown.)
     */
    public WakamitiException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * Constructs a new WakamitiException with the specified detail message.
     *
     * @param message The detail message (which is saved for later retrieval by the
     *                {@link #getMessage()} method).
     */
    public WakamitiException(String message) {
        super(message);
    }

    /**
     * Constructs a new WakamitiException with a formatted detail message.
     *
     * @param message The detail message format string.
     * @param args The arguments referenced by the format specifiers in the format
     *             string. If there are more arguments than format specifiers, the
     *             extra arguments are ignored. The number of arguments is variable
     *             and may be zero.
     */
    public WakamitiException(String message, Object... args) {
        super(format(message, argsWithoutThrowable(args)), throwable(args));
    }

    /**
     * Constructs a new WakamitiException with the specified cause and a detail
     * message of ({@code cause==null ? null : cause.toString()}) (which typically contains
     * the class and detail message of cause). This constructor is useful for
     * exceptions that are little more than wrappers for other throwable.
     *
     * @param throwable The cause (which is saved for later retrieval by the
     *                  {@link #getCause()} method). (A null value is permitted, and
     *                  indicates that the cause is nonexistent or unknown.)
     */
    public WakamitiException(Throwable throwable) {
        super(throwable.getMessage(), throwable);
    }


    private static Object[] argsWithoutThrowable(Object[] args) {
        return throwable(args) == null ? args : Arrays.copyOf(args, args.length - 1);
    }


    protected static Throwable throwable(Object... args) {
        if (args == null || args.length == 0) {
            return null;
        }
        return args[args.length - 1] instanceof Throwable ? (Throwable) args[args.length - 1] : null;
    }

}