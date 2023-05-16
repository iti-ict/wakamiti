import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.extensions.*;

module es.iti.wakamiti.api {

    exports es.iti.wakamiti.api;
    exports es.iti.wakamiti.api.extensions;
    exports es.iti.wakamiti.api.annotations;
    exports es.iti.wakamiti.api.plan;
    exports es.iti.wakamiti.api.util;
    exports es.iti.wakamiti.api.event;
    exports es.iti.wakamiti.api.datatypes;
    exports es.iti.wakamiti.api.model;

    requires transitive imconfig;
    requires transitive iti.commons.jext;
    requires transitive org.slf4j;

    requires org.hamcrest;
    requires slf4jansi;
    requires java.instrument;
    requires java.xml;
    requires xmlbeans;
    requires com.fasterxml.jackson.databind;
    requires json.path;
    requires commons.beanutils;
    requires org.apache.commons.lang3;

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

    provides ConfigContributor with WakamitiConfiguration;

}