/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api.extensions;


import iti.commons.configurer.Configuration;
import iti.commons.jext.ExtensionPoint;
import iti.kukumo.api.plan.PlanNodeBuilder;


@ExtensionPoint
public interface PlanTransformer extends Contributor {

    /** Transform a plan */
    PlanNodeBuilder transform(PlanNodeBuilder plan, Configuration configuration);

}