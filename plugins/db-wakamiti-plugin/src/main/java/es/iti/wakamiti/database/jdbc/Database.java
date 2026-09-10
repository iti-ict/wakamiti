/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.jdbc;


import static es.iti.wakamiti.api.util.MapUtils.entryCollector;
import static es.iti.wakamiti.database.jdbc.LogUtils.debugRows;
import static es.iti.wakamiti.database.jdbc.LogUtils.message;
import static es.iti.wakamiti.database.jdbc.LogUtils.traceSQL;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.slf4j.Logger;

import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.database.SQLParser;
import es.iti.wakamiti.database.exception.SQLRuntimeException;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;


/**
 * Provides methods to interact with a database, including querying
 * tables, retrieving column information, performing data transformations,
 * and executing SQL statements.
 */
public final class Database {

    private static final Logger LOGGER = WakamitiLogger.forName("es.iti.wakamiti.database");
    private static final Map<String, Schema> CACHED_SCHEMA = new HashMap<>();
    private static final Map<String, ResolvedTable> CACHED_TABLES = new HashMap<>();
    private static final String COLUMN_NAME = "COLUMN_NAME";

    private final ConnectionProvider connection;
    private final DatabaseType type;
    private final SQLParser parser;
    private final Schema schema;

    /**
     * Constructs a new Database instance with the provided connection provider.
     *
     * @param connection The connection provider
     */
    private Database(
            ConnectionProvider connection
    ) {
        this.connection = connection;
        this.type = DatabaseType.fromUrl(connection.parameters().url());
        this.parser = new SQLParser(type, connection.parameters().autoTrim());
        this.schema = CACHED_SCHEMA.computeIfAbsent(connection.parameters().url(), k -> new Schema());
    }

    /**
     * Gets a new {@link Database} with retrieved connection provider.
     *
     * @param connection The connection provider
     * @return the new {@code Database}
     */
    public static Database from(
            ConnectionProvider connection
    ) {
        return new Database(connection);
    }

    /**
     * Gets the type of the database.
     *
     * @return The database type
     */
    public DatabaseType type() {
        return type;
    }

    /**
     * Gets the SQL parser associated with this database instance.
     *
     * @return The SQL parser
     */
    public SQLParser parser() {
        return parser;
    }

    /**
     * Gets the number of records in the given table.
     *
     * @param table The table name
     * @return the number of records
     */
    public long count(
            String table
    ) {
        try (Statement statement = connection()
                .createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            String query = parser.sqlSelectCountFrom(table).toString();
            traceSQL(query);
            try (ResultSet rs = statement.executeQuery(query)) {
                long count = 0;
                while (rs.next()) {
                    count = rs.getLong(1);
                }
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
    public String table(
            String table
    ) {
        return resolveTable(table).sqlName();
    }

    /**
     * Gets the column name in stored format.
     *
     * @param table  The table name
     * @param column The column name
     * @return The stored format
     */
    public String column(
            final String table,
            String column
    ) {
        ResolvedTable resolvedTable = resolveTable(table);
        String tableKey = resolvedTable.cacheKey();
        UnaryOperator<String> retrieve = col -> {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Retrieving column {} of table {}", col, resolvedTable.sqlName());
            }
            try (ResultSet rs = connection().getMetaData()
                    .getColumns(resolvedTable.catalog(), resolvedTable.schema(), resolvedTable.name(),
                            "_".repeat(parser.unquote(column).length()))) {
                String name = null;
                while (rs.next()) {
                    String current = rs.getString(COLUMN_NAME);
                    if (col.equalsIgnoreCase(current)) {
                        name = current;
                        break;
                    }
                }
                if (name == null) {
                    throw new SQLRuntimeException("The column {}.{} does not exist", resolvedTable.sqlName(), col);
                }
                return name;
            } catch (SQLException e) {
                throw new SQLRuntimeException(
                        message("Error retrieving column {}.{}", parser.unquote(column), resolvedTable.sqlName()), e);
            }
        };

        if (schema.columns.containsKey(tableKey)) {
            Map<String, String> columns = schema.columns.get(tableKey);
            return columns.computeIfAbsent(parser.unquote(column), retrieve);
        } else {
            LinkedHashMap<String, String> columns = new LinkedHashMap<>();
            columns.put(parser.unquote(column), retrieve.apply(parser.unquote(column)));
            schema.columns.put(tableKey, columns);
        }
        return schema.columns.get(tableKey).get(parser.unquote(column));
    }

    /**
     * Gets the name of columns that make up the primary key.
     *
     * @param table The table name
     * @return the name of columns
     */
    public Stream<String> primaryKey(
            String table
    ) {
        ResolvedTable resolvedTable = resolveTable(table);
        return schema.pk.computeIfAbsent(resolvedTable.cacheKey(), k -> {
            LOGGER.debug("Retrieving primary key of table {}", resolvedTable.sqlName());
            ArrayList<String> primaryKeys = new ArrayList<>();
            try (ResultSet rs = connection().getMetaData().getPrimaryKeys(
                    resolvedTable.catalog(), resolvedTable.schema(), resolvedTable.name())) {
                while (rs.next()) {
                    primaryKeys.add(rs.getString(COLUMN_NAME));
                }
                return primaryKeys;
            } catch (SQLException e) {
                throw new SQLRuntimeException(
                        message("Error retrieving primary key of table {}", resolvedTable.sqlName()), e);
            }
        }).stream();
    }

    /**
     * Gets the column types of the given table.
     *
     * @param table The table name
     * @return the column types
     */
    public Map<String, JDBCType> columnTypes(
            String table
    ) {
        ResolvedTable resolvedTable = resolveTable(table);
        return schema.types.computeIfAbsent(resolvedTable.cacheKey(), k -> {
            LOGGER.debug("Retrieving column types of table {}", resolvedTable.sqlName());
            LinkedHashMap<String, JDBCType> types = new LinkedHashMap<>();
            try (ResultSet rs = connection().getMetaData()
                    .getColumns(resolvedTable.catalog(), resolvedTable.schema(), resolvedTable.name(), null)) {
                while (rs.next()) {
                    types.put(
                            rs.getString(COLUMN_NAME),
                            JDBCType.valueOf(rs.getInt("DATA_TYPE"))
                    );
                }
                return types;
            } catch (SQLException e) {
                throw new SQLRuntimeException(
                        message("Error retrieving column types of table {}", resolvedTable.sqlName()), e);
            }
        });
    }

    /**
     * Truncates the given table.
     *
     * @param table The table name
     */
    public void truncate(
            String table
    ) {
        try (Statement statement = connection().createStatement()) {
            int count = 0;
            try {
                count += (int) count(table);

                String query = message("TRUNCATE TABLE {}", parser.format(table));
                traceSQL(query);
                statement.executeUpdate(query);
            } catch (SQLException e) {
                String query = message("DELETE FROM {}", parser.format(table));
                traceSQL(query);
                count = statement.executeUpdate(query);
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
    public Map<String, Object> processData(
            String table,
            Map<String, String> data
    ) {
        Map<String, JDBCType> types = columnTypes(table);
        return data.entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(
                        column(table, parser.unquote(e.getKey())), e.getValue()))
                .peek(e -> {
                    if (!types.containsKey(e.getKey())) {
                        throw new SQLRuntimeException("Column {}.{} not found", parser.unquote(table), e.getKey());
                    }
                })
                .collect(entryCollector(Map.Entry::getKey, e ->
                        this.type.formatter().formatValue(e.getValue(), types.get(e.getKey()))));
    }

    /**
     * Gets a {@link Select.Builder} from given sql.
     *
     * @param sql The select string
     * @return The {@code Select} builder
     */
    public Select.Builder select(
            String sql
    ) {
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
    public Update.Builder update(
            String sql
    ) {
        return new Update.Builder(this, sql);
    }

    /**
     * Gets a {@link Call.Builder} from given sql.
     *
     * @param sql The callable operation string
     * @return The {@code Call} builder
     */
    public Call.Builder call(
            String sql
    ) {
        traceSQL(sql);
        return new Call.Builder(this, sql);
    }

    private ResolvedTable resolveTable(
            String table
    ) {
        RequestedTable requested = parseTable(table);
        String defaultCatalog = catalog();
        String defaultSchema = schema();
        String cacheKey = Stream.of(
                        connection.parameters().url(),
                        defaultCatalog,
                        defaultSchema,
                        requested.catalog(),
                        requested.schema(),
                        requested.name()
                )
                .map(value -> Optional.ofNullable(value).orElse(""))
                .map(String::toUpperCase)
                .collect(java.util.stream.Collectors.joining("|"));
        return CACHED_TABLES.computeIfAbsent(cacheKey,
                key -> retrieveTable(requested, defaultCatalog, defaultSchema, key));
    }

    private RequestedTable parseTable(
            String table
    ) {
        try {
            net.sf.jsqlparser.statement.select.Select select =
                    (net.sf.jsqlparser.statement.select.Select) SQLParser.parseStatement("SELECT * FROM " + table);
            PlainSelect plainSelect = select.getPlainSelect();
            if (!(plainSelect.getFromItem() instanceof Table parsedTable)) {
                throw new SQLRuntimeException("Invalid table name {}", table);
            }
            String database = parsedTable.getDatabase().getDatabaseName();
            return new RequestedTable(
                    unquote(database),
                    unquote(parsedTable.getSchemaName()),
                    unquote(parsedTable.getName())
            );
        } catch (JSQLParserException | ClassCastException e) {
            throw new SQLRuntimeException(message("Invalid table name {}", table), e);
        }
    }

    private ResolvedTable retrieveTable(
            RequestedTable requested,
            String defaultCatalog,
            String defaultSchema,
            String cacheKey
    ) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Retrieving the table {}", requested.qualifiedName());
        }
        List<MetadataScope> scopes = new ArrayList<>();
        if (requested.catalog() != null) {
            scopes.add(new MetadataScope(requested.catalog(), requested.schema(), false));
        } else if (requested.schema() != null) {
            scopes.add(new MetadataScope(defaultCatalog, requested.schema(), false));
            scopes.add(new MetadataScope(requested.schema(), null, true));
        } else {
            scopes.add(new MetadataScope(defaultCatalog, defaultSchema, false));
        }

        try {
            for (MetadataScope scope : scopes) {
                Optional<ResolvedTable> resolved = findTable(requested, scope, cacheKey);
                if (resolved.isPresent()) {
                    return resolved.get();
                }
            }
        } catch (SQLException e) {
            throw new SQLRuntimeException(message("Error retrieving the table {}", requested.qualifiedName()), e);
        }
        throw new SQLRuntimeException("The table {} does not exist", requested.qualifiedName());
    }

    private Optional<ResolvedTable> findTable(
            RequestedTable requested,
            MetadataScope scope,
            String cacheKey
    ) throws SQLException {
        try (ResultSet rs = connection().getMetaData().getTables(
                scope.catalog(), scope.schema(), "_".repeat(requested.name().length()), null)) {
            while (rs.next()) {
                String current = rs.getString("TABLE_NAME");
                if (requested.name().equalsIgnoreCase(current)) {
                    String actualCatalog = rs.getString("TABLE_CAT");
                    String actualSchema = rs.getString("TABLE_SCHEM");
                    String qualifiedName = qualifiedName(requested, scope, actualCatalog, actualSchema, current);
                    return Optional.of(new ResolvedTable(
                            actualCatalog,
                            actualSchema,
                            current,
                            qualifiedName,
                            cacheKey
                    ));
                }
            }
        }
        return Optional.empty();
    }

    private String qualifiedName(
            RequestedTable requested,
            MetadataScope scope,
            String actualCatalog,
            String actualSchema,
            String table
    ) {
        if (requested.catalog() != null) {
            return Stream.of(
                            Optional.ofNullable(actualCatalog).orElse(requested.catalog()),
                            Optional.ofNullable(actualSchema).orElse(requested.schema()),
                            table
                    )
                    .filter(Objects::nonNull)
                    .collect(java.util.stream.Collectors.joining("."));
        }
        if (requested.schema() != null) {
            String qualifier = scope.schemaAsCatalog() ? actualCatalog : actualSchema;
            return Optional.ofNullable(qualifier).orElse(requested.schema()) + "." + table;
        }
        return table;
    }

    private String unquote(
            String identifier
    ) {
        return identifier == null ? null : parser.unquote(identifier);
    }

    private String catalog() {
        String catalog = connection.parameters().catalog();
        if (catalog != null) {
            return catalog;
        }
        try {
            return connection.get().getCatalog();
        } catch (SQLException e) {
            LOGGER.warn(e.getMessage());
            return null;
        }
    }

    private String schema() {
        String schema = connection.parameters().schema();
        if (schema != null) {
            return schema;
        }
        try {
            return connection.get().getSchema();
        } catch (SQLException e) {
            LOGGER.warn(e.getMessage());
            return null;
        }
    }

    /**
     * Returns the live JDBC connection backing this database facade.
     * <p>
     * The connection remains owned by this database; callers must not close it
     * independently.
     *
     * @return active JDBC connection
     */
    public Connection connection() {
        return connection.get();
    }

    private record RequestedTable(String catalog, String schema, String name) {

        private String qualifiedName() {
            return Stream.of(catalog, schema, name)
                    .filter(Objects::nonNull)
                    .collect(java.util.stream.Collectors.joining("."));
        }

    }

    private record MetadataScope(String catalog, String schema, boolean schemaAsCatalog) {

    }

    private record ResolvedTable(String catalog, String schema, String name, String sqlName, String cacheKey) {

    }

}
