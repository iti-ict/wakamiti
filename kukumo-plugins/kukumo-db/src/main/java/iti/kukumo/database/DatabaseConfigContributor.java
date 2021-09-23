/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.database;


import iti.commons.configurer.Configuration;
import iti.commons.configurer.Configurer;
import iti.commons.jext.Extension;
import iti.kukumo.api.extensions.ConfigContributor;


@Extension(
    provider = "iti.kukumo",
    name = "database-step-config",
    version = "1.1",
    extensionPoint = "iti.kukumo.api.extensions.ConfigContributor"
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


    private static final Configuration DEFAULTS = Configuration.fromPairs(
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
        configuration
            .ifPresent(DATABASE_CONNECTION_URL, String.class, connectionParameters::url)
            .ifPresent(DATABASE_CONNECTION_USERNAME, String.class, connectionParameters::username)
            .ifPresent(DATABASE_CONNECTION_PASSWORD, String.class, connectionParameters::password)
            .ifPresent(DATABASE_CONNECTION_DRIVER, String.class, connectionParameters::driver)
            .ifPresent(DATABASE_METADATA_SCHEMA, String.class, connectionParameters::schema)
            .ifPresent(DATABASE_METADATA_CATALOG, String.class, connectionParameters::catalog)
            .ifPresent(DATABASE_XLS_IGNORE_SHEET_PATTERN, String.class, contributor::setXlsIgnoreSheetRegex)
            .ifPresent(DATABASE_NULL_SYMBOL, String.class, contributor::setNullSymbol)
            .ifPresent(DATABASE_CSV_FORMAT, String.class, contributor::setCsvFormat)
            .ifPresent(DATABASE_ENABLE_CLEANUP_UPON_COMPLETION, Boolean.class, contributor::setEnableCleanupUponCompletion)
        ;
        configuration.get(DATABASE_METADATA_CASE_SENSITIVITY, String.class)
            .map(String::toUpperCase)
            .map(CaseSensitivity::valueOf)
            .ifPresent(contributor::setCaseSensitivity);

    }

}