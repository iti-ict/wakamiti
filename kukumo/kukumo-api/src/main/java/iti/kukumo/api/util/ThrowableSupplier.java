/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package iti.kukumo.api.util;

import iti.kukumo.api.KukumoException;

import java.util.function.Supplier;

public interface ThrowableSupplier<T> extends Supplier<T> {

    @Override
    default T get() {
        try {
            return getThrowable();
        } catch (Exception e) {
            throw new KukumoException(e);
        }
    }

    T getThrowable();

}
