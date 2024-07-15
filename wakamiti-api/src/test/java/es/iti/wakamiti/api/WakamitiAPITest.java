/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api;

import es.iti.commons.jext.ExtensionManager;
import es.iti.wakamiti.api.plan.PlanSerializer;
import es.iti.wakamiti.api.util.ResourceLoader;
import imconfig.Configuration;

import java.nio.file.Path;

public class WakamitiAPITest implements WakamitiAPI {

    @Override
    public WakamitiContributors contributors() {
        return null;
    }

    @Override
    public ExtensionManager extensionManager() {
        return null;
    }

    @Override
    public PlanSerializer planSerializer() {
        return null;
    }

    @Override
    public ResourceLoader resourceLoader() {
        return null;
    }

    @Override
    public void publishEvent(String eventType, Object data) {
        // this implementation method does nothing
    }

    @Override
    public String version() {
        return Integer.MAX_VALUE + "." + Integer.MAX_VALUE + "." + Integer.MAX_VALUE;
    }

    @Override
    public Path workingDir(Configuration configuration) {
        return null;
    }

    @Override
    public Configuration defaultConfiguration() {
        return null;
    }
}
