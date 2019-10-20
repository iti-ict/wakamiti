/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.core.backend;


import iti.kukumo.api.KukumoException;


public class WrongStepDefinitionException extends KukumoException {

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
