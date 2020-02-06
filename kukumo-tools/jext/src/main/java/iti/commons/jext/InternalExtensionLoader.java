/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.commons.jext;


import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



class InternalExtensionLoader implements ExtensionLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalExtensionLoader.class);

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
