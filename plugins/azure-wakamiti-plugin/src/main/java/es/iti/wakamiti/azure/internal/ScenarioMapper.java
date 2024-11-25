/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.internal;


import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.azure.AzureSynchronizer;
import es.iti.wakamiti.azure.api.model.TestSuite;

import java.util.stream.Stream;


/**
 * A specific implementation of {@link Mapper} for handling Gherkin scenarios.
 * <p>
 * This class maps Gherkin scenario nodes to Azure test artifacts, such as test cases and suites.
 * It customizes the mapping logic to accommodate scenario-specific requirements.
 * </p>
 */
public class ScenarioMapper extends Mapper {

    /**
     * Constructs a ScenarioMapper with a specified base directory for test suites.
     *
     * @param suiteBase the base directory for mapping test suites.
     */
    public ScenarioMapper(String suiteBase) {
        super(suiteBase);
    }

    /**
     * Maps a Gherkin scenario node to a stream of test suite and plan node pairs.
     * <p>
     * Overrides the base {@link Mapper#suiteMap(PlanNodeSnapshot)} method to handle
     * scenario-specific mapping logic. Includes parent suite relationships where necessary.
     * </p>
     *
     * @param target the {@link PlanNodeSnapshot} representing the node to map.
     * @return a stream of {@link Pair} objects containing the plan node and associated test suite.
     */
    @Override
    protected Stream<Pair<PlanNodeSnapshot, TestSuite>> suiteMap(PlanNodeSnapshot target) {
        return super.suiteMap(target)
                .flatMap(p ->
                        p.key().flatten(node -> gherkinType(node).equals(type()))
                                .map(node -> new Pair<>(node, target.getProperties().containsKey(AZURE_SUITE) ?
                                        p.value() : new TestSuite().name(p.key().getName()).parent(p.value())))
                );
    }

    /**
     * Returns the type of this mapper as a string.
     *
     * @return the type of the mapper, which is {@code "scenario"}.
     */
    @Override
    public String type() {
        return AzureSynchronizer.GHERKIN_TYPE_SCENARIO;
    }

}
