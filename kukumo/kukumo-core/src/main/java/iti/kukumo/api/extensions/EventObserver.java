package iti.kukumo.api.extensions;

import iti.commons.jext.ExtensionPoint;
import iti.kukumo.api.event.Event;

/**
 * @author ITI
 *         Created by ITI on 2/01/19
 */
@ExtensionPoint
public interface EventObserver extends Contributor {

    void eventReceived (Event<?> event);

    boolean acceptType(String eventType);
}
