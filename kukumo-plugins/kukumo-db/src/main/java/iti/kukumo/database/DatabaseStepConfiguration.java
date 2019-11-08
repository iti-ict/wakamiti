/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.database;


public class DatabaseStepConfiguration {

    public static final String DATABASE_CONNECTION_URL = "database.connection.url";
    public static final String DATABASE_CONNECTION_USERNAME = "database.connection.username";
    public static final String DATABASE_CONNECTION_PASSWORD = "database.connection.password";
    public static final String DATABASE_CONNECTION_DRIVER = "database.connection.driver";
    public static final String DATABASE_CONNECTION_SCHEMA = "database.connection.schema";
    public static final String DATABASE_CONNECTION_CATALOG = "database.connection.catalog";

    public static final String DATABASE_CASE_SENSITIVITY = "database.caseSensitivity";

    public static final String DATABASE_ENABLE_CLEANUP_UPON_COMPLETION = "database.enableCleanupUponCompletion";

    public static final String DATABASE_XLS_IGNORE_SHEET_PATTERN = "database.xls.ignoreSheetPattern";
    public static final String DATABASE_XLS_NULL_SYMBOL = "database.xls.nullSymbol";

    /* The CSV format name as specified at {@link CSVFormat} */
    public static final String DATABASE_CSV_FORMAT = "database.csv.format";


    private DatabaseStepConfiguration() {
        /* avoid instantiation */ }


    public static class Defaults {

        public static final String DEFAULT_DATABASE_XLS_IGNORE_SHEET_PATTERN = "#.*";
        public static final String DEFAULT_DATABASE_XLS_NULL_SYMBOL = "<null>";
        public static final String DEFAULT_DATABASE_CSV_FORMAT = "DEFAULT";
        public static final boolean DEFAULT_DATABASE_ENABLE_CLEANUP_UPON_COMPLETION = false;

        private Defaults() {
            /* avoid instantiation */ }
    }

}
