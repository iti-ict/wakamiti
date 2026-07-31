/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.jacoco;


import java.nio.file.Path;
import java.util.function.Consumer;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.Configurer;
import es.iti.wakamiti.api.util.PathUtil;


/**
 * Stores the configuration used by the Jacoco Config component.
 */
@Extension(
        provider = "es.iti.wakamiti",
        name = "jacoco-config",
        version = "2.13"
)
public class JacocoConfig implements ConfigContributor<JacocoReporter> {

    /** Configuration key for the host exposing the JaCoCo agent dump endpoint. */
    public static final String JACOCO_HOST = "jacoco.dump.host";
    /** Configuration key for the TCP port of the JaCoCo agent dump endpoint. */
    public static final String JACOCO_PORT = "jacoco.dump.port";
    /** Configuration key for the destination of the downloaded execution-data file. */
    public static final String JACOCO_OUTPUT = "jacoco.dump.output";
    /** Configuration key for retries when connecting to the JaCoCo agent. */
    public static final String JACOCO_RETRIES = "jacoco.dump.retries";
    /** Configuration key for the generated JaCoCo XML report path. */
    public static final String JACOCO_XML = "jacoco.report.xml";
    /** Configuration key for the generated JaCoCo CSV report path. */
    public static final String JACOCO_CSV = "jacoco.report.csv";
    /** Configuration key for the generated JaCoCo HTML report directory. */
    public static final String JACOCO_HTML = "jacoco.report.html";
    /** Configuration key containing class-file roots analyzed for coverage. */
    public static final String JACOCO_CLASSES = "jacoco.report.classes";
    /** Configuration key containing source roots linked from coverage reports. */
    public static final String JACOCO_SOURCES = "jacoco.report.sources";
    /** Configuration key for the source tab width used by the HTML report. */
    public static final String JACOCO_TABWITH = "jacoco.report.tabwith";
    /** Configuration key for the logical bundle name displayed in reports. */
    public static final String JACOCO_NAME = "jacoco.report.name";

    @Override
    public Configuration defaultConfiguration() {
        return Configuration.factory().fromPairs(
                JACOCO_HOST, "localhost",
                JACOCO_PORT, "6300",
                JACOCO_RETRIES, "10",
                JACOCO_OUTPUT, ".",
                JACOCO_TABWITH, "4",
                JACOCO_NAME, "JaCoCo Coverage Report"
        );
    }

    @Override
    public Configurer<JacocoReporter> configurer() {
        return this::configure;
    }

    private void configure(
            JacocoReporter reporter,
            Configuration configuration
    ) {
        configuration.get(JACOCO_HOST, String.class).ifPresent(reporter::setHost);
        configuration.get(JACOCO_PORT, String.class).ifPresent(reporter::setPort);
        configuration.get(JACOCO_RETRIES, Integer.class).ifPresent(reporter::setRetries);
        configuration.get(JACOCO_OUTPUT, Path.class)
                .map(PathUtil::replaceTemporalPlaceholders)
                .ifPresent(reporter::setOutput);
        configuration.get(JACOCO_XML, Path.class)
                .map(PathUtil::replaceTemporalPlaceholders)
                .ifPresent(reporter::setXml);
        configuration.get(JACOCO_CSV, Path.class)
                .map(PathUtil::replaceTemporalPlaceholders)
                .ifPresent(reporter::setCsv);
        configuration.get(JACOCO_HTML, Path.class)
                .map(PathUtil::replaceTemporalPlaceholders)
                .ifPresent(reporter::setHtml);
        requiredProperty(configuration, JACOCO_CLASSES, Path.class, reporter::setClasses);
        configuration.get(JACOCO_SOURCES, Path.class).ifPresent(reporter::setSources);
        configuration.get(JACOCO_TABWITH, Integer.class).ifPresent(reporter::setTabwidth);
        configuration.get(JACOCO_NAME, String.class).ifPresent(reporter::setName);
    }

    private <T> void requiredProperty(
            Configuration config,
            String property,
            Class<T> type,
            Consumer<T> setter
    ) {
        T value = config.get(property, type)
                .orElseThrow(() -> new WakamitiException("Property '{}' is required", property));
        setter.accept(value);
    }

}
