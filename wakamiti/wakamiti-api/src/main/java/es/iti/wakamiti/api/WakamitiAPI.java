package es.iti.wakamiti.api;

import es.iti.commons.jext.ExtensionManager;
import es.iti.wakamiti.api.plan.PlanSerializer;
import es.iti.wakamiti.api.util.ResourceLoader;


import java.util.ServiceLoader;

public interface WakamitiAPI {

    static WakamitiAPI instance() {
        return ServiceLoader.load(WakamitiAPI.class).findFirst().orElseThrow();
    }

    WakamitiContributors contributors();
    ExtensionManager extensionManager();
    PlanSerializer planSerializer();
    ResourceLoader resourceLoader();
    String version();
}
