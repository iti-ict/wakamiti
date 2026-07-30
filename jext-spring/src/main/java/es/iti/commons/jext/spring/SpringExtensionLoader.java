/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.commons.jext.spring;


import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.iti.commons.jext.ExtensionLoader;


/**
 * {@link ExtensionLoader} implementation backed by the Spring application
 * context.
 * <p>
 * Extensions are resolved as Spring beans of the requested type and returned
 * as already-managed instances.
 * </p>
 */
public class SpringExtensionLoader implements ExtensionLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringExtensionLoader.class);

    /**
     * Loads extension instances from the current Spring context.
     *
     * @param type   extension contract type to resolve
     * @param loader ignored (bean resolution is delegated to Spring)
     * @param <T>    extension type
     * @return resolved beans, or an empty collection when no context exists
     */
    @Override
    public <T> Iterable<T> load(
            Class<T> type,
            ClassLoader loader
    ) {
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
