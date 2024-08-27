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
 * Represents the backend responsible for executing tests.
 * Each test case execution should have a new instance of the
 * backend to ensure isolation in multi-thread environments.
 * The backend is created by a BackendFactory based on the
 * context information regarding a test case.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public interface Backend {

    String UNNAMED_ARG = "unnamed";
    String DOCUMENT_ARG = "document";
    String DATATABLE_ARG = "datatable";

    /**
     * Performs set-up operations prior to running any step.
     * Typically, these operations correspond to methods
     * annotated with {@link SetUp} in {@link StepContributor}.
     */
    void setUp();

    /**
     * Performs tear-down operations after running all steps.
     * Typically, these operations correspond to methods
     * annotated with {@link TearDown} in {@link StepContributor}.
     */
    void tearDown();

    /**
     * Runs a plan node of type {@link NodeType#STEP}, updating
     * its execution state with the results.
     *
     * @param modelStep The step to be executed.
     * @throws WakamitiException when the given node is not
     *                           suitable for being executed.
     */
    void runStep(PlanNode modelStep);

    /**
     * Exposes information about the type registry used by the backend.
     *
     * @return The data type registry.
     */
    WakamitiDataTypeRegistry getTypeRegistry();

    /**
     * Gets extra properties to use in the scenario context.
     *
     * @return The extra properties map.
     */
    default Map<String, Object> getExtraProperties() {
        return new LinkedHashMap<>();
    }

    /**
     * Obtain a list of all steps from any available step contributor
     * used by the backend. If a step uses data types with variations
     * (like assertions), a new element would be presented for each
     * variation.
     *
     * @param locale            The language used to describe the steps.
     * @param includeVariations Set whether every variation for data types
     *                          should be included.
     * @return A list with the available steps (empty if none present).
     */
    List<String> getAvailableSteps(Locale locale, boolean includeVariations);

    /**
     * Given an invalid step, obtain a list of valid suggestions. The
     * result would be sorted by string proximity with the original
     * step, being the first element the one with the higher
     * probability to solve the problem. If a step uses data types with
     * variations (like assertions), a new element would be presented
     * for each variation.
     *
     * @param invalidStep       The original (invalid) step.
     * @param locale            The language used.
     * @param numberOfHints     The number of suggestions.
     * @param includeVariations Set whether every variation for data
     *                          types should be included.
     * @return A list of suggestions (empty if none present).
     */
    List<String> getSuggestionsForInvalidStep(
            String invalidStep,
            Locale locale,
            int numberOfHints,
            boolean includeVariations
    );

    /**
     * Given an invalid step, obtain a message with the most probable
     * solutions. This is intended to be used as a part of an error
     * message or diagnostics.
     *
     * @param wrongStep The original (invalid) step.
     * @param locale    The language used.
     * @return A message with probable solutions.
     */
    String getHintFor(String wrongStep, Locale locale);

}