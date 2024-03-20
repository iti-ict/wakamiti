/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api;


/**
 * An exception class specific to Wakamiti, indicating that a test
 * case or step has been skipped.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class WakamitiSkippedException extends WakamitiException {

    public WakamitiSkippedException() {
        super();
    }

    /**
     * Constructs a new WakamitiSkippedException with the specified detail message and cause.
     *
     * @param message   The detail message (which is saved for later retrieval by the
     *                  {@link #getMessage()} method).
     * @param throwable The cause (which is saved for later retrieval by the
     *                  {@link #getCause()} method). (A null value is permitted, and
     *                  indicates that the cause is nonexistent or unknown.)
     */
    public WakamitiSkippedException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * Constructs a new WakamitiSkippedException with the specified detail message.
     *
     * @param message The detail message (which is saved for later retrieval by the
     *                {@link #getMessage()} method).
     */
    public WakamitiSkippedException(String message) {
        super(message);
    }

    /**
     * Constructs a new WakamitiSkippedException with a formatted detail message.
     *
     * @param message The detail message format string.
     * @param args    The arguments referenced by the format specifiers in the format
     *                string. If there are more arguments than format specifiers, the
     *                extra arguments are ignored. The number of arguments is variable
     *                and may be zero.
     */
    public WakamitiSkippedException(String message, Object... args) {
        super(message, args);
    }

    /**
     * Constructs a new WakamitiSkippedException with the specified cause and a detail
     * message of ({@code cause==null ? null : cause.toString()}) (which typically contains
     * the class and detail message of cause). This constructor is useful for
     * exceptions that are little more than wrappers for other throwables.
     *
     * @param throwable The cause (which is saved for later retrieval by the
     *                  {@link #getCause()} method). (A null value is permitted, and
     *                  indicates that the cause is nonexistent or unknown.)
     */
    public WakamitiSkippedException(Throwable throwable) {
        super(throwable);
    }

}