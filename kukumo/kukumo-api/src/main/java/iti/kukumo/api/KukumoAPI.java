package iti.kukumo.api;

import iti.commons.jext.ExtensionManager;
import iti.kukumo.api.plan.PlanSerializer;
import iti.kukumo.api.util.ResourceLoader;


import java.util.ServiceLoader;

public interface KukumoAPI {

    static KukumoAPI instance() {
        return ServiceLoader.load(KukumoAPI.class).findFirst().orElseThrow();
    }


    ExtensionManager extensionManager();
    PlanSerializer planSerializer();
    ResourceLoader resourceLoader();
}
