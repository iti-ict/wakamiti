/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.wakamiti.api.plan;


import java.util.function.UnaryOperator;



public interface PlanNodeData {

    PlanNodeData copy();

    PlanNodeData copyReplacingVariables(UnaryOperator<String> replacer);

}