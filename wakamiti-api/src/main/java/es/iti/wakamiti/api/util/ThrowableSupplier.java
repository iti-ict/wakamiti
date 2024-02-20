/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.util;


import es.iti.wakamiti.api.WakamitiException;

import java.util.function.Supplier;


/**
 * A functional interface representing a supplier of results
 * that may throw checked exceptions.
 *
 * @param <T> The type of the supplied result.
 * @author Maria Galbis Calomarde - mgalbis@iti.es
 */
public interface ThrowableSupplier<T> extends Supplier<T> {

    /**
     * Gets a result, potentially throwing a checked exception.
     *
     * @return The result of the supplier.
     * @throws WakamitiException If an exception occurs while
     *                           obtaining the result.
     */
    @Override
    default T get() {
        try {
            return getThrowable();
        } catch (Exception e) {
            throw new WakamitiException(e);
        }
    }

    /**
     * Gets a result, potentially throwing a checked exception.
     *
     * @return The result of the supplier.
     */
    T getThrowable();

}
