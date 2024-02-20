/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api;


import java.util.List;


/**
 * This interface provides utility methods to offer completion suggestions
 * about a test plan.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public interface Hinter {

    /**
     * Get a list of expanded available steps.
     *
     * @return List of expanded available steps.
     */
    List<String> getExpandedAvailableSteps();

    /**
     * Get a list of compact available steps.
     *
     * @return List of compact available steps.
     */
    List<String> getCompactAvailableSteps();

    /**
     * Get a list of available properties.
     *
     * @return List of available properties.
     */
    List<String> getAvailableProperties();

    /**
     * Check if a step is valid.
     *
     * @param stepLiteral The literal representation of the step.
     * @return {@code true} if the step is valid, {@code false} otherwise.
     */
    boolean isValidStep(String stepLiteral);

    /**
     * Get hints for an invalid step.
     *
     * @param invalidStepLiteral The literal representation of the
     *                           invalid step.
     * @param numberOfHints      The number of suggestions.
     * @param includeVariations  Set whether every variation for data types
     *                           should be included.
     * @return List of suggestions for the invalid step.
     */
    List<String> getHintsForInvalidStep(
            String invalidStepLiteral,
            int numberOfHints,
            boolean includeVariations
    );

    /**
     * Get the step provider by its definition.
     *
     * @param stepDefinition The definition of the step.
     * @return The step provider associated with the step definition.
     */
    String getStepProviderByDefinition(String stepDefinition);

}