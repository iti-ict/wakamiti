/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api.extensions;


import iti.commons.jext.ExtensionPoint;
import iti.kukumo.api.event.Event;



@ExtensionPoint
public interface EventObserver extends Contributor {

    void eventReceived(Event event);

    boolean acceptType(String eventType);
}
