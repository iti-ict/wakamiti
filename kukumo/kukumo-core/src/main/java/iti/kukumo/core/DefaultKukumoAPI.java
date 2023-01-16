package iti.kukumo.core;

import iti.commons.jext.ExtensionManager;
import iti.kukumo.api.KukumoAPI;
import iti.kukumo.api.KukumoContributors;
import iti.kukumo.api.plan.PlanSerializer;
import iti.kukumo.api.util.ResourceLoader;

public class DefaultKukumoAPI implements KukumoAPI {

    @Override
    public KukumoContributors contributors() {
        return Kukumo.contributors();
    }

    @Override
    public ExtensionManager extensionManager() {
        return Kukumo.extensionManager();
    }

    @Override
    public PlanSerializer planSerializer() {
        return Kukumo.planSerializer();
    }

    @Override
    public ResourceLoader resourceLoader() {
        return Kukumo.resourceLoader();
    }

}
