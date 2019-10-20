/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api.extensions;


import java.util.List;

import iti.commons.jext.ExtensionPoint;
import iti.kukumo.api.KukumoDataType;


@ExtensionPoint
public interface DataTypeContributor extends Contributor {

    List<KukumoDataType<?>> contributeTypes();

}
