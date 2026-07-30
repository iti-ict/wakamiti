/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.extensions;


import es.iti.commons.jext.ExtensionPoint;
import es.iti.wakamiti.api.event.Event;


/**
 * Observes Wakamiti runtime events.
 *
 * @see Event
 */
@ExtensionPoint
public interface EventObserver extends Contributor {

    /**
     * Handles one delivered event.
     * <p>
     * This callback executes in the publisher thread. Exceptions propagate to
     * the dispatcher caller unless the publisher catches them.
     * </p>
     *
     * @param event received event, never {@code null}
     */
    void eventReceived(
            Event event
    );

    /**
     * Determines whether this observer should receive a given event type.
     *
     * @param eventType event type identifier
     * @return {@code true} to receive matching events
     */
    boolean acceptType(
            String eventType
    );

}
