/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.extensions;


import es.iti.commons.jext.ExtensionPoint;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.Configurer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


/**
 * Defines a contract for configuring any component with the global configuration settings.
 *
 * @param <T> The class of the component to configure
 * @author Luis Iñesta Gelabert - linesta@iti.es
 * @author Maria Galbis Calomarde - mgalbis@iti.es
 */
@ExtensionPoint
public interface ConfigContributor<T> extends Contributor {

    /**
     * Checks if this configurer can configure the given object. It verifies
     * if the class of the given object matches the {@code <T>} parameter of this interface.
     *
     * @param contributor The {@link Contributor} to configure
     * @return {@code true} if the configurator is capable of configuring the given object,
     * {@code false} otherwise
     */
    default boolean accepts(Object contributor) {
        for (Type thisInterface : this.getClass().getGenericInterfaces()) {
            if (thisInterface instanceof ParameterizedType
                    && ((ParameterizedType) thisInterface).getRawType() == ConfigContributor.class) {
                Type thisT = ((ParameterizedType) thisInterface).getActualTypeArguments()[0];
                if (thisT instanceof Class && (((Class<?>) thisT).isAssignableFrom(contributor.getClass()))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Retrieves the default configuration settings for the object being
     * configured.
     *
     * @return The default configuration
     */
    Configuration defaultConfiguration();

    /**
     * Retrieves a {@link Configurer<T>}, which is responsible for performing
     * the configuration of objects of type {@code <T>}.
     *
     * @return The configurer for objects of type {@code <T>}
     */
    Configurer<T> configurer();

}