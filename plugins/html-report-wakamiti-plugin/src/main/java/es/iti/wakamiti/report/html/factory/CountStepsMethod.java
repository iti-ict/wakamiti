/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.report.html.factory;


import java.util.List;

import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.plan.Result;
import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateMethodModelEx;


/**
 * Provides the Count Steps Method functionality used by Wakamiti.
 */
public class CountStepsMethod implements TemplateMethodModelEx {

    @Override
    public Object exec(
            List args
    ) {
        if (args.isEmpty() || !(args.get(0) instanceof StringModel model)
                || !(model.getWrappedObject() instanceof PlanNodeSnapshot)) {
            throw new WakamitiException("Argument must be a PlanNodeSnapshot");
        }
        return args.size() == 1 ? countSteps((PlanNodeSnapshot) model.getWrappedObject())
                : countSteps((PlanNodeSnapshot) model.getWrappedObject(), args.get(1).toString());
    }

    private long countSteps(
            PlanNodeSnapshot node
    ) {
        long sum = node.getChildren() == null ? 0 : node.getChildren().stream().mapToLong(this::countSteps).sum();
        if (node.getNodeType().isAnyOf(NodeType.VIRTUAL_STEP, NodeType.STEP)) {
            sum++;
        }
        return sum;
    }

    private long countSteps(
            PlanNodeSnapshot node,
            String result
    ) {
        long sum = node.getChildren() == null ? 0 : node.getChildren().stream().mapToLong(c -> countSteps(c, result)).sum();
        if (node.getNodeType().isAnyOf(NodeType.VIRTUAL_STEP, NodeType.STEP)
                && node.getResult() == Result.valueOf(result)) {
            sum++;
        }
        return sum;
    }

}
