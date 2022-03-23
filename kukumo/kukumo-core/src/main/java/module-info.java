/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.extensions.ConfigContributor;
import iti.kukumo.api.extensions.DataTypeContributor;
import iti.kukumo.api.extensions.PlanBuilder;
import iti.kukumo.api.extensions.PlanTransformer;
import iti.kukumo.api.extensions.ResourceType;
import iti.kukumo.core.datatypes.KukumoCoreTypes;
import iti.kukumo.core.datatypes.assertion.KukumoAssertTypes;
import iti.kukumo.core.plan.CleanUpPlanTransformer;
import iti.kukumo.gherkin.GherkinPlanBuilder;
import iti.kukumo.gherkin.GherkinRedefinitionPlanTransformer;
import iti.kukumo.gherkin.GherkinResourceType;

open module kukumo.core {

    exports iti.kukumo.api;
    exports iti.kukumo.api.annotations;
    exports iti.kukumo.api.event;
    exports iti.kukumo.api.extensions;
    exports iti.kukumo.api.plan;
    exports iti.kukumo.api.datatypes;
    exports iti.kukumo.gherkin;
    exports iti.kukumo.junit;
    exports iti.kukumo.util;

    requires transitive imconfig;
    requires transitive iti.commons.jext;
    requires transitive slf4jansi;
    requires transitive maven.fetcher;

    requires transitive junit;
    requires org.hamcrest;
    requires tag.expressions;

    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.databind;
    requires transitive gherkin.parser;
    requires net.harawata.appdirs;
    requires org.codehaus.groovy;
    requires java.instrument;


    uses iti.kukumo.api.extensions.ResourceType;
    uses iti.kukumo.api.extensions.ConfigContributor;
    uses iti.kukumo.api.extensions.DataTypeContributor;
    uses iti.kukumo.api.extensions.EventObserver;
    uses iti.kukumo.api.extensions.PlanBuilder;
    uses iti.kukumo.api.extensions.PlanTransformer;
    uses iti.kukumo.api.extensions.Reporter;
    uses iti.kukumo.api.extensions.StepContributor;

    provides ResourceType with GherkinResourceType;
    provides PlanBuilder with GherkinPlanBuilder;
    provides PlanTransformer with GherkinRedefinitionPlanTransformer, CleanUpPlanTransformer;
    provides DataTypeContributor with KukumoCoreTypes, KukumoAssertTypes;
    provides ConfigContributor with KukumoConfiguration;

}