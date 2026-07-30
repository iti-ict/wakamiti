/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.extensions;


import es.iti.commons.jext.ExtensionPoint;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;


/**
 * Generates output artifacts from an executed plan snapshot.
 */
@ExtensionPoint
public interface Reporter extends Contributor {

    /**
     * Produces report output for the supplied execution tree.
     * <p>
     * Implementations should treat {@code rootNode} as read-only and are free
     * to perform blocking I/O. Runtime failures should be propagated so the
     * caller can decide whether report generation is optional or fatal.
     * </p>
     *
     * @param rootNode executed plan snapshot; may represent one plan or an
     *                 aggregator root
     */
    void report(
            PlanNodeSnapshot rootNode
    );

}
