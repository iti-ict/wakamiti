import iti.wakamiti.api.WakamitiAPI;
import iti.wakamiti.api.WakamitiConfiguration;
import iti.wakamiti.api.extensions.*;

module wakamiti.api {

    exports iti.wakamiti.api;
    exports iti.wakamiti.api.extensions;
    exports iti.wakamiti.api.annotations;
    exports iti.wakamiti.api.plan;
    exports iti.wakamiti.api.util;
    exports iti.wakamiti.api.event;
    exports iti.wakamiti.api.datatypes;
    exports iti.wakamiti.api.model;

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