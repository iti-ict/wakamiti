/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package iti.wakamiti.report.html.factory;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateMethodModelEx;
import iti.wakamiti.api.WakamitiException;
import iti.wakamiti.api.plan.NodeType;
import iti.wakamiti.api.plan.PlanNodeSnapshot;
import iti.wakamiti.api.plan.Result;

import java.util.List;

public class CountStepsMethod implements TemplateMethodModelEx {

    @Override
    public Object exec(List args) {
        if (args.size() < 1 || !(args.get(0) instanceof StringModel)
            || !(((StringModel) args.get(0)).getWrappedObject() instanceof PlanNodeSnapshot)) {
            throw new WakamitiException("Argument must be a PlanNodeSnapshot");
        }
        return args.size() == 1 ? countSteps((PlanNodeSnapshot) ((StringModel) args.get(0)).getWrappedObject())
                : countSteps((PlanNodeSnapshot) ((StringModel) args.get(0)).getWrappedObject(), args.get(1).toString());
    }

    private long countSteps(PlanNodeSnapshot node) {
        long sum = node.getChildren() == null ? 0 : node.getChildren().stream().mapToLong(this::countSteps).sum();
        if (node.getNodeType().isAnyOf(NodeType.VIRTUAL_STEP, NodeType.STEP)) {
            sum++;
        }
        return sum;
    }

    private long countSteps(PlanNodeSnapshot node, String result) {
        long sum = node.getChildren() == null ? 0 : node.getChildren().stream().mapToLong(c -> countSteps(c, result)).sum();
        if (node.getNodeType().isAnyOf(NodeType.VIRTUAL_STEP, NodeType.STEP)
            && node.getResult() == Result.valueOf(result)) {
            sum++;
        }
        return sum;
    }
}
