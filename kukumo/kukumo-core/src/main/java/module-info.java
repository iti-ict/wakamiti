/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import iti.kukumo.api.KukumoAPI;
import iti.kukumo.api.extensions.*;
import iti.kukumo.api.extensions.DataTypeContributor;
import iti.kukumo.core.DefaultKukumoAPI;
import iti.kukumo.core.datatypes.KukumoCoreTypes;
import iti.kukumo.core.datatypes.assertion.KukumoAssertTypes;
import iti.kukumo.core.gherkin.GherkinPlanBuilder;
import iti.kukumo.core.gherkin.GherkinRedefinitionPlanTransformer;
import iti.kukumo.core.gherkin.GherkinResourceType;
import iti.kukumo.core.properties.GlobalPropertyEvaluator;
import iti.kukumo.core.properties.StepPropertyEvaluator;

open module kukumo.core {

    exports iti.kukumo.core.gherkin;
    exports iti.kukumo.core.util;
    exports iti.kukumo.core.runner;
    exports iti.kukumo.core;

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
    requires kukumo.api;
    requires xmlbeans;

    provides KukumoAPI with DefaultKukumoAPI;

    provides ResourceType with GherkinResourceType;
    provides PlanBuilder with GherkinPlanBuilder;
    provides PlanTransformer with GherkinRedefinitionPlanTransformer;
    provides DataTypeContributor with KukumoCoreTypes, KukumoAssertTypes;
    provides PropertyEvaluator with GlobalPropertyEvaluator, StepPropertyEvaluator;


}