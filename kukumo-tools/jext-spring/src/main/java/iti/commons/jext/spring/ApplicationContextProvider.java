package iti.commons.jext.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextProvider  implements ApplicationContextAware  {

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
