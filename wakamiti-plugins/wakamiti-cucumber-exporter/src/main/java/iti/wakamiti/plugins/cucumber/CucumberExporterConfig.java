package iti.wakamiti.plugins.cucumber;

import imconfig.Configuration;
import imconfig.Configurer;
import iti.commons.jext.Extension;
import iti.wakamiti.api.extensions.ConfigContributor;


@Extension(
    provider = "iti.wakamiti",
    name = "cucumber-exporter-config",
    version = "1.0",
    extensionPoint = "iti.wakamiti.api.extensions.ConfigContributor"
)
public class CucumberExporterConfig implements ConfigContributor<CucumberExporter> {



    private static Configuration DEFAULT = Configuration.factory().fromPairs(
        "cucumberExporter.outputFile", "cucumber-report.json",
        "cucumberExporter.multiLevelStrategy", CucumberExporter.Strategy.OUTERSTEPS.name().toLowerCase()
    );


    @Override
    public boolean accepts(Object contributor) {
        return contributor instanceof CucumberExporter;
    }


    @Override
    public Configuration defaultConfiguration() {
        return DEFAULT;
    }


    @Override
    public Configurer<CucumberExporter> configurer() {
        return this::configure;
    }


    private void configure(CucumberExporter cucumberExporter, Configuration configuration) {
        configuration.get("cucumberExporter.outputFile", String.class).ifPresent(cucumberExporter::setOutputFile);
        configuration.get("cucumberExporter.multiLevelStrategy", String.class)
                .map(it->it.toUpperCase())
                .map(it-> CucumberExporter.Strategy.valueOf(it))
                .ifPresent(cucumberExporter::setStrategy);
    }


}

