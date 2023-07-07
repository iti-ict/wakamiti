package es.iti.wakamiti.core;

import es.iti.commons.jext.ExtensionManager;
import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.WakamitiContributors;
import es.iti.wakamiti.api.plan.PlanSerializer;
import es.iti.wakamiti.api.util.ResourceLoader;
import imconfig.Configuration;

import java.io.File;
import java.nio.file.Path;

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
    public void publishEvent(String eventType, Object data) {
        Wakamiti.instance().publishEvent(eventType,data);
    }

    @Override
    public String version() {
        return getClass().getPackage().getImplementationVersion();
    }

    @Override
    public Path workingDir(Configuration configuration) {
        return Wakamiti.workingDir(configuration);
    }

    @Override
    public Configuration defaultConfiguration() {
        return Wakamiti.defaultConfiguration();
    }
}
