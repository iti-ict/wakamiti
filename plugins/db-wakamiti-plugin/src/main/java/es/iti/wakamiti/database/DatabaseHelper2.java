package es.iti.wakamiti.database;

//import es.iti.wakamiti.api.WakamitiException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class DatabaseHelper2 {

//    private final AtomicReference<String> currentDatabase = new AtomicReference<>("db1");
//    private final Map<String, Map<String, Map<String, Map<String, String>>>> cachedMetadata = new HashMap<>();
//
//    private ConnectionProvider connectionProvider;
//    private ConnectionParameters connectionParameters;
//    private CaseSensitivity caseSensitivity = CaseSensitivity.INSENSITIVE;
//
//    public interface ConnectionProvider {
//
//        Connection obtainConnection() throws SQLException;
//    }
//
//    protected Connection connection() throws SQLException {
//        return connectionProvider.obtainConnection();
//    }
//
//    private void getMetadata(String table) {
//        try {
//            if (!cachedMetadata.containsKey(currentDatabase.get())
//                    && !cachedMetadata.get(currentDatabase.get()).containsKey(table)) {
//                DatabaseMetaData metadata = connection().getMetaData();
//                ResultSet resultSet = metadata.getColumns(catalog(), schema(), caseSensitivity.format(table), "%");
//                while (resultSet != null && resultSet.next()) {
//                    cachedMetadata
//                            .get(currentDatabase.get())
//                            .get(table)
//                            .putIfAbsent(resultSet.getString("COLUMN_NAME"),
//                                    Map.of(
//                                            "DATA_TYPE", resultSet.getString("DATA_TYPE"),
//                                            "COLUMN_SIZE", resultSet.getString("COLUMN_SIZE")
//                                    )
//                            );
//                }
//
//            }
//
//
//            // return primaryKeys.isEmpty() ? Optional.empty() : Optional.of(primaryKeys.toArray(new String[0]));
//        } catch (Exception e) {
//            throw new WakamitiException(e);
//        }
//    }
//
//    private String catalog() {
//        try {
//            String catalog = connectionParameters.catalog();
//            if (catalog != null) {
//                return catalog;
//            }
//            catalog = connection().getCatalog();
//            if (catalog != null) {
//                connectionParameters.catalog(catalog);
//                return catalog;
//            }
//            return null;
//        } catch (SQLException e) {
//            return null;
//        }
//    }
//
//
//
//    private String schema() {
//        try {
//            String schema = connectionParameters.schema();
//            if (schema != null) {
//                return schema;
//            }
//            schema = connection().getSchema();
//            return schema;
//        } catch (SQLException e) {
//            return null;
//        }
//    }

}
