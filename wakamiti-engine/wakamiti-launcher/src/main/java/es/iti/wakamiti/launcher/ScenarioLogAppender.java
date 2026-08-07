/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.launcher;


import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;


final class ScenarioLogAppender extends AbstractAppender {

    static final String APPENDER_NAME = "ScenarioLogAppender";
    static final String CONTEXT_KEY = "wakamiti.scenarioId";
    static final String LOG_PATH_PROPERTY = "wakamiti.log.path";
    static final String EXECUTION_TIMESTAMP_PROPERTY = "wakamiti.log.executionTimestamp";

    private static final String LOG_FILE_PATTERN = "%d  [%C{1.}.%M] %6p -  "
            + "%replace{%m}{\\x1B\\[([0-9]{1,2}(;[0-9]{1,2})?)?[mGK]}{}%n";

    private final Map<Path, OutputStream> streams;

    private ScenarioLogAppender() {
        super(
                APPENDER_NAME,
                (Filter) null,
                PatternLayout.newBuilder().withPattern(LOG_FILE_PATTERN).build(),
                true,
                null
        );
        this.streams = new ConcurrentHashMap<>();
    }

    static void install(
            LoggerContext context,
            String loggerName
    ) {
        Configuration configuration = context.getConfiguration();
        Appender previousAppender = configuration.getAppender(APPENDER_NAME);
        if (previousAppender != null) {
            previousAppender.stop();
            configuration.getRootLogger().removeAppender(APPENDER_NAME);
            LoggerConfig namedLogger = configuration.getLoggerConfig(loggerName);
            namedLogger.removeAppender(APPENDER_NAME);
            configuration.getAppenders().remove(APPENDER_NAME);
        }
        ScenarioLogAppender appender = new ScenarioLogAppender();
        appender.start();
        configuration.addAppender(appender);
        configuration.getRootLogger().addAppender(appender, null, null);
        context.updateLoggers();
    }

    @Override
    public void append(
            LogEvent event
    ) {
        Object scenarioIdValue = event.getContextData().getValue(CONTEXT_KEY);
        String scenarioId = scenarioIdValue == null ? null : scenarioIdValue.toString();
        if (scenarioId == null || scenarioId.isBlank()) {
            return;
        }
        try {
            OutputStream outputStream = streams.computeIfAbsent(resolveFile(scenarioId), this::openStream);
            outputStream.write(getLayout().toByteArray(event));
            outputStream.flush();
        } catch (IOException e) {
            throw new UncheckedIOException("Error writing scenario log file", e);
        }
    }

    @Override
    public boolean stop(
            long timeout,
            TimeUnit timeUnit
    ) {
        streams.values().forEach(this::closeQuietly);
        streams.clear();
        return super.stop(timeout, timeUnit);
    }

    private Path resolveFile(
            String scenarioId
    ) {
        String basePath = System.getProperty(LOG_PATH_PROPERTY, ".");
        String timestamp = System.getProperty(EXECUTION_TIMESTAMP_PROPERTY, "");
        return Path.of(basePath).resolve("wakamiti-" + timestamp + "-" + sanitize(scenarioId) + ".log");
    }

    private OutputStream openStream(
            Path file
    ) {
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            return Files.newOutputStream(file, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException("Error opening scenario log file", e);
        }
    }

    private void closeQuietly(
            OutputStream outputStream
    ) {
        try {
            outputStream.close();
        } catch (IOException ignored) {
            // Nothing to do while closing log streams.
        }
    }

    private static String sanitize(
            String scenarioId
    ) {
        return scenarioId.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
    }

}
