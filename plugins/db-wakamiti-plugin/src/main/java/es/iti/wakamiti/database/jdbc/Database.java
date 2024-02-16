/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.jdbc;


import com.mdimension.jchronic.Chronic;
import com.mdimension.jchronic.Options;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.database.SQLParser;
import es.iti.wakamiti.database.exception.SQLRuntimeException;
import net.sf.jsqlparser.JSQLParserException;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static es.iti.wakamiti.database.DatabaseHelper.DATE_TIME_FORMATTER;
import static es.iti.wakamiti.database.jdbc.LogUtils.*;


public final class Database {

    private static final Logger LOGGER = WakamitiLogger.forName("es.iti.wakamiti.database");
    private static final Map<String, Schema> CACHED_SCHEMA = new HashMap<>();

    private final ConnectionProvider connection;
    private final DatabaseType type;
    private final SQLParser parser;
    private final Schema schema;


    private Database(ConnectionProvider connection) {
        this.connection = connection;
        this.type = DatabaseType.fromUrl(connection.parameters().url());
        this.parser = new SQLParser(type);
        this.schema = CACHED_SCHEMA.computeIfAbsent(connection.parameters().url(), k -> new Schema());
    }

    /**
     * Gets a new {@link Database} with retrieved connection provider.
     *
     * @param connection The connection provider
     * @return the new {@code Database}
     */
    public static Database from(ConnectionProvider connection) {
        return new Database(connection);
    }

    public DatabaseType type() {
        return type;
    }

    public SQLParser parser() {
        return parser;
    }

    /**
     * Gets the number of records in the given table.
     *
     * @param table The table name
     * @return the number of records
     */
    public long count(String table) {
        try (Statement statement = connection()
                .createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            String query = parser.sqlSelectCountFrom(table).toString();
            traceSQL(query);
            try (ResultSet rs = statement.executeQuery(query)) {
                long count = 0;
                while (rs.next()) count = rs.getLong(1);
                return count;
            }
        } catch (SQLException e) {
            throw new SQLRuntimeException(message("Error counting records from table {}", table), e);
        }
    }

    /**
     * Gets the table name in stored format.
     *
     * @param table The table name
     * @return The stored format
     */
    public String table(String table) {
        return schema.tables.computeIfAbsent(parser.unquote(table), k -> {
            LOGGER.debug("Retrieving the table {}", parser.unquote(table));
            try (ResultSet rs = connection().getMetaData()
                    .getTables(catalog(), schema(), "_".repeat(parser.unquote(table).length()), null)) {
                String name = null;
                while (rs.next()) {
                    String current = rs.getString("TABLE_NAME");
                    if (k.equalsIgnoreCase(current)) {
                        name = current;
                        break;
                    }
                }
                if (name == null) {
                    throw new SQLRuntimeException("The table {} does not exist", k);
                }
                return name;
            } catch (SQLException e) {
                throw new SQLRuntimeException(message("Error retrieving the table {}", k), e);
            }
        });
    }

    /**
     * Gets the column name in stored format.
     *
     * @param table  The table name
     * @param column The column name
     * @return The stored format
     */
    public String column(final String table, String column) {
        Function<String, String> retrieve = col -> {
            LOGGER.trace("Retrieving column {} of table {}", col, table(table));
            try (ResultSet rs = connection().getMetaData()
                    .getColumns(catalog(), schema(), table(table), "_".repeat(parser.unquote(column).length()))) {
                String name = null;
                while (rs.next()) {
                    String current = rs.getString("COLUMN_NAME");
                    if (col.equalsIgnoreCase(current)) {
                        name = current;
                        break;
                    }
                }
                if (name == null) {
                    throw new SQLRuntimeException("The column {}.{} does not exist", table(table), col);
                }
                return name;
            } catch (SQLException e) {
                throw new SQLRuntimeException(
                        message("Error retrieving column {}.{}", parser.unquote(column), table(table)), e);
            }
        };

        if (schema.columns.containsKey(table(table))) {
            Map<String, String> columns = schema.columns.get(table(table));
            return columns.computeIfAbsent(parser.unquote(column), retrieve);
        } else {
            LinkedHashMap<String, String> columns = new LinkedHashMap<>();
            columns.put(parser.unquote(column), retrieve.apply(parser.unquote(column)));
            schema.columns.put(table(table), columns);
        }
        return schema.columns.get(table(table)).get(parser.unquote(column));
    }

    /**
     * Gets the name of columns that make up the primary key.
     *
     * @param table The table name
     * @return the name of columns
     */
    public Stream<String> primaryKey(String table) {
        return schema.pk.computeIfAbsent(table(parser.unquote(table)), k -> {
            LOGGER.debug("Retrieving primary key of table {}", k);
            ArrayList<String> primaryKeys = new ArrayList<>();
            try (ResultSet rs = connection().getMetaData().getPrimaryKeys(catalog(), schema(), k)) {
                while (rs.next()) {
                    primaryKeys.add(rs.getString("COLUMN_NAME"));
                }
                return primaryKeys;
            } catch (SQLException e) {
                throw new SQLRuntimeException(message("Error retrieving primary key of table {}", k), e);
            }
        }).stream();

    }

    /**
     * Gets the column types of the given table.
     *
     * @param table The table name
     * @return the column types
     */
    public Map<String, JDBCType> columnTypes(String table) {
        return schema.types.computeIfAbsent(table(parser.unquote(table)), k -> {
            LOGGER.debug("Retrieving column types of table {}", k);
            LinkedHashMap<String, JDBCType> types = new LinkedHashMap<>();
            try (ResultSet rs = connection().getMetaData()
                    .getColumns(catalog(), schema(), k, null)) {
                while (rs.next()) {
                    types.put(
                            rs.getString("COLUMN_NAME"),
                            JDBCType.valueOf(rs.getInt("DATA_TYPE"))
                    );
                }
                return types;
            } catch (SQLException e) {
                throw new SQLRuntimeException(message("Error retrieving column types of table {}", k), e);
            }
        });
    }

    /**
     * Truncates the given table.
     *
     * @param table The table name
     */
    public void truncate(String table) {
        try (Statement statement = connection().createStatement()) {
            String truncate = "TRUNCATE TABLE " + parser().format(table);
            String delete = "DELETE FROM " + parser().format(table);
            int count = 0;
            try {
                count += count(table);
                statement.executeUpdate(truncate);
                traceSQL(truncate);
            } catch (SQLException e) {
                count = statement.executeUpdate(delete);
                traceSQL(delete);
            }
            debugRows(count);
        } catch (SQLException e) {
            throw new SQLRuntimeException(message("Error truncating table {}", table), e);
        }
    }

    /**
     * Processes the input values and transform them to the data type of
     * the given table.
     *
     * @param table The table name
     * @param data  The input values
     * @return The transformed data
     */
    public Map<String, Object> processData(String table, Map<String, String> data) {
        Map<String, JDBCType> types = columnTypes(table(parser.unquote(table)));
        return data.entrySet().stream()
                .collect(LinkedHashMap::new, (m, e) -> {
                    String column = column(table(parser.unquote(table)), parser.unquote(e.getKey()));
                    Object value = e.getValue();
                    if (value != null) {
                        if (!types.containsKey(column)) {
                            throw new SQLRuntimeException("Column {}.{} not found", parser.unquote(table), column);
                        }
                        switch (types.get(column)) {
                            case BIT:
                            case BOOLEAN:
                                value = Boolean.parseBoolean(e.getValue());
                                break;
                            case TINYINT:
                            case BIGINT:
                            case INTEGER:
                            case SMALLINT:
                                if (e.getValue().contains(".")) value = new BigDecimal(e.getValue()).toBigInteger();
                                else value = new BigInteger(e.getValue());
                                break;
                            case DECIMAL:
                            case DOUBLE:
                            case FLOAT:
                            case NUMERIC:
                            case REAL:
                                if (!e.getValue().contains(".")) value = new BigInteger(e.getValue());
                                else value = new BigDecimal(e.getValue());
                                break;
                            case DATE:
                                Calendar calendar = Chronic.parse(e.getValue(), new Options(false))
                                        .getBeginCalendar();
                                value = Timestamp.valueOf(
                                        LocalDateTime.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId())
                                                .truncatedTo(ChronoUnit.DAYS), true
                                );
                                break;
                            case TIMESTAMP:
                            case TIME:
                            case TIME_WITH_TIMEZONE:
                            case TIMESTAMP_WITH_TIMEZONE:
                                Calendar calendar2 = Chronic.parse(e.getValue(), new Options(false))
                                        .getBeginCalendar();
                                value = DATE_TIME_FORMATTER.withZone(calendar2.getTimeZone().toZoneId())
                                        .format(calendar2.toInstant());
                                value = Timestamp.valueOf(value.toString(), false);
                        }
                    }
                    m.put(column, value);
                }, LinkedHashMap::putAll);
    }

    /**
     * Gets a {@link Select.Builder} from given sql.
     *
     * @param sql The select string
     * @return The {@code Select} builder
     */
    public Select.Builder select(String sql) {
        sql = sql.replaceAll(";$", "");
        traceSQL(sql);
        return new Select.Builder(this, sql);
    }

    /**
     * Gets a {@link Update.Builder} from given sql.
     *
     * @param sql The update operation string
     * @return The {@code Update} builder
     */
    public Update.Builder update(String sql) {
        return new Update.Builder(this, sql);
    }

    /**
     * Gets a {@link Call.Builder} from given sql.
     *
     * @param sql The callable operation string
     * @return The {@code Call} builder
     */
    public Call.Builder call(String sql) {
        traceSQL(sql);
        return new Call.Builder(this, sql);
    }


    private String catalog() {
        try {
            return Optional.ofNullable(connection.parameters().catalog()).orElse(connection.get().getCatalog());
        } catch (SQLException e) {
            LOGGER.warn(e.getMessage());
            return null;
        }
    }

    private String schema() {
        try {
            return Optional.ofNullable(connection.parameters().schema()).orElse(connection.get().getSchema());
        } catch (SQLException e) {
            LOGGER.warn(e.getMessage());
            return null;
        }
    }

    public Connection connection() {
        return connection.get();
    }
}
