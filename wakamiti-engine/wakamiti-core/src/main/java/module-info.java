/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.extensions.*;
import es.iti.wakamiti.core.DefaultWakamitiAPI;
import es.iti.wakamiti.core.datatypes.WakamitiCoreTypes;
import es.iti.wakamiti.core.datatypes.assertion.WakamitiAssertTypes;
import es.iti.wakamiti.core.gherkin.GherkinPlanBuilder;
import es.iti.wakamiti.core.gherkin.GherkinRedefinitionPlanTransformer;
import es.iti.wakamiti.core.gherkin.GherkinResourceType;
import es.iti.wakamiti.core.properties.GlobalPropertyEvaluator;
import es.iti.wakamiti.core.properties.StepPropertyEvaluator;

open module es.iti.wakamiti.core {

    exports es.iti.wakamiti.core.gherkin;
    exports es.iti.wakamiti.core.util;
    exports es.iti.wakamiti.core.runner;
    exports es.iti.wakamiti.core;
    exports es.iti.wakamiti.core.junit;
    exports es.iti.wakamiti.core.gherkin.parser;

    requires transitive es.iti.wakamiti.api;
    requires transitive imconfig;
    requires transitive iti.commons.jext;
    requires transitive slf4jansi;
    requires transitive maven.fetcher;
    requires junit;
    requires org.hamcrest;
    requires tag.expressions;

    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires net.harawata.appdirs;
    requires java.instrument;
    requires org.apache.xmlbeans;
    requires org.apache.commons.lang3;

    provides WakamitiAPI with DefaultWakamitiAPI;

    provides ResourceType with GherkinResourceType;
    provides PlanBuilder with GherkinPlanBuilder;
    provides PlanTransformer with GherkinRedefinitionPlanTransformer;
    provides DataTypeContributor with WakamitiCoreTypes, WakamitiAssertTypes;
    provides PropertyEvaluator with GlobalPropertyEvaluator, StepPropertyEvaluator;


}