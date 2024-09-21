/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.report.html;


import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.Configurer;
import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.extensions.ConfigContributor;


@Extension(provider =  "es.iti.wakamiti", name = "html-report-config", version = "2.6",
    extensionPoint =  "es.iti.wakamiti.api.extensions.ConfigContributor")
public class HtmlReportGeneratorConfig implements ConfigContributor<HtmlReportGenerator> {

    public static final String PREFIX = "htmlReport";
    public static final String EXTRA_INFO = PREFIX+".extra_info";
    public static final String OUTPUT_FILE = PREFIX+".output";
    public static final String CSS_FILE = PREFIX+".css";
    public static final String TITLE = PREFIX+".title";


    private static final Configuration DEFAULTS = Configuration.factory().fromPairs(
        CSS_FILE, "",
        OUTPUT_FILE, "wakamiti.html"
    );

    @Override
    public Configuration defaultConfiguration() {
        return DEFAULTS;
    }

    @Override
    public Configurer<HtmlReportGenerator> configurer() {
        return this::configure;
    }

    private void configure(HtmlReportGenerator contributor, Configuration configuration) {
        contributor.setConfiguration(configuration);
    }

}