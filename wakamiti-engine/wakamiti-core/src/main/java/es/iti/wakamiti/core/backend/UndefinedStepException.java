/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.backend;


import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.util.Either;


/**
 * Exception thrown when a step cannot be matched with any defined step.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class UndefinedStepException extends WakamitiException {

    private static final long serialVersionUID = 5923513040489649029L;

    /**
     * Constructs an UndefinedStepException for a specific PlanNode step
     * with a detailed message and extra information.
     *
     * @param step      The PlanNode step that couldn't be matched.
     * @param message   A detailed message indicating the reason for the
     *                  exception.
     * @param extraInfo Additional information about the exception.
     */
    public UndefinedStepException(PlanNode step, String message, String extraInfo) {
        this(Either.of(step), message, extraInfo);
    }

    /**
     * Constructs an UndefinedStepException for a step represented by a
     * string with a detailed message and extra information.
     *
     * @param step      The step as a string that couldn't be matched.
     * @param message   A detailed message indicating the reason for the
     *                  exception.
     * @param extraInfo Additional information about the exception.
     */
    public UndefinedStepException(Either<PlanNode, String> step, String message, String extraInfo) {
        super(
                step
                        .value()
                        .map(node -> "Cannot match step at <" + node.source() + "> '{}' : {}\n{}")
                        .orElse("Cannot match step '{}' : {}\n{}"),
                step.mapValueOrFallback(PlanNode::name),
                message, extraInfo
        );
    }

    /**
     * Constructs an UndefinedStepException with a general message and
     * optional arguments.
     *
     * @param message A general message indicating the reason for the
     *                exception.
     * @param args    Optional arguments to be included in the message.
     */
    public UndefinedStepException(String message, Object... args) {
        super(message, args);
    }

}