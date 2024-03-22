package es.iti.wakamiti.plugins.cucumber;

import imconfig.Configuration;
import imconfig.Configurer;
import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.extensions.ConfigContributor;


@Extension(provider =  "es.iti.wakamiti", name = "cucumber-exporter-config", version = "2.4",
    extensionPoint =  "es.iti.wakamiti.api.extensions.ConfigContributor")
public class CucumberExporterConfig implements ConfigContributor<CucumberExporter> {

    @Override
    public Configuration defaultConfiguration() {
        return Configuration.factory().fromPairs(
                "cucumberExporter.outputFile", "cucumber-report.json",
                "cucumberExporter.multiLevelStrategy", CucumberExporter.Strategy.OUTERSTEPS.name().toLowerCase()
        );
    }


    @Override
    public Configurer<CucumberExporter> configurer() {
        return this::configure;
    }


    private void configure(CucumberExporter cucumberExporter, Configuration configuration) {
        configuration.get("cucumberExporter.outputFile", String.class).ifPresent(cucumberExporter::setOutputFile);
        configuration.get("cucumberExporter.multiLevelStrategy", String.class)
                .map(String::toUpperCase)
                .map(CucumberExporter.Strategy::valueOf)
                .ifPresent(cucumberExporter::setStrategy);
    }


}

