package iti.kukumo.api.extensions;

import iti.commons.jext.ExtensionPoint;
import iti.commons.jext.LoadStrategy;
import iti.kukumo.api.Resource;
import iti.kukumo.api.plan.PlanNode;

import java.util.List;

/**
 * This interface defines the methods required in order to instantiate a Kukumo model iti.kukumo.test.gherkin.plan from a set of Gherkin
 * resources.
 * @author luinge@gmail.com
 *
 */
@ExtensionPoint(loadStrategy = LoadStrategy.FRESH)
public interface Planner extends Contributor {


    boolean acceptResourceType(ResourceType<?> resourceType);

    /**
     * Instantiate a new Kukumo model iti.kukumo.test.gherkin.plan according the resources provided.
     * @param resources A list of resources
     * @return A new Kukumo model iti.kukumo.test.gherkin.plan, without any execution data
     */
    PlanNode createPlan(List<Resource<?>> resources);






}
