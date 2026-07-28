/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.extensions;


import es.iti.commons.jext.ExtensionPoint;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.plan.PlanNodeBuilder;


/**
 * This interface defines a contract for contributors that transform plans
 * for processing resources.
 *
 */
@ExtensionPoint
public interface PlanTransformer extends Contributor {

    /**
     * Transforms a plan using the specified configuration.
     *
     * @param plan          The original plan to be transformed.
     * @param configuration The configuration to apply during the transformation.
     * @return A {@link PlanNodeBuilder} representing the transformed plan.
     */
    PlanNodeBuilder transform(
            PlanNodeBuilder plan,
            Configuration configuration
    );

}
