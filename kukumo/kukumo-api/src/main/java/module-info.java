import iti.kukumo.api.KukumoAPI;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.extensions.*;

module kukumo.api {

    exports iti.kukumo.api;
    exports iti.kukumo.api.extensions;
    exports iti.kukumo.api.annotations;
    exports iti.kukumo.api.plan;
    exports iti.kukumo.api.util;
    exports iti.kukumo.api.event;
    exports iti.kukumo.api.datatypes;
    exports iti.kukumo.api.model;

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
    uses KukumoAPI;
    uses ResourceType;
    uses ConfigContributor;
    uses DataTypeContributor;
    uses EventObserver;
    uses PlanBuilder;
    uses PlanTransformer;
    uses Reporter;
    uses StepContributor;
    uses LoaderContributor;

    provides ConfigContributor with KukumoConfiguration;

}