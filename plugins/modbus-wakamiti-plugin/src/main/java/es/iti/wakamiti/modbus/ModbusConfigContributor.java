/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.modbus;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.Configurer;


/**
 * Supplies default Modbus connection settings and applies them to
 * {@link ModbusStepContributor}.
 */
@Extension(
        provider = "es.iti.wakamiti",
        name = "modbus-config",
        version = "2.7",
        extensionPoint = "es.iti.wakamiti.api.extensions.ConfigContributor"
)
public class ModbusConfigContributor implements ConfigContributor<ModbusStepContributor> {

    /** Configuration key for the Modbus TCP server host name or address. */
    public static final String HOST = "modbus.host";
    /** Configuration key for the Modbus TCP server port. */
    public static final String PORT = "modbus.port";
    /** Configuration key for the Modbus unit or slave identifier. */
    public static final String SLAVE_ID = "modbus.slaveId";

    private static final Configuration DEFAULTS = Configuration.factory().fromPairs(
            HOST, "localhost",
            PORT, "5020",
            SLAVE_ID, "1"
    );

    @Override
    public boolean accepts(
            Object contributor
    ) {
        return contributor instanceof ModbusStepContributor;
    }

    @Override
    public Configuration defaultConfiguration() {
        return DEFAULTS;
    }

    /**
     * Returns the configurator callback used to transfer configuration values
     * into the step contributor.
     *
     * @return contributor configurator
     */
    @Override
    public Configurer<ModbusStepContributor> configurer() {
        return this::configure;
    }

    /**
     * Applies resolved host/port/slave settings to the contributor.
     *
     * @param contributor modbus step contributor
     * @param configuration effective runtime configuration
     */
    private void configure(
            ModbusStepContributor contributor,
            Configuration configuration
    ) {
        configuration.get(HOST, String.class).ifPresent(contributor::setHost);
        configuration.get(PORT, Integer.class).ifPresent(contributor::setPort);
        configuration.get(SLAVE_ID, String.class).ifPresent(contributor::setSlaveId);
    }

}
