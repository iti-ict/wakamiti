/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.core.backend;


import iti.kukumo.api.KukumoException;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.util.Either;


public class UndefinedStepException extends KukumoException {

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