package iti.kukumo.report.html;

import java.util.Locale;
import java.util.Optional;

import iti.commons.configurer.Configuration;
import iti.commons.jext.Extension;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.extensions.Configurator;
import iti.kukumo.util.LocaleLoader;

@Extension(
    provider="iti.kukumo",
    name="html-report-config", 
    version="1.0.0", 
    extensionPoint="iti.kukumo.api.extensions.Configurator"
)
public class HtmlReportGeneratorConfig implements Configurator<HtmlReportGenerator> {

    public static final String OUTPUT_FILE = "htmlReport.output";
    public static final String CSS_FILE = "htmlReport.css";
    public static final String REPORT_LOCALE = "htmlReport.locale";
    
    
    @Override
    public boolean accepts(Object contributor) {
        return HtmlReportGenerator.class.equals(contributor.getClass());
    }

    
    @Override
    public void configure(HtmlReportGenerator contributor, Configuration configuration) {
        contributor.setCssFile(configuration.getString(CSS_FILE).orElse(null));
        contributor.setOutputFile(configuration.getString(OUTPUT_FILE).orElse("kukumo.html"));
        Optional<String> localeProperty = Optional.ofNullable( 
                configuration.getString(REPORT_LOCALE)
                .orElse(configuration.getString(KukumoConfiguration.LANGUAGE).orElse(null))
        ); 
        contributor.setReportLocale(localeProperty.map(LocaleLoader::forLanguage).orElse(Locale.ENGLISH));        
    }


}
