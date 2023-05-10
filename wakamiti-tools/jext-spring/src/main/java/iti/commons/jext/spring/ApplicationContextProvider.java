/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.commons.jext.spring;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;


@Component
public class ApplicationContextProvider implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationContextProvider.class);
    private static ApplicationContext applicationContext;


    public static ApplicationContext applicationContext() {
        return applicationContext;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        LOGGER.debug("Spring ApplicationContext set in jExt Spring");
        ApplicationContextProvider.applicationContext = applicationContext;
    }


    public static boolean hasContext() {
        return ApplicationContextProvider.applicationContext != null;
    }
}