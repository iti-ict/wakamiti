package iti.commons.jext.spring;

import iti.commons.jext.ExtensionLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * @author ITI
 * Created by ITI on 15/05/19
 */
public class SpringExtensionLoader implements ExtensionLoader  {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringExtensionLoader.class);

    @Override
    public <T> Iterable<T> load(Class<T> type, ClassLoader loader) {
    	if (ApplicationContextProvider.hasContext()) {
    	    LOGGER.trace("Getting beans of type {}...", type);
            Collection<T> beans = ApplicationContextProvider.applicationContext().getBeansOfType(type).values();
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
   	        LOGGER.warn("Trying to load extension but ApplicationContextProvider has not been set yet!");
    		return Collections.emptyList();
    	}
    }
}
