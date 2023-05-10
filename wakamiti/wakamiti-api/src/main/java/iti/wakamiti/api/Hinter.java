/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - luiinge@gmail.com
 */
package iti.wakamiti.api;
import java.util.List;


/**
 * This interface provides utility methods to offer completion suggestions
 * about a test plan.
 */
public interface Hinter {

    List<String> getExpandedAvailableSteps();

    List<String> getCompactAvailableSteps();

    List<String> getAvailableProperties();

    boolean isValidStep(String stepLiteral);

    List<String> getHintsForInvalidStep(
        String invalidStepLiteral,
        int numberOfHints,
        boolean includeVariations
    );

	String getStepProviderByDefinition(String stepDefinition);

}