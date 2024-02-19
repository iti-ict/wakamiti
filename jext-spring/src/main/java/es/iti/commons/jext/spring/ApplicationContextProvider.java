/*
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
 * Provides a static method to access the Spring {@link ApplicationContextAware} and a utility method to check
 * whether the {@link ApplicationContextAware} has been set.
 * <p>
 * This class is intended to be used in Spring applications where access to the ApplicationContext is required
 * outside of the Spring container, for example, in non-Spring managed classes.
 * </p>
 *
 * @author Luis Iñesta Gelabert
 */
@Component
public class ApplicationContextProvider implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationContextProvider.class);
    private static ApplicationContext applicationContext;

    /**
     * Gets the Spring ApplicationContext.
     *
     * @return The Spring ApplicationContext.
     */
    public static ApplicationContext applicationContext() {
        return applicationContext;
    }

    /**
     * Checks if the ApplicationContext has been set.
     *
     * @return {@code true} if the ApplicationContext is set, {@code false}
     *         otherwise.
     */
    public static boolean hasContext() {
        return ApplicationContextProvider.applicationContext != null;
    }

    /**
     * Sets the Spring ApplicationContext when invoked by the Spring framework.
     *
     * @param applicationContext The Spring ApplicationContext to be set.
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        LOGGER.debug("Spring ApplicationContext set in jExt Spring");
        ApplicationContextProvider.applicationContext = applicationContext;
    }

}