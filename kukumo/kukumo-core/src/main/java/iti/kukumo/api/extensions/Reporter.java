package iti.kukumo.api.extensions;

import iti.commons.jext.ExtensionPoint;
import iti.kukumo.api.plan.PlanNodeDescriptor;

/**
 * @author ITI
 *         Created by ITI on 2/01/19
 */
@ExtensionPoint
public interface Reporter extends Contributor {

    
    /**
     * Perform the report operation on the given iti.kukumo.test.gherkin.plan node descriptor.
     * @param rootNode The root node descriptor. It may be a standalone iti.kukumo.test.gherkin.plan or a root node grouping several plans.
     */
    public void report(PlanNodeDescriptor rootNode);
    
    
    
}
