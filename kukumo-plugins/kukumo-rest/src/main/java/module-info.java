import iti.kukumo.api.extensions.Configurator;
import iti.kukumo.api.extensions.StepContributor;
import iti.kukumo.rest.RestStepConfigurator;
import iti.kukumo.rest.RestStepContributor;

module kukumo.rest {

    exports iti.kukumo.rest;

    requires transitive kukumo.core;
    requires org.hamcrest;
    requires transitive rest.assured;
    requires transitive xml.path;
    requires org.json;
    requires json.path;
    requires com.fasterxml.jackson.databind;
    requires org.xmlunit;
    requires iti.commons.jext;
    requires junit;
    requires com.fasterxml.jackson.dataformat.xml;


    uses Configurator;
    uses StepContributor;

    provides Configurator with RestStepConfigurator;
    provides StepContributor with RestStepContributor;

}