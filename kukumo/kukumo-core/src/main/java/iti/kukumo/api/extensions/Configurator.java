/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api.extensions;


import iti.commons.configurer.Configuration;
import iti.commons.jext.ExtensionPoint;



@ExtensionPoint
public interface Configurator<T> {

    void configure(T contributor, Configuration configuration);


    boolean accepts(Object contributor);

}
