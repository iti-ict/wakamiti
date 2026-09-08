/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database;


import java.io.File;
import java.util.List;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.ConfigurationException;
import es.iti.wakamiti.api.imconfig.Configurer;
import slf4jansi.AnsiLogger;


/**
 * A contributor class for configuring database-related parameters.
 *
 * @see ConfigContributor
 */
@Extension(
        provider = "es.iti.wakamiti",
        name = "database-step-config",
        version = "2.13",
        extensionPoint = "es.iti.wakamiti.api.extensions.ConfigContributor"
)
public class DatabaseConfigContributor implements ConfigContributor<DatabaseStepContributor> {

    /** Configuration key for the text marker interpreted as an SQL {@code NULL}. */
    public static final String DATABASE_NULL_SYMBOL = "database.nullSymbol";
    /** Configuration key enabling rollback or cleanup actions after execution. */
    public static final String DATABASE_ENABLE_CLEANUP_UPON_COMPLETION = "database.enableCleanupUponCompletion";
    /** Configuration key for the pattern that excludes spreadsheet sheets from datasets. */
    public static final String DATABASE_XLS_IGNORE_SHEET_PATTERN = "database.xls.ignoreSheetPattern";
    /** The CSV format name as specified by {@code CSVFormat}. */
    public static final String DATABASE_CSV_FORMAT = "database.csv.format";
    /** Configuration key enabling connection validation before database steps run. */
    public static final String DATABASE_HEALTHCHECK = "database.healthcheck";
    /**
     * Max duration (milliseconds) allowed for similar-record lookup.
     * Values &lt;= 0 disable timeout.
     */
    public static final String DATABASE_SIMILAR_SEARCH_TIMEOUT_MS = "database.similarSearch.timeout";

    private static final String PROPERTY_BASE = "database";
    private static final String DATASOURCE_BASE = "datasource";
    private static final String SCRIPTS_SETUP = "scripts.setup";
    private static final String SCRIPTS_TEARDOWN = "scripts.teardown";
    private static final String CONNECTION_BASE = "connection";
    private static final String CONNECTION_URL = "url";
    private static final String CONNECTION_USERNAME = "username";
    private static final String CONNECTION_PASSWORD = "password";
    private static final String CONNECTION_DRIVER = "driver";
    private static final String METADATA_SCHEMA = "metadata.schema";
    private static final String METADATA_CATALOG = "metadata.catalog";
    private static final String AUTO_TRIM = "autotrim";
    private static final String AUTO_COMMIT = "autocommit";

    private static final Configuration DEFAULTS = Configuration.factory().fromPairs(
            DATABASE_XLS_IGNORE_SHEET_PATTERN, "#.*",
            DATABASE_NULL_SYMBOL, "<null>",
            DATABASE_CSV_FORMAT, "DEFAULT",
            DATABASE_ENABLE_CLEANUP_UPON_COMPLETION, Boolean.FALSE.toString(),
            DATABASE_HEALTHCHECK, Boolean.TRUE.toString(),
            DATABASE_SIMILAR_SEARCH_TIMEOUT_MS, Long.toString(10000)
    );

    /**
     * Retrieves the default configuration.
     *
     * @return The default configuration
     */
    @Override
    public Configuration defaultConfiguration() {
        return DEFAULTS;
    }

    /**
     * Retrieves the configurer for the database step contributor.
     *
     * @return The configurer
     */
    @Override
    public Configurer<DatabaseStepContributor> configurer() {
        return this::configure;
    }

    /**
     * Configures the database step contributor with the provided configuration.
     *
     * @param contributor   The database step contributor
     * @param configuration The configuration to apply
     */
    private void configure(
            DatabaseStepContributor contributor,
            Configuration configuration
    ) {
        Configuration databaseConfig = configuration.inner(PROPERTY_BASE);

        configuration.get(DATABASE_XLS_IGNORE_SHEET_PATTERN, String.class)
                .ifPresent(contributor::setXlsIgnoreSheetRegex);
        configuration.get(DATABASE_NULL_SYMBOL, String.class).ifPresent(contributor::setNullSymbol);
        configuration.get(DATABASE_CSV_FORMAT, String.class).ifPresent(contributor::setCsvFormat);
        configuration.get(DATABASE_ENABLE_CLEANUP_UPON_COMPLETION, Boolean.class)
                .ifPresent(contributor::setEnableCleanupUponCompletion);
        configuration.get(DATABASE_HEALTHCHECK, Boolean.class)
                .ifPresent(contributor::setHealthcheck);
        configuration.get(DATABASE_SIMILAR_SEARCH_TIMEOUT_MS, Long.class)
                .ifPresent(contributor::setSimilarSearchTimeoutMs);

        int connections = 0;
        if (hasConnection(databaseConfig)) {
            configureDatasource(contributor, databaseConfig, DatabaseSupport.DEFAULT);
            connections++;
        }
        Configuration datasourceConfig = databaseConfig.inner(DATASOURCE_BASE);
        List<String> aliases = datasourceConfig.keyStream()
                .map(key -> key.split("\\.")[0])
                .distinct()
                .toList();
        for (String alias : aliases) {
            configureDatasource(contributor, datasourceConfig.inner(alias), alias);
            connections++;
        }

        if (connections == 0) {
            throw new ConfigurationException("At least one connection configuration is required");
        }

        AnsiLogger.addStyle("sql", "yellow,bold");
    }

    private void configureDatasource(
            DatabaseStepContributor contributor,
            Configuration configuration,
            String alias
    ) {
        if (!hasConnection(configuration)) {
            throw new ConfigurationException(
                    "A connection configuration is mandatory for datasource '" + alias + "'"
            );
        }
        contributor.addConnection(alias, connectionParameters(configuration));
        configuration.getList(SCRIPTS_SETUP, File.class)
                .forEach(script -> contributor.addSetupScript(alias, script));
        configuration.getList(SCRIPTS_TEARDOWN, File.class)
                .forEach(script -> contributor.addTeardownScript(alias, script));
    }

    private boolean hasConnection(
            Configuration configuration
    ) {
        return configuration.keyStream().anyMatch(key -> key.startsWith(CONNECTION_BASE + "."));
    }

    /**
     * Retrieves connection parameters from the provided configuration.
     *
     * @param configuration The configuration to extract connection parameters from
     * @return The connection parameters
     */
    private ConnectionParameters connectionParameters(
            Configuration configuration
    ) {
        ConnectionParameters connectionParameters = new ConnectionParameters();
        configuration.get("%s.%s".formatted(CONNECTION_BASE, CONNECTION_URL), String.class)
                .ifPresent(connectionParameters::url);
        configuration.get("%s.%s".formatted(CONNECTION_BASE, CONNECTION_USERNAME), String.class)
                .ifPresent(connectionParameters::username);
        configuration.get("%s.%s".formatted(CONNECTION_BASE, CONNECTION_PASSWORD), String.class)
                .ifPresent(connectionParameters::password);
        configuration.get("%s.%s".formatted(CONNECTION_BASE, CONNECTION_DRIVER), String.class)
                .ifPresent(connectionParameters::driver);
        configuration.get(METADATA_SCHEMA, String.class).ifPresent(connectionParameters::schema);
        configuration.get(METADATA_CATALOG, String.class).ifPresent(connectionParameters::catalog);
        configuration.get(AUTO_TRIM, Boolean.class).ifPresent(connectionParameters::autoTrim);
        configuration.get(AUTO_COMMIT, Boolean.class).ifPresent(connectionParameters::autoCommit);
        return connectionParameters;
    }

}
