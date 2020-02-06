/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api.plan;


import java.util.function.UnaryOperator;



public interface PlanNodeData {

    PlanNodeData copy();

    PlanNodeData copyReplacingVariables(UnaryOperator<String> replacer);

}
