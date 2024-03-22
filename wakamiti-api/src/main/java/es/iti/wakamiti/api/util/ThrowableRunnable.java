/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util;


/**
 * A functional interface representing a runnable operation
 * that may throw checked exceptions.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
@FunctionalInterface
public interface ThrowableRunnable {

    /**
     * Runs the operation, potentially throwing a checked exception.
     *
     * @param arguments The arguments for the operation (if any).
     * @return The result of the operation.
     * @throws Exception If an exception occurs during the operation.
     */
    Object run(Object... arguments) throws Exception;

}