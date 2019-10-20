/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.core.backend;


import iti.kukumo.api.KukumoException;
import iti.kukumo.api.plan.PlanNode;


public class UndefinedStepException extends KukumoException {

    private static final long serialVersionUID = 5923513040489649029L;


    public UndefinedStepException(PlanNode step, String message, String extraInfo) {
        super(
            "Cannot match step at <{}> '{}' : {}\n{}", step.source(), step.name(), message,
            extraInfo
        );
    }

}
