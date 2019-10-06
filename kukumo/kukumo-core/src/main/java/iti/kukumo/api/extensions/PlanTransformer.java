package iti.kukumo.api.extensions;

import iti.commons.jext.ExtensionPoint;
import iti.kukumo.api.plan.PlanNodeBuilder;

@ExtensionPoint
public interface PlanTransformer {

    /** Transform a plan */
    public PlanNodeBuilder transform(PlanNodeBuilder plan);

}
