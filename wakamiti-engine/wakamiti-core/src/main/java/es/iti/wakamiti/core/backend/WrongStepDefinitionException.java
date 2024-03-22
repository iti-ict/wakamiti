/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.backend;


import es.iti.wakamiti.api.WakamitiException;


/**
 * Exception thrown when a step definition is incorrect.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class WrongStepDefinitionException extends WakamitiException {

    private static final long serialVersionUID = 213010011198230927L;

    /**
     * Constructs a WrongStepDefinitionException with information about
     * the incorrect step definition.
     *
     * @param stepProviderClass The class providing the step definition.
     * @param stepDefinitionKey The key identifying the step definition.
     * @param message           A message providing details about the
     *                          incorrect step definition.
     * @param args              Optional arguments to be included in the
     *                          message.
     */
    public WrongStepDefinitionException(
            Class<?> stepProviderClass, String stepDefinitionKey, String message,
            Object... args
    ) {
        super(
                "Wrong step definition <" + stepProviderClass
                        .getSimpleName() + "::'" + stepDefinitionKey + "'>: " + message,
                args
        );
    }

}