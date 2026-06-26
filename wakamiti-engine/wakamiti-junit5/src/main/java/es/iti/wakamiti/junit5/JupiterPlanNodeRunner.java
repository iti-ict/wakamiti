package es.iti.wakamiti.junit5;


import es.iti.wakamiti.api.BackendFactory;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.core.runner.PlanNodeLogger;
import es.iti.wakamiti.core.runner.PlanNodeRunner;


final class JupiterPlanNodeRunner extends PlanNodeRunner {

    JupiterPlanNodeRunner(
            PlanNode node,
            Configuration configuration,
            BackendFactory backendFactory,
            PlanNodeLogger logger,
            String nodePath
    ) {
        super(node, configuration, backendFactory, java.util.Optional.empty(), logger, false, nodePath);
    }

    Result execute() {
        return runNode();
    }
}
