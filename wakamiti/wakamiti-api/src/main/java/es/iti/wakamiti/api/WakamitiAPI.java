package es.iti.wakamiti.api;

import es.iti.commons.jext.ExtensionManager;
import es.iti.wakamiti.api.plan.PlanSerializer;
import es.iti.wakamiti.api.util.ResourceLoader;
import imconfig.Configuration;


import java.io.File;
import java.nio.file.Path;
import java.util.ServiceLoader;

public interface WakamitiAPI {

    static WakamitiAPI instance() {
        return ServiceLoader.load(WakamitiAPI.class).findFirst().orElseThrow();
    }

    WakamitiContributors contributors();
    ExtensionManager extensionManager();
    PlanSerializer planSerializer();


    /**
     * @deprecated Use either {@link #resourceLoader(File)} or {@link #resourceLoader(Configuration)}
     * instead
     */
    @Deprecated
    ResourceLoader resourceLoader();

    ResourceLoader resourceLoader(File workingDir);

    ResourceLoader resourceLoader(Configuration configuration);


    void publishEvent(String eventType, Object data);
    String version();
    Path workingDir(Configuration configuration);

    Configuration defaultConfiguration();
}
