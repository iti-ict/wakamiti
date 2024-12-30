/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.jmeter.mockserver;


import es.iti.wakamiti.api.util.WakamitiLogger;
import org.mockserver.client.MockServerClient;
import org.mockserver.client.initialize.PluginExpectationInitializer;
import org.slf4j.Logger;

import java.io.IOException;

import static es.iti.wakamiti.jmeter.TestUtil.prepare;


public class InitializationClass implements PluginExpectationInitializer {

    private static final Logger LOGGER = WakamitiLogger.forClass(InitializationClass.class);

    @Override
    public void initializeExpectations(MockServerClient mockServerClient) {
        try {
            prepare(mockServerClient, "wakamiti/server", mime -> true);
            LOGGER.debug("MockServer started in port: {}", mockServerClient.getPort());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
