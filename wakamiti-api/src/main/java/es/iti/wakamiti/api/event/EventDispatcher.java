/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.event;


import es.iti.wakamiti.api.extensions.EventObserver;

import java.time.Clock;
import java.util.concurrent.CopyOnWriteArraySet;


/**
 * The {@code EventDispatcher} class manages the distribution of events to registered
 * {@link EventObserver} instances. It allows adding and removing observers and publishing
 * events to those observers.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class EventDispatcher {

    private final CopyOnWriteArraySet<EventObserver> observers = new CopyOnWriteArraySet<>();
    private final Clock clock = Clock.systemUTC();

    /**
     * Adds an {@link EventObserver} to the list of observers.
     *
     * @param observer The observer to add.
     */
    public void addObserver(EventObserver observer) {
        this.observers.add(observer);
    }

    /**
     * Removes an {@link EventObserver} from the list of observers.
     *
     * @param observer The observer to remove.
     */
    public void removeObserver(EventObserver observer) {
        this.observers.remove(observer);
    }

    /**
     * Publishes an event to all registered observers that accept the specified event type.
     *
     * @param type The type of the event.
     * @param data The data associated with the event.
     */
    public void publishEvent(String type, Object data) {
        Event event = new Event(type, clock.instant(), data);
        for (EventObserver observer : this.observers) {
            if (observer.acceptType(type)) {
                observer.eventReceived(event);
            }
        }
    }

    /**
     * Gets an Iterable containing all registered observers.
     *
     * @return An Iterable of {@link EventObserver} instances.
     */
    public Iterable<EventObserver> observers() {
        return observers;
    }

}