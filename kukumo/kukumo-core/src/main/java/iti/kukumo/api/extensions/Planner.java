package iti.kukumo.api.extensions;

import iti.commons.jext.ExtensionPoint;
import iti.kukumo.api.Resource;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.api.plan.PlanStep;

import java.util.List;

/**
 * This interface defines the methods required in order to instantiate a Kukumo model iti.kukumo.test.gherkin.plan from a set of Gherkin
 * resources.
 * @author luinge@gmail.com
 *
 */
@ExtensionPoint
public interface Planner extends Contributor {


    boolean acceptResourceType(ResourceType<?> resourceType);

    /**
     * Instantiate a new Kukumo model iti.kukumo.test.gherkin.plan according the resources provided.
     * @param resources A list of resources
     * @return A new Kukumo model iti.kukumo.test.gherkin.plan, without any execution data
     */
    PlanNode createPlan(List<Resource<?>> resources);

    /**
     * Returns a special step that will be used for redefining steps that do nothing.
     * <p>This step satisfies that the method {@link PlanStep#isVoid} always returns <tt>true</tt>.</p>
     * @return A step with no definition and no implementation
     */
    PlanStep voidStep();





}
