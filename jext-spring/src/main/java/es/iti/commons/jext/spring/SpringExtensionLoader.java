/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.commons.jext.spring;


import es.iti.commons.jext.ExtensionLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;


/**
 * The {@code SpringExtensionLoader} class implements the {@link ExtensionLoader} interface
 * and provides a mechanism for loading extensions using the Spring framework.
 * <p>
 * It leverages the {@link ApplicationContextProvider} to obtain beans of a specified type
 * from the Spring application context.
 * </p>
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class SpringExtensionLoader implements ExtensionLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringExtensionLoader.class);

    /**
     * Loads extensions of the specified type from the Spring application context.
     *
     * @param type   The type of extensions to load.
     * @param loader The class loader to use (not used in this implementation).
     * @param <T>    The type parameter for the extensions.
     * @return An Iterable containing the loaded extensions.
     */
    @Override
    public <T> Iterable<T> load(Class<T> type, ClassLoader loader) {
        if (ApplicationContextProvider.hasContext()) {
            LOGGER.trace("Getting beans of type {}...", type);
            Collection<T> beans = ApplicationContextProvider.applicationContext()
                    .getBeansOfType(type).values();
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(
                        "{} beans found [{}]",
                        beans.size(),
                        beans.stream()
                                .map(Object::getClass)
                                .map(Class::getCanonicalName)
                                .collect(Collectors.joining(", "))
                );
            }
            return beans;
        } else {
            LOGGER.warn(
                    "Trying to load extension but ApplicationContextProvider has not been set yet!"
            );
            return Collections.emptyList();
        }
    }

}