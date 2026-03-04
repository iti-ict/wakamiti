/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.Configurer;
import slf4jansi.AnsiLogger;

import java.util.stream.Collectors;


/**
 * A contributor class for configuring database-related parameters.
 *
 * @see ConfigContributor
 */
@Extension(provider = "es.iti.wakamiti", name = "database-step-config", version = "2.6",
        extensionPoint = "es.iti.wakamiti.api.extensions.ConfigContributor")
public class DatabaseConfigContributor implements ConfigContributor<DatabaseStepContributor> {

    public static final String DATABASE_NULL_SYMBOL = "database.nullSymbol";
    public static final String DATABASE_ENABLE_CLEANUP_UPON_COMPLETION = "database.enableCleanupUponCompletion";
    public static final String DATABASE_XLS_IGNORE_SHEET_PATTERN = "database.xls.ignoreSheetPattern";
    /* The CSV format name as specified at {@link CSVFormat} */
    public static final String DATABASE_CSV_FORMAT = "database.csv.format";
    public static final String DATABASE_HEALTHCHECK = "database.healthcheck";
    /**
     * Max duration (milliseconds) allowed for similar-record lookup.
     * Values &lt;= 0 disable timeout.
     */
    public static final String DATABASE_SIMILAR_SEARCH_TIMEOUT_MS = "database.similarSearch.timeout";
    /** Enables Lucene candidate preselection for similar-record lookup. */
    public static final String DATABASE_SIMILAR_SEARCH_LUCENE_ENABLED = "database.similarSearch.lucene.enabled";
    /** Number of Lucene candidates to evaluate with final Levenshtein scoring. */
    public static final String DATABASE_SIMILAR_SEARCH_LUCENE_TOPK = "database.similarSearch.lucene.topK";
    /** Optional filesystem base directory for Lucene indexes. */
    public static final String DATABASE_SIMILAR_SEARCH_LUCENE_INDEX_DIR = "database.similarSearch.lucene.indexDir";
    private static final String PROPERTY_BASE = "database";
    private static final String DATASOURCE_BASE = "datasource";
    private static final String CONNECTION_URL = "connection.url";
    private static final String CONNECTION_USERNAME = "connection.username";
    private static final String CONNECTION_PASSWORD = "connection.password";
    private static final String CONNECTION_DRIVER = "connection.driver";
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
            DATABASE_SIMILAR_SEARCH_TIMEOUT_MS, Long.toString(10000),
            DATABASE_SIMILAR_SEARCH_LUCENE_ENABLED, Boolean.FALSE.toString(),
            DATABASE_SIMILAR_SEARCH_LUCENE_TOPK, Integer.toString(10)
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
     * @param contributor  The database step contributor
     * @param configuration The configuration to apply
     */
    private void configure(DatabaseStepContributor contributor, Configuration configuration) {
        Configuration databaseConfig = configuration.inner(PROPERTY_BASE);

        configuration.get(DATABASE_XLS_IGNORE_SHEET_PATTERN, String.class).ifPresent(contributor::setXlsIgnoreSheetRegex);
        configuration.get(DATABASE_NULL_SYMBOL, String.class).ifPresent(contributor::setNullSymbol);
        configuration.get(DATABASE_CSV_FORMAT, String.class).ifPresent(contributor::setCsvFormat);
        configuration.get(DATABASE_ENABLE_CLEANUP_UPON_COMPLETION, Boolean.class)
                .ifPresent(contributor::setEnableCleanupUponCompletion);
        configuration.get(DATABASE_HEALTHCHECK, Boolean.class)
                .ifPresent(contributor::setHealthcheck);
        configuration.get(DATABASE_SIMILAR_SEARCH_TIMEOUT_MS, Long.class)
                .ifPresent(contributor::setSimilarSearchTimeoutMs);
        configuration.get(DATABASE_SIMILAR_SEARCH_LUCENE_ENABLED, Boolean.class)
                .ifPresent(contributor::setLuceneSimilarSearchEnabled);
        configuration.get(DATABASE_SIMILAR_SEARCH_LUCENE_TOPK, Integer.class)
                .ifPresent(contributor::setLuceneSimilarSearchTopK);
        configuration.get(DATABASE_SIMILAR_SEARCH_LUCENE_INDEX_DIR, String.class)
                .ifPresent(contributor::setLuceneSimilarSearchIndexDir);

        if (databaseConfig.keyStream().anyMatch(k -> k.startsWith(DATASOURCE_BASE))) {
            Configuration datasourceConfig = databaseConfig.inner(DATASOURCE_BASE);
            datasourceConfig.keyStream()
                    .map(key -> key.split("\\.")[0])
                    .collect(Collectors.toSet())
                    .forEach(alias -> contributor.addConnection(alias, parameters(datasourceConfig.inner(alias))));
        } else if (databaseConfig.keyStream().anyMatch(k -> k.startsWith("connection"))) {
            contributor.addConnection(parameters(databaseConfig));
        }

        AnsiLogger.addStyle("sql", "yellow,bold");
    }

    /**
     * Retrieves connection parameters from the provided configuration.
     *
     * @param configuration The configuration to extract connection parameters from
     * @return The connection parameters
     */
    private ConnectionParameters parameters(Configuration configuration) {
        ConnectionParameters connectionParameters = new ConnectionParameters();
        configuration.get(CONNECTION_URL, String.class).ifPresent(connectionParameters::url);
        configuration.get(CONNECTION_USERNAME, String.class).ifPresent(connectionParameters::username);
        configuration.get(CONNECTION_PASSWORD, String.class).ifPresent(connectionParameters::password);
        configuration.get(CONNECTION_DRIVER, String.class).ifPresent(connectionParameters::driver);
        configuration.get(METADATA_SCHEMA, String.class).ifPresent(connectionParameters::schema);
        configuration.get(METADATA_CATALOG, String.class).ifPresent(connectionParameters::catalog);
        configuration.get(AUTO_TRIM, Boolean.class).ifPresent(connectionParameters::autoTrim);
        configuration.get(AUTO_COMMIT, Boolean.class).ifPresent(connectionParameters::autoCommit);
        return connectionParameters;
    }

}
