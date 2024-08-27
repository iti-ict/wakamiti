package es.iti.wakamiti.core;


import es.iti.commons.jext.ExtensionManager;
import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.WakamitiContributors;
import es.iti.wakamiti.api.plan.PlanSerializer;
import es.iti.wakamiti.api.util.ResourceLoader;
import es.iti.wakamiti.api.imconfig.Configuration;

import java.nio.file.Path;


/**
 * The DefaultWakamitiAPI class implements the {@link WakamitiAPI} interface and provides default
 * implementations for various methods. It acts as a bridge between the Wakamiti core functionality
 * and external components.
 *
 * <p>The class provides access to contributors, the extension manager, plan serializer,
 * resource loader, event publishing, version information, working directory, and default configuration.
 */
public class DefaultWakamitiAPI implements WakamitiAPI {

    /**
     * {@inheritDoc}
     */
    @Override
    public WakamitiContributors contributors() {
        return Wakamiti.contributors();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtensionManager extensionManager() {
        return Wakamiti.extensionManager();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlanSerializer planSerializer() {
        return Wakamiti.planSerializer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceLoader resourceLoader() {
        return Wakamiti.resourceLoader();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishEvent(String eventType, Object data) {
        Wakamiti.instance().publishEvent(eventType, data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String version() {
        return getClass().getPackage().getImplementationVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path workingDir(Configuration configuration) {
        return Wakamiti.workingDir(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Configuration defaultConfiguration() {
        return Wakamiti.defaultConfiguration();
    }

}
