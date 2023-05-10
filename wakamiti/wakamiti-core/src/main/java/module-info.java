/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import iti.wakamiti.api.WakamitiAPI;
import iti.wakamiti.api.extensions.*;
import iti.wakamiti.api.extensions.DataTypeContributor;
import iti.wakamiti.core.DefaultWakamitiAPI;
import iti.wakamiti.core.datatypes.WakamitiCoreTypes;
import iti.wakamiti.core.datatypes.assertion.WakamitiAssertTypes;
import iti.wakamiti.core.gherkin.GherkinPlanBuilder;
import iti.wakamiti.core.gherkin.GherkinRedefinitionPlanTransformer;
import iti.wakamiti.core.gherkin.GherkinResourceType;
import iti.wakamiti.core.properties.GlobalPropertyEvaluator;
import iti.wakamiti.core.properties.StepPropertyEvaluator;

open module wakamiti.core {

    exports iti.wakamiti.core.gherkin;
    exports iti.wakamiti.core.util;
    exports iti.wakamiti.core.runner;
    exports iti.wakamiti.core;

    requires transitive imconfig;
    requires transitive iti.commons.jext;
    requires transitive slf4jansi;
    requires transitive maven.fetcher;

    requires transitive junit;
    requires org.hamcrest;
    requires tag.expressions;

    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires transitive gherkin.parser;
    requires net.harawata.appdirs;
    requires java.instrument;
    requires wakamiti.api;
    requires xmlbeans;

    provides WakamitiAPI with DefaultWakamitiAPI;

    provides ResourceType with GherkinResourceType;
    provides PlanBuilder with GherkinPlanBuilder;
    provides PlanTransformer with GherkinRedefinitionPlanTransformer;
    provides DataTypeContributor with WakamitiCoreTypes, WakamitiAssertTypes;
    provides PropertyEvaluator with GlobalPropertyEvaluator, StepPropertyEvaluator;


}