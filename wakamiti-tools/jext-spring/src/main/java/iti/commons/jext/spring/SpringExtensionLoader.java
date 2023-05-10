/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.commons.jext.spring;


import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iti.commons.jext.ExtensionLoader;



public class SpringExtensionLoader implements ExtensionLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringExtensionLoader.class);


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