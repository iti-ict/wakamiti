/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api.extensions;


import iti.commons.jext.ExtensionPoint;
import iti.kukumo.api.plan.PlanNodeSnapshot;



@ExtensionPoint
public interface Reporter extends Contributor {

    /**
     * Perform the report operation on the given iti.kukumo.test.gherkin.plan
     * node descriptor.
     *
     * @param rootNode The root node descriptor. It may be a standalone
     *                 iti.kukumo.test.gherkin.plan or a root node grouping
     *                 several plans.
     */
    void report(PlanNodeSnapshot rootNode);

}