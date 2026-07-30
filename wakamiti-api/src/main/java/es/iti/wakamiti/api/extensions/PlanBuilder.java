/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.extensions;


import java.util.List;

import es.iti.commons.jext.ExtensionPoint;
import es.iti.commons.jext.LoadStrategy;
import es.iti.wakamiti.api.Resource;
import es.iti.wakamiti.api.plan.PlanNodeBuilder;


/**
 * Builds a plan model from input resources.
 * <p>
 * Builders are discovered as extensions and instantiated with
 * {@link LoadStrategy#FRESH}, so each use receives a new instance.
 * </p>
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
    boolean acceptResourceType(
            ResourceType<?> resourceType
    );

    /**
     * Parses resources and creates a plan tree.
     *
     * @param resources ordered resources to parse
     * @return mutable plan builder root
     * @throws RuntimeException when resources are malformed or cannot be parsed
     */
    PlanNodeBuilder createPlan(
            List<Resource<?>> resources
    );

}
