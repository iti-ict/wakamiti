import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.extensions.Reporter;
import es.iti.wakamiti.plugins.cucumber.CucumberExporter;
import es.iti.wakamiti.plugins.cucumber.CucumberExporterConfig;

module es.iti.wakamiti.report.cucumber {

    exports es.iti.wakamiti.plugins.cucumber;

    requires es.iti.wakamiti.api;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;

    provides ConfigContributor with CucumberExporterConfig;
    provides Reporter with CucumberExporter;

}