package iti.wakamiti.api;

import iti.commons.jext.ExtensionManager;
import iti.wakamiti.api.plan.PlanSerializer;
import iti.wakamiti.api.util.ResourceLoader;


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
