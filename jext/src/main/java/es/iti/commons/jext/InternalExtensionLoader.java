/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.commons.jext;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;


/**
 * An internal implementation of the {@link ExtensionLoader} interface used for loading
 * extensions. This loader delegates the loading process to {@link ServiceLoader}.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
class InternalExtensionLoader implements ExtensionLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalExtensionLoader.class);

    /**
     * Loads instances of the specified type using the {@link ServiceLoader}.
     *
     * @param type   The type of extension to load
     * @param loader The class loader to be used
     * @param <T>    The type of the extension
     * @return An Iterable of loaded extensions or an empty list in case of an error
     */
    @Override
    public <T> Iterable<T> load(Class<T> type, ClassLoader loader) {
        try {
            // dynamically declaration of 'use' directive, otherwise it will cause an error
            InternalExtensionLoader.class.getModule().addUses(type);
            return ServiceLoader.load(type, loader);
        } catch (ServiceConfigurationError e) {
            LOGGER.debug(e.toString());
            return List.of();
        }
    }

    @Override
    public String toString() {
        return "Built-in extension loader";
    }

}