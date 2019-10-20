/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api.extensions;


import iti.commons.jext.ExtensionPoint;
import iti.commons.jext.LoadStrategy;


@ExtensionPoint(loadStrategy = LoadStrategy.FRESH)
public interface StepContributor extends Contributor {

}
