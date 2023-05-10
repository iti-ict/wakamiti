package iti.wakamiti.core;

import iti.commons.jext.ExtensionManager;
import iti.wakamiti.api.WakamitiAPI;
import iti.wakamiti.api.WakamitiContributors;
import iti.wakamiti.api.plan.PlanSerializer;
import iti.wakamiti.api.util.ResourceLoader;

public class DefaultWakamitiAPI implements WakamitiAPI {

    @Override
    public WakamitiContributors contributors() {
        return Wakamiti.contributors();
    }

    @Override
    public ExtensionManager extensionManager() {
        return Wakamiti.extensionManager();
    }

    @Override
    public PlanSerializer planSerializer() {
        return Wakamiti.planSerializer();
    }

    @Override
    public ResourceLoader resourceLoader() {
        return Wakamiti.resourceLoader();
    }

    @Override
    public String version() {
        return getClass().getPackage().getImplementationVersion();
    }

}
