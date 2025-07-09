/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.modbus;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.Configurer;


@Extension(
        provider = "es.iti.wakamiti",
        name = "modbus-config",
        version = "2.7",
        extensionPoint = "es.iti.wakamiti.api.extensions.ConfigContributor"
)
public class ModbusConfigContributor implements ConfigContributor<ModbusStepContributor> {

    public static final String HOST = "modbus.host";
    public static final String PORT = "modbus.port";
    public static final String SLAVE_ID = "modbus.slaveId";

    private static final Configuration DEFAULTS = Configuration.factory().fromPairs(
            HOST, "localhost",
            PORT, "5020",
            SLAVE_ID, "1"
    );

    @Override
    public boolean accepts(Object contributor) {
        return contributor instanceof ModbusStepContributor;
    }

    @Override
    public Configuration defaultConfiguration() {
        return DEFAULTS;
    }

    @Override
    public Configurer<ModbusStepContributor> configurer() {
        return this::configure;
    }

    private void configure(ModbusStepContributor contributor, Configuration configuration) {
        configuration.get(HOST, String.class).ifPresent(contributor::setHost);
        configuration.get(PORT, Integer.class).ifPresent(contributor::setPort);
        configuration.get(SLAVE_ID, String.class).ifPresent(contributor::setSlaveId);

    }

}