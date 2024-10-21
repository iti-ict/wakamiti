/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.xray;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.Configurer;


@Extension(
        provider = "es.iti.wakamiti",
        name = "xray-config",
        version = "2.6",
        extensionPoint = "es.iti.wakamiti.api.extensions.ConfigContributor"
)
public class XrayConfigContributor implements ConfigContributor<XRayReporter> {

    private static final Configuration DEFAULTS = Configuration.factory().fromPairs(
            "xray.property1", "value1"
    );


    @Override
    public boolean accepts(Object contributor) {
        return contributor instanceof XRayReporter;
    }


    @Override
    public Configuration defaultConfiguration() {
        return DEFAULTS;
    }


    @Override
    public Configurer<XRayReporter> configurer() {
        return this::configure;
    }


    private void configure(XRayReporter contributor, Configuration configuration) {
        // TODO
    }


}