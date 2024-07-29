import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.extensions.*;

module es.iti.wakamiti.api {

    exports es.iti.wakamiti.api;
    exports es.iti.wakamiti.api.extensions;
    exports es.iti.wakamiti.api.imconfig;
    exports es.iti.wakamiti.api.annotations;
    exports es.iti.wakamiti.api.plan;
    exports es.iti.wakamiti.api.util;
    exports es.iti.wakamiti.api.util.http.oauth;
    exports es.iti.wakamiti.api.event;
    exports es.iti.wakamiti.api.datatypes;
    exports es.iti.wakamiti.api.model;
    exports es.iti.wakamiti.api.matcher;

    requires transitive iti.commons.jext;
    requires transitive org.slf4j;

    requires org.hamcrest;
    requires slf4jansi;
    requires java.instrument;
    requires java.xml;
    requires org.apache.commons.configuration2;
    requires org.apache.commons.lang3;
    requires org.apache.xmlbeans;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires json.path;
    requires org.apache.groovy;
    requires org.yaml.snakeyaml;
    requires java.net.http;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires plexus.utils;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.dataformat.xml;
    requires org.apache.groovy.xml;
    requires Saxon.HE;

    uses PropertyEvaluator;
    uses WakamitiAPI;
    uses ResourceType;
    uses ConfigContributor;
    uses DataTypeContributor;
    uses EventObserver;
    uses PlanBuilder;
    uses PlanTransformer;
    uses Reporter;
    uses StepContributor;
    uses LoaderContributor;
    uses es.iti.wakamiti.api.imconfig.ConfigurationFactory;

    provides ConfigContributor with WakamitiConfiguration;
    provides es.iti.wakamiti.api.imconfig.ConfigurationFactory with es.iti.wakamiti.api.imconfig.internal.ApacheConfiguration2Factory;

    opens es.iti.wakamiti.api.plan to com.fasterxml.jackson.databind;

}