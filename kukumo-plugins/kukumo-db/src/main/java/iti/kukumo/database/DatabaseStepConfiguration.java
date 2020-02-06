/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.database;


public class DatabaseStepConfiguration {

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


    private DatabaseStepConfiguration() {
        /* avoid instantiation */ }


    public static class Defaults {

        public static final String DEFAULT_DATABASE_XLS_IGNORE_SHEET_PATTERN = "#.*";
        public static final String DEFAULT_DATABASE_NULL_SYMBOL = "<null>";
        public static final String DEFAULT_DATABASE_CSV_FORMAT = "DEFAULT";
        public static final boolean DEFAULT_DATABASE_ENABLE_CLEANUP_UPON_COMPLETION = false;

        private Defaults() {
            /* avoid instantiation */ }
    }

}
