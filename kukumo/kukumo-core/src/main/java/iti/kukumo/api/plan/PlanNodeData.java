package iti.kukumo.api.plan;

import java.util.function.UnaryOperator;

/**
 * @author ITI
 * Created by ITI on 3/10/19
 */
public interface PlanNodeData {

    PlanNodeData copy();

    PlanNodeData copyReplacingVariables(UnaryOperator<String> replacer);

}
