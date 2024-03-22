/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ${package};



import imconfig.Configuration;
import imconfig.Configurer;
import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.extensions.ConfigContributor;


@Extension(
        provider =  "es.iti.wakamiti",
        name = "${pluginId}-config",
        version = "1.1",
        extensionPoint =  "es.iti.wakamiti.api.extensions.ConfigContributor"
)
public class ${PluginId}ConfigContributor implements ConfigContributor<${PluginId}StepContributor> {

    private static final Configuration DEFAULTS = Configuration.factory().fromPairs(
        "${pluginId}.property1", "value1"
    );


    @Override
    public boolean accepts(Object contributor) {
        return contributor instanceof ${PluginId}StepContributor;
    }



    @Override
    public Configuration defaultConfiguration() {
        return DEFAULTS;
    }


    @Override
    public Configurer<${PluginId}StepContributor> configurer() {
        return this::configure;
    }


    private void configure(${PluginId}StepContributor contributor, Configuration configuration) {
        // TODO
    }



}