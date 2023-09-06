/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.core.backend;


import java.util.List;

import imconfig.Configuration;
import es.iti.wakamiti.api.WakamitiDataTypeRegistry;
import es.iti.wakamiti.api.plan.PlanNode;

/*
 * This implementation of Backend does not allow to run tests. Its main purpose
 * is just offer information about available steps to third-party components
 * (like completion tools)
 */
public class NonRunnableBackend extends AbstractBackend {


    public NonRunnableBackend(
        Configuration configuration,
        WakamitiDataTypeRegistry typeRegistry,
        List<RunnableStep> steps
    ) {
        super(configuration,typeRegistry,steps);
    }



    @Override
    public void runStep(PlanNode step) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void setUp() {
        // nothing
    }


    @Override
    public void tearDown() {
        // nothing
    }


}