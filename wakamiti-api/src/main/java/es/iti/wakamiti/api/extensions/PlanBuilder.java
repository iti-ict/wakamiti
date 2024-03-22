/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.extensions;


import es.iti.commons.jext.ExtensionPoint;
import es.iti.commons.jext.LoadStrategy;
import es.iti.wakamiti.api.Resource;
import es.iti.wakamiti.api.plan.PlanNodeBuilder;

import java.util.List;


/**
 * This interface defines the methods required to instantiate a
 * Wakamiti model {@link es.iti.wakamiti.api.plan} from a set of
 * Gherkin resources.
 *
 * @author @author Luis Iñesta Gelabert - linesta@iti.es
 */
@ExtensionPoint(loadStrategy = LoadStrategy.FRESH)
public interface PlanBuilder extends Contributor {

    /**
     * Determines whether the plan builder accepts the specified resource type.
     *
     * @param resourceType The resource type to check.
     * @return {@code true} if the plan builder accepts the specified resource
     * type, {@code false} otherwise.
     */
    boolean acceptResourceType(ResourceType<?> resourceType);


    /**
     * Creates a plan using the provided list of resources.
     *
     * @param resources The list of resources for the plan.
     * @return A {@link PlanNodeBuilder} representing the created plan.
     */
    PlanNodeBuilder createPlan(List<Resource<?>> resources);

}