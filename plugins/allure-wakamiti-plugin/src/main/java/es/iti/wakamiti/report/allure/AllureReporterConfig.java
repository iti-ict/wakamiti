/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.report.allure;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.Configurer;
import es.iti.wakamiti.api.util.PathUtil;

import java.nio.file.Path;


@Extension(
        provider = "es.iti.wakamiti",
        name = "allure-report-config",
        version = "2.6",
        extensionPoint = "es.iti.wakamiti.api.extensions.ConfigContributor"
)
public class AllureReporterConfig implements ConfigContributor<AllureReporter> {

    public static final String PREFIX = "allureReport";
    public static final String OUTPUT = PREFIX + ".output";

    private static final Configuration DEFAULTS = Configuration.factory().fromPairs(
            OUTPUT, "allure-results"
    );

    @Override
    public Configuration defaultConfiguration() {
        return DEFAULTS;
    }

    @Override
    public Configurer<AllureReporter> configurer() {
        return this::configure;
    }

    private void configure(
            AllureReporter reporter,
            Configuration configuration
    ) {
        configuration.get(OUTPUT, Path.class)
                .map(PathUtil::replaceTemporalPlaceholders)
                .ifPresent(reporter::setOutputDir);
    }
}
