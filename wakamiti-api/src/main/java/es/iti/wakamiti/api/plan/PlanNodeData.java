/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.plan;


import java.util.function.UnaryOperator;


/**
 * Represents data associated with a {@link PlanNode}.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public interface PlanNodeData {

    /**
     * Creates a copy of the PlanNodeData.
     *
     * @return A new instance of PlanNodeData with the same data.
     */
    PlanNodeData copy();

    /**
     * Creates a copy of the PlanNodeData, replacing variables in the data.
     *
     * @param replacer The UnaryOperator to apply to each variable in the data.
     * @return A new instance of PlanNodeData with variables replaced.
     */
    PlanNodeData copyReplacingVariables(UnaryOperator<String> replacer);

}