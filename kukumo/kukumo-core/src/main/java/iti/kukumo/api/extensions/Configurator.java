package iti.kukumo.api.extensions;

import iti.commons.configurer.Configuration;
import iti.commons.jext.ExtensionPoint;

/**
 * @author ITI
 *         Created by ITI on 12/03/19
 */
@ExtensionPoint
public interface Configurator<T> {

    void configure(T contributor, Configuration configuration);

    boolean accepts(Object contributor);

}
