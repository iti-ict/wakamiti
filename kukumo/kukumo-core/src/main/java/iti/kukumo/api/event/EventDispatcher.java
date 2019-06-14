package iti.kukumo.api.event;

import java.time.Clock;
import java.util.concurrent.CopyOnWriteArraySet;

import iti.kukumo.api.extensions.EventObserver;

/**
 * @author ITI
 *         Created by ITI on 2/01/19
 */
public class EventDispatcher {

 
    private final CopyOnWriteArraySet<EventObserver> observers = new CopyOnWriteArraySet<>();
    private final Clock clock = Clock.systemUTC();


    public void addObserver(EventObserver observer) {
        this.observers.add(observer);
    }

    public void removeObserver(EventObserver observer) {
        this.observers.remove(observer);
    }

    
    public <T> void publishEvent(String type, T data) {
        Event<?> event = new Event<>(type, clock.instant(), data);
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
