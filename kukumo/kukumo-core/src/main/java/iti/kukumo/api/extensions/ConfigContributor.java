/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api.extensions;


import iti.commons.configurer.Configuration;
import iti.commons.configurer.Configurer;
import iti.commons.jext.ExtensionPoint;


@ExtensionPoint
public interface ConfigContributor<T> extends Contributor {

    /** @return <tt>true</tt> if the configurator can configure the given object */
    boolean accepts(Object contributor);

    Configuration defaultConfiguration();

    Configurer<T> configurer();

}
