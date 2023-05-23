/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api;


import es.iti.wakamiti.api.annotations.SetUp;
import es.iti.wakamiti.api.annotations.TearDown;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The backend is the piece which actually runs the tests. It is usually created by a
 * {@link BackendFactory} from the context information regarding a test case (available
 * contributors, test case configuration, etc.).
 * A new backend instance should be created each time a test case is executed, in order to
 * ensure isolation in multi-thread environments.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
public interface Backend {

    /**
     * Perform set-up operations prior to run any step.
     * Usually, those operations correspond to {@link StepContributor} methods annotated with
     * {@link SetUp}
     */
    void setUp();


    /**
     * Perform set-up operations prior to run any step.
     * Usually, those operations correspond to {@link StepContributor} methods annotated with
     * {@link TearDown}
     */
    void tearDown();


    /**
     * Run a plan node of type {@link NodeType#STEP}, updating its execution state
     * with the results.
     *
     * @param modelStep The step to be executed.
     * @throws WakamitiException when the given node is not suitable for being executed.
     */
    void runStep(PlanNode modelStep);


    /**
     * Exposes information about the type registry used by the backend
     */
    WakamitiDataTypeRegistry getTypeRegistry();

    /**
     * Given an extra properties {@link Map} to use in the scenario context,
     * which include the "{@code results}" and the "{@code id}" of the current
     * scenario.
     *
     * @return The extra properties {@link Map}
     */
    default Map<String, Object> getExtraProperties() {
        return new LinkedHashMap<>();
    }


    /**
     * Obtain a list of all steps from any available step contributor used by the backend.
     * If a step uses data type with variations (like assertions), a new element would be
     * presented for each variation.
     *
     * @param locale            The language used to describe the steps
     * @param includeVariations Set whether every variation for data types should be included
     * @return A list with the available steps (empty if none present)
     */
    List<String> getAvailableSteps(Locale locale, boolean includeVariations);


    /**
     * Given an invalid step, obtain a list of valid suggestions. The result would be
     * sorted by string proximity with the original step, being the first element the one
     * with the higher probability to solve the problem.
     * If a step uses data type with variations (like assertions), a new element would be
     * presented for each variation.
     *
     * @param invalidStep       The original (invalid) step
     * @param locale            The language used
     * @param numberOfHints     How many suggestions should be obtained
     * @param includeVariations Set whether every variation for data types should be included
     * @return A list of suggestions (empty if none present)
     */
    List<String> getSuggestionsForInvalidStep(
            String invalidStep,
            Locale locale,
            int numberOfHints,
            boolean includeVariations
    );


    /**
     * Given an invalid step, obtain a message with the most probable solutions.
     * This is intended to be used as a part of an error message or diagnostics.
     *
     * @param wrongStep The original (invalid) step
     * @param locale    The language used
     */
    String getHintFor(String wrongStep, Locale locale);
}