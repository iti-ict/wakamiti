/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.wakamiti.core.backend;


import iti.wakamiti.api.WakamitiException;


public class WrongStepDefinitionException extends WakamitiException {

    private static final long serialVersionUID = 213010011198230927L;


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