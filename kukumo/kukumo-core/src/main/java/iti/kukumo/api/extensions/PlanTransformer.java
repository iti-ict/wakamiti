package iti.kukumo.api.extensions;

import iti.commons.configurer.Configuration;
import iti.commons.jext.ExtensionPoint;
import iti.kukumo.core.plan.PlanNodeBuilder;

@ExtensionPoint
public interface PlanTransformer extends Contributor {

    /** Transform a plan */
    PlanNodeBuilder transform(PlanNodeBuilder plan, Configuration configuration);

}
