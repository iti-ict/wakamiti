/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api.extensions;


import java.util.List;

import iti.commons.jext.ExtensionPoint;
import iti.commons.jext.LoadStrategy;
import iti.kukumo.api.Resource;
import iti.kukumo.api.plan.PlanNodeBuilder;


/** This interface defines the methods required in order to instantiate a * Kukumo model iti.kukumo.test.gherkin.plan from a set of Gherkin resources.* @author luinge@gmail.com*/
@ExtensionPoint(loadStrategy = LoadStrategy.FRESH)
public interface PlanBuilder extends Contributor {

    boolean acceptResourceType(ResourceType<?> resourceType);


    /**
     * Instantiate a new Kukumo model plan according the resources provided.
     *
     * @param resources A list of resources
     * @return A new Kukumo model plan
     */
    PlanNodeBuilder createPlan(List<Resource<?>> resources);

}