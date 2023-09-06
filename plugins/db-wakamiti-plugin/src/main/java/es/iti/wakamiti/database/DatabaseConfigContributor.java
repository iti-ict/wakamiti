/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.database;


import imconfig.Configuration;
import imconfig.Configurer;
import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.extensions.ConfigContributor;


@Extension(
    provider =  "es.iti.wakamiti",
    name = "database-step-config",
    version = "1.1",
    extensionPoint =  "es.iti.wakamiti.api.extensions.ConfigContributor"
)
public class DatabaseConfigContributor implements ConfigContributor<DatabaseStepContributor> {

    public static final String DATABASE_CONNECTION_URL = "database.connection.url";
    public static final String DATABASE_CONNECTION_USERNAME = "database.connection.username";
    public static final String DATABASE_CONNECTION_PASSWORD = "database.connection.password";
    public static final String DATABASE_CONNECTION_DRIVER = "database.connection.driver";

    public static final String DATABASE_METADATA_SCHEMA = "database.metadata.schema";
    public static final String DATABASE_METADATA_CATALOG = "database.metadata.catalog";
    public static final String DATABASE_METADATA_CASE_SENSITIVITY = "database.metadata.caseSensitivity";

    public static final String DATABASE_NULL_SYMBOL = "database.nullSymbol";
    public static final String DATABASE_ENABLE_CLEANUP_UPON_COMPLETION = "database.enableCleanupUponCompletion";

    public static final String DATABASE_XLS_IGNORE_SHEET_PATTERN = "database.xls.ignoreSheetPattern";

    /* The CSV format name as specified at {@link CSVFormat} */
    public static final String DATABASE_CSV_FORMAT = "database.csv.format";


    private static final Configuration DEFAULTS = Configuration.factory().fromPairs(
        DATABASE_XLS_IGNORE_SHEET_PATTERN,  "#.*",
        DATABASE_NULL_SYMBOL,  "<null>",
        DATABASE_CSV_FORMAT, "DEFAULT",
        DATABASE_ENABLE_CLEANUP_UPON_COMPLETION, "false"
    );

    @Override
    public boolean accepts(Object contributor) {
        return contributor instanceof DatabaseStepContributor;
    }

    @Override
    public Configuration defaultConfiguration() {
        return DEFAULTS;
    }

    @Override
    public Configurer<DatabaseStepContributor> configurer() {
        return this::configure;
    }

    private void configure(DatabaseStepContributor contributor, Configuration configuration) {

        ConnectionParameters connectionParameters = contributor.getConnectionParameters();
        configuration.get(DATABASE_CONNECTION_URL, String.class).ifPresent(connectionParameters::url);
        configuration.get(DATABASE_CONNECTION_USERNAME, String.class).ifPresent(connectionParameters::username);
        configuration.get(DATABASE_CONNECTION_PASSWORD, String.class).ifPresent(connectionParameters::password);
        configuration.get(DATABASE_CONNECTION_DRIVER, String.class).ifPresent(connectionParameters::driver);
        configuration.get(DATABASE_METADATA_SCHEMA, String.class).ifPresent(connectionParameters::schema);
        configuration.get(DATABASE_METADATA_CATALOG, String.class).ifPresent(connectionParameters::catalog);
        configuration.get(DATABASE_XLS_IGNORE_SHEET_PATTERN, String.class).ifPresent(contributor::setXlsIgnoreSheetRegex);
        configuration.get(DATABASE_NULL_SYMBOL, String.class).ifPresent(contributor::setNullSymbol);
        configuration.get(DATABASE_CSV_FORMAT, String.class).ifPresent(contributor::setCsvFormat);
        configuration.get(DATABASE_ENABLE_CLEANUP_UPON_COMPLETION, Boolean.class).ifPresent(contributor::setEnableCleanupUponCompletion);

        configuration.get(DATABASE_METADATA_CASE_SENSITIVITY, String.class)
            .map(String::toUpperCase)
            .map(CaseSensitivity::valueOf)
            .ifPresent(contributor::setCaseSensitivity);

    }

}