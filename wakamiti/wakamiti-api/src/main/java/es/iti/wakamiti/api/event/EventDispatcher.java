/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.api.event;


import es.iti.wakamiti.api.extensions.EventObserver;

import java.time.Clock;
import java.util.concurrent.CopyOnWriteArraySet;


public class EventDispatcher {

    private final CopyOnWriteArraySet<EventObserver> observers = new CopyOnWriteArraySet<>();
    private final Clock clock = Clock.systemUTC();


    public void addObserver(EventObserver observer) {
        this.observers.add(observer);
    }


    public void removeObserver(EventObserver observer) {
        this.observers.remove(observer);
    }


    public  void publishEvent(String type, Object data) {
        Event event = new Event(type, clock.instant(), data);
        for (EventObserver observer : this.observers) {
            if (observer.acceptType(type)) {
                observer.eventReceived(event);
            }
        }
    }


    public Iterable<EventObserver> observers() {
        return observers;
    }

}