/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.wakamiti.api.extensions;


import iti.commons.jext.ExtensionPoint;
import iti.commons.jext.LoadStrategy;
import iti.wakamiti.api.Resource;
import iti.wakamiti.api.plan.PlanNodeBuilder;

import java.util.List;


/** This interface defines the methods required in order to instantiate a * Wakamiti model iti.wakamiti.test.gherkin.plan from a set of Gherkin resources.* @author luinge@gmail.com*/
@ExtensionPoint(loadStrategy = LoadStrategy.FRESH)
public interface PlanBuilder extends Contributor {

    boolean acceptResourceType(ResourceType<?> resourceType);


    /**
     * Instantiate a new Wakamiti model plan according the resources provided.
     *
     * @param resources A list of resources
     * @return A new Wakamiti model plan
     */
    PlanNodeBuilder createPlan(List<Resource<?>> resources);

}