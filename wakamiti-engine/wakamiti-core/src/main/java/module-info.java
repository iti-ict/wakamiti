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
import es.iti.wakamiti.core.datatypes.duration.WakamitiDurationType;
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
    exports es.iti.wakamiti.core.maven;
    exports es.iti.wakamiti.core.gherkin.parser;
    exports es.iti.wakamiti.core.generator.features;

    requires transitive es.iti.wakamiti.api;
    requires transitive iti.commons.jext;
    requires transitive slf4jansi;
    requires maven.resolver.provider;
    requires org.apache.maven.resolver;
    requires org.apache.maven.resolver.util;
    requires org.apache.maven.resolver.impl;
    requires org.apache.maven.resolver.spi;
    requires org.apache.maven.resolver.connector.basic;
    requires org.apache.maven.resolver.transport.http;
    requires org.apache.maven.resolver.transport.file;
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
    requires java.net.http;
    requires json.path;
    requires org.apache.commons.io;

    requires swagger.parser.core;
    requires swagger.parser;
    requires io.swagger.v3.oas.models;
    requires org.apache.logging.log4j;

    provides WakamitiAPI with DefaultWakamitiAPI;

    provides ResourceType with GherkinResourceType;
    provides PlanBuilder with GherkinPlanBuilder;
    provides PlanTransformer with GherkinRedefinitionPlanTransformer;
    provides DataTypeContributor with WakamitiCoreTypes, WakamitiAssertTypes, WakamitiDurationType;
    provides PropertyEvaluator with GlobalPropertyEvaluator, StepPropertyEvaluator;

}