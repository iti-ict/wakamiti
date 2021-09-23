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

    public static final String OUTPUT_FILE = "htmlReport.output";
    public static final String CSS_FILE = "htmlReport.css";
    public static final String REPORT_LOCALE = "htmlReport.locale";

    private static final Configuration DEFAULTS = Configuration.fromPairs(
        CSS_FILE, "",
        OUTPUT_FILE, "kukumo.html",
        REPORT_LOCALE, ""
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
        configuration.ifPresent(CSS_FILE,String.class,contributor::setCssFile);
        configuration.ifPresent(OUTPUT_FILE,String.class,contributor::setOutputFile);
        configuration.get(REPORT_LOCALE,String.class)
           .or(()->configuration.get(KukumoConfiguration.LANGUAGE,String.class))
           .map(LocaleLoader::forLanguage)
           .or(()->Optional.of(Locale.ENGLISH))
           .ifPresent(contributor::setReportLocale);
    }

}