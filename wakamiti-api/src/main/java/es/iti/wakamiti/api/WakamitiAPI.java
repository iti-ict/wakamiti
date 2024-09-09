/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api;


import es.iti.commons.jext.ExtensionManager;
import es.iti.wakamiti.api.plan.PlanSerializer;
import es.iti.wakamiti.api.util.ResourceLoader;
import es.iti.wakamiti.api.imconfig.Configuration;

import java.nio.file.Path;
import java.util.ServiceLoader;


/**
 * Provides access to various components and services within the
 * Wakamiti application.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public interface WakamitiAPI {

    static WakamitiAPI instance() {
        return ServiceLoader.load(WakamitiAPI.class).findFirst().orElseThrow();
    }

    WakamitiContributors contributors();

    ExtensionManager extensionManager();

    PlanSerializer planSerializer();

    ResourceLoader resourceLoader();

    void publishEvent(String eventType, Object data);

    String version();

    Path workingDir(Configuration configuration);

    Configuration defaultConfiguration();

}
