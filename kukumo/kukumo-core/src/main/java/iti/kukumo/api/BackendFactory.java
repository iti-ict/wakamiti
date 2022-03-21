/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api;


import iti.commons.configurer.Configuration;
import iti.kukumo.api.plan.NodeType;
import iti.kukumo.api.plan.PlanNode;


public interface BackendFactory {

	/**
	 * Create a new backend for a node of type {@link NodeType#TEST_CASE} and
	 * a given configuration
	 * @param node The test case node
	 * @param configuration The test case configuration
	 * @return A new backend
	 */
    Backend createBackend(PlanNode node, Configuration configuration);

    /**
     * Create a new <b>non-runnable</b> backend based on a given configuration,
     * limited to step-related data providing. It is useful to expose that
     * information to third-party components (like completion tools) without compromising
     * the test plan execution.
     * The resulting backend would throw {@link UnsupportedOperationException} if a test
     * case is attempted to be run.
     * @param configuration
     * @return
     */
	Backend createNonRunnableBackend(Configuration configuration);


	Hinter createHinter(Configuration configuration);

}