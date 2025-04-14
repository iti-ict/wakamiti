/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.modbus;



import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.Configurer;
import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.util.ThrowableFunction;

import java.net.URL;


@Extension(
        provider =  "es.iti.wakamiti",
        name = "modbus-config",
        version = "2.7",
        extensionPoint =  "es.iti.wakamiti.api.extensions.ConfigContributor"
)
public class ModbusConfigContributor implements ConfigContributor<ModbusStepContributor> {

    public static final String BASE_URL = "modbus.baseURL";
    public static final String SLAVE_ID = "modbus.slaveId";

    private static final Configuration DEFAULTS = Configuration.factory().fromPairs(
            BASE_URL, "http://localhost:5020",
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
        configuration.get(BASE_URL, String.class)
                .map(ThrowableFunction.unchecked(URL::new))
                .ifPresent(contributor::setBaseURL);
        configuration.get(SLAVE_ID, String.class).ifPresent(contributor::setSlaveId);

    }



}