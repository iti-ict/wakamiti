/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.jacoco;


import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

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
        version = "3.1"
)
public class JacocoConfig implements ConfigContributor<JacocoReporter> {

    private static final int MAX_IPV4_OCTET = 255;
    private static final int MAX_PORT = 65535;
    private static final int MAX_HOST_LENGTH = 253;
    private static final Pattern HOST_LABEL_PATTERN = Pattern.compile(
            "[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?"
    );
    private static final Pattern DOT_PATTERN = Pattern.compile("\\.");
    private static final Pattern IPV4_PATTERN = Pattern.compile("^(?:\\d{1,3}\\.){3}\\d{1,3}$");
    private static final Pattern PORT_PATTERN = Pattern.compile("\\d{1,5}");

    /** Configuration key for the JaCoCo agent dump endpoints. */
    public static final String JACOCO_HOSTS = "jacoco.dump.hosts";
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
    /** Configuration key controlling whether execution data is merged. */
    public static final String JACOCO_MERGE = "jacoco.report.merge";

    @Override
    public Configuration defaultConfiguration() {
        return Configuration.factory().fromPairs(
                JACOCO_HOSTS, "localhost:6300",
                JACOCO_RETRIES, "10",
                JACOCO_OUTPUT, ".",
                JACOCO_TABWITH, "4",
                JACOCO_NAME, "JaCoCo Coverage Report",
                JACOCO_MERGE, Boolean.TRUE.toString()
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
        reporter.setHosts(validatedHosts(configuration));
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
        List<Path> classes = requiredListProperty(configuration, JACOCO_CLASSES, Path.class);
        classes.forEach(path -> validateDirectory(JACOCO_CLASSES, path));
        reporter.setClasses(classes);
        List<Path> sources = configuration.getList(JACOCO_SOURCES, Path.class);
        sources.forEach(path -> validateDirectory(JACOCO_SOURCES, path));
        reporter.setSources(sources);
        configuration.get(JACOCO_TABWITH, Integer.class).ifPresent(reporter::setTabwidth);
        configuration.get(JACOCO_NAME, String.class).ifPresent(reporter::setName);
        configuration.get(JACOCO_MERGE, Boolean.class).ifPresent(reporter::setMerge);
    }

    private Path validateDirectory(
            String property,
            Path path
    ) {
        if (!Files.isDirectory(path)) {
            throw new WakamitiException("Property '{}' must be an existing directory: '{}'", property, path);
        }
        return path;
    }

    private List<String> validatedHosts(
            Configuration configuration
    ) {
        List<String> hosts = configuration.getList(JACOCO_HOSTS, String.class);
        if (hosts.isEmpty()) {
            throw new WakamitiException("Property '{}' requires at least one host", JACOCO_HOSTS);
        }
        hosts.forEach(this::validateHost);
        return hosts;
    }

    private void validateHost(
            String endpoint
    ) {
        int separator = endpoint == null ? -1 : endpoint.indexOf(':');
        if (separator <= 0 || separator != endpoint.lastIndexOf(':')) {
            throw invalidHost(endpoint);
        }

        String host = endpoint.substring(0, separator);
        String port = endpoint.substring(separator + 1);
        if (!validHost(host) || !PORT_PATTERN.matcher(port).matches()) {
            throw invalidHost(endpoint);
        }

        int portNumber = Integer.parseInt(port);
        if (portNumber < 1 || portNumber > MAX_PORT) {
            throw invalidHost(endpoint);
        }
    }

    private boolean validHost(
            String host
    ) {
        if (host.length() > MAX_HOST_LENGTH) {
            return false;
        }

        String[] labels = DOT_PATTERN.split(host, -1);
        for (String label : labels) {
            if (!HOST_LABEL_PATTERN.matcher(label).matches()) {
                return false;
            }
        }

        return !IPV4_PATTERN.matcher(host).matches()
                || DOT_PATTERN.splitAsStream(host)
                        .mapToInt(Integer::parseInt)
                        .allMatch(octet -> octet <= MAX_IPV4_OCTET);
    }

    private WakamitiException invalidHost(
            String endpoint
    ) {
        return new WakamitiException(
                "Invalid value '{}' for property '{}': expected host:port with port between 1 and 65535",
                endpoint,
                JACOCO_HOSTS
        );
    }

    private <T> List<T> requiredListProperty(
            Configuration config,
            String property,
            Class<T> type
    ) {
        List<T> values = config.getList(property, type);
        if (values.isEmpty()) {
            throw new WakamitiException("Property '{}' is required", property);
        }
        return values;
    }

}
