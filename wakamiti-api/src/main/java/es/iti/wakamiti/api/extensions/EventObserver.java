/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.api.extensions;


import es.iti.commons.jext.ExtensionPoint;
import es.iti.wakamiti.api.event.Event;



@ExtensionPoint
public interface EventObserver extends Contributor {

    void eventReceived(Event event);

    boolean acceptType(String eventType);
}