/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.report.html;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.Configurer;


/**
 * Stores the configuration used by the Html Report Generator Config component.
 */
@Extension(
        provider = "es.iti.wakamiti",
        name = "html-report-config",
        version = "2.13",
        extensionPoint = "es.iti.wakamiti.api.extensions.ConfigContributor"
)
public class HtmlReportGeneratorConfig implements ConfigContributor<HtmlReportGenerator> {

    /** Root configuration namespace for the HTML report generator. */
    public static final String PREFIX = "htmlReport";
    /** Configuration key for arbitrary metadata rendered in the report header. */
    public static final String EXTRA_INFO = PREFIX + ".extra_info";
    /** Configuration key for the generated HTML report path. */
    public static final String OUTPUT_FILE = PREFIX + ".output";
    /** Configuration key for an optional custom stylesheet included in the report. */
    public static final String CSS_FILE = PREFIX + ".css";
    /** Configuration key for the title displayed in the generated report. */
    public static final String TITLE = PREFIX + ".title";

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

    private void configure(
            HtmlReportGenerator contributor,
            Configuration configuration
    ) {
        contributor.setConfiguration(configuration);
    }

}
