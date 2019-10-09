package iti.kukumo.api;

import iti.commons.configurer.Configuration;
import iti.kukumo.api.plan.PlanNode;

public interface BackendFactory {

    Backend createBackend(PlanNode node, Configuration configuration);

}
