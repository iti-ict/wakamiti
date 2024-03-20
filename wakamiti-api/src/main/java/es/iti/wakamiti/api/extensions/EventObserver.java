/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.extensions;


import es.iti.commons.jext.ExtensionPoint;
import es.iti.wakamiti.api.event.Event;


/**
 * This interface defines a contract for observers that handle Wakamiti events.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 * @see Event
 */
@ExtensionPoint
public interface EventObserver extends Contributor {

    /**
     * Handles the received Wakamiti event.
     *
     * @param event The received event.
     */
    void eventReceived(Event event);

    /**
     * Determines whether the observer accepts events of the specified type.
     *
     * @param eventType The type of the event.
     * @return {@code true} if the observer accepts events of the specified type, {@code false} otherwise.
     */
    boolean acceptType(String eventType);
}