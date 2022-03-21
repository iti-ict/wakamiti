/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.report.html;


import java.util.Locale;
import java.util.Optional;

import iti.commons.configurer.Configuration;
import iti.commons.configurer.Configurer;
import iti.commons.jext.Extension;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.extensions.ConfigContributor;
import iti.kukumo.util.LocaleLoader;


@Extension(
    provider = "iti.kukumo",
    name = "html-report-config",
    version = "1.1",
    extensionPoint = "iti.kukumo.api.extensions.ConfigContributor"
)
public class HtmlReportGeneratorConfig implements ConfigContributor<HtmlReportGenerator> {

    public static final String PREFIX = "htmlReport";
    public static final String OUTPUT_FILE = PREFIX+".output";
    public static final String CSS_FILE = PREFIX+".css";
    public static final String TITLE = PREFIX+".title";


    private static final Configuration DEFAULTS = Configuration.fromPairs(
        CSS_FILE, "",
        OUTPUT_FILE, "kukumo.html"
    );

    @Override
    public Configuration defaultConfiguration() {
        return DEFAULTS;
    }

    @Override
    public boolean accepts(Object contributor) {
        return HtmlReportGenerator.class.equals(contributor.getClass());
    }


    @Override
    public Configurer<HtmlReportGenerator> configurer() {
        return this::configure;
    }

    private void configure(HtmlReportGenerator contributor, Configuration configuration) {
        contributor.setConfiguration(configuration);
    }

}