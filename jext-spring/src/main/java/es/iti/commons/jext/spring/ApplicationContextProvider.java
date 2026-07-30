/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.commons.jext.spring;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;


/**
 * Exposes the Spring {@link ApplicationContext} through a static holder.
 * <p>
 * This bridge is useful for infrastructure code that cannot receive normal
 * dependency injection. The stored context is process-wide and mutable, so it
 * should be treated as global state.
 * </p>
 */
@Component
public class ApplicationContextProvider implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationContextProvider.class);
    private static ApplicationContext applicationContext;

    /**
     * Returns the currently stored application context.
     *
     * @return Spring context, or {@code null} when the container has not called
     *         {@link #setApplicationContext(ApplicationContext)} yet
     */
    public static ApplicationContext applicationContext() {
        return applicationContext;
    }

    /**
     * Indicates whether a Spring context has already been stored.
     *
     * @return {@code true} when a context has been assigned
     */
    public static boolean hasContext() {
        return ApplicationContextProvider.applicationContext != null;
    }

    /**
     * Stores the context provided by Spring at startup.
     *
     * @param applicationContext active Spring application context
     */
    @Override
    public void setApplicationContext(
            ApplicationContext applicationContext
    ) {
        LOGGER.debug("Spring ApplicationContext set in jExt Spring");
        ApplicationContextProvider.applicationContext = applicationContext;
    }

}
