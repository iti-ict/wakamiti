/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.jacoco;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.Configurer;
import es.iti.wakamiti.api.util.PathUtil;

import java.nio.file.Path;
import java.util.function.Consumer;


@Extension(provider = "es.iti.wakamiti", name = "jacoco-config", version = "2.6")
public class JacocoConfig implements ConfigContributor<JacocoReporter> {

    public static final String JACOCO_HOST = "jacoco.dump.host";
    public static final String JACOCO_PORT = "jacoco.dump.port";
    public static final String JACOCO_OUTPUT = "jacoco.dump.output";
    public static final String JACOCO_RETRIES = "jacoco.dump.retries";
    public static final String JACOCO_XML = "jacoco.report.xml";
    public static final String JACOCO_CSV = "jacoco.report.csv";
    public static final String JACOCO_HTML = "jacoco.report.html";
    public static final String JACOCO_CLASSES = "jacoco.report.classes";
    public static final String JACOCO_SOURCES = "jacoco.report.sources";
    public static final String JACOCO_TABWITH = "jacoco.report.tabwith";
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
