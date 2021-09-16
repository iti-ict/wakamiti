import iti.kukumo.api.extensions.ConfigContributor;
import iti.kukumo.api.extensions.StepContributor;
import iti.kukumo.rest.RestConfigContributor;
import iti.kukumo.rest.RestStepContributor;

module kukumo.rest {

    exports iti.kukumo.rest;

    requires transitive kukumo.core;
    requires transitive rest.assured;
    requires transitive xml.path;
    requires org.json;
    requires json.path;
    requires com.fasterxml.jackson.databind;
    requires org.xmlunit;
    requires iti.commons.jext;
    requires junit;
    requires com.fasterxml.jackson.dataformat.xml;
    requires org.apache.commons.lang3;

    uses ConfigContributor;
    uses StepContributor;

    provides ConfigContributor with RestConfigContributor;
    provides StepContributor with RestStepContributor;

}