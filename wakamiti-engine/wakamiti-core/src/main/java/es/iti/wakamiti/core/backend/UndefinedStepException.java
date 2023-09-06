/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.core.backend;


import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.util.Either;


public class UndefinedStepException extends WakamitiException {

    private static final long serialVersionUID = 5923513040489649029L;


    public UndefinedStepException(PlanNode step, String message, String extraInfo) {
        this(Either.of(step),message,extraInfo);
    }


    public UndefinedStepException(Either<PlanNode,String> step, String message, String extraInfo) {
        super(
            step
            .value()
            .map(node ->"Cannot match step at <"+node.source()+"> '{}' : {}\n{}")
            .orElse("Cannot match step '{}' : {}\n{}"),
            step.mapValueOrFallback(PlanNode::name),
            message,extraInfo
        );
    }


    public UndefinedStepException(String message, Object... args) {
        super(message,args);
    }



}