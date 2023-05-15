package es.iti.wakamiti.core;

import es.iti.commons.jext.ExtensionManager;
import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.WakamitiContributors;
import es.iti.wakamiti.api.plan.PlanSerializer;
import es.iti.wakamiti.api.util.ResourceLoader;

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
