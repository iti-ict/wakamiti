/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api.extensions;



import imconfig.Configuration;
import imconfig.Configurer;
import iti.commons.jext.ExtensionPoint;


@ExtensionPoint
public interface ConfigContributor<T> extends Contributor {

    /** @return <tt>true</tt> if the configurator can configure the given object */
    boolean accepts(Object contributor);

    Configuration defaultConfiguration();

    Configurer<T> configurer();

}