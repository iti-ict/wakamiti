package iti.kukumo.api;

import java.time.Clock;

import iti.commons.configurer.Configuration;
import iti.commons.jext.ExtensionPoint;
import iti.kukumo.api.plan.PlanNode;

@ExtensionPoint
public interface BackendFactory {

    BackendFactory setClock(Clock clock);
    BackendFactory setConfiguration(Configuration configuration);
    Backend createBackend(PlanNode node);


}
