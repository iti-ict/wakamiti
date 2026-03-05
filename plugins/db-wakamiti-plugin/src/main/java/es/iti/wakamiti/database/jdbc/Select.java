/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.jdbc;


import es.iti.wakamiti.database.exception.SQLRuntimeException;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static es.iti.wakamiti.database.jdbc.LogUtils.traceResultRow;


/**
 * Represents an SQL SELECT statement that retrieves data from a database.
 * It provides methods for mapping, filtering, reducing, and streaming the results.
 *
 * @param <T> The type of the result retrieved by the SELECT statement
 */
public class Select<T> extends Sentence<Statement> {

    private final ResultSet resultset;
    private final Function<ResultSet, Optional<T>> mapper;
    private BinaryOperator<T> reducer;

    /**
     * Constructs a new Select object with the provided parameters.
     *
     * @param db        The database connection
     * @param sql       The SQL query string
     * @param statement The SQL statement object
     * @param resultset The result set obtained from executing the SQL query
     * @param mapper    The mapper function to convert result set rows to objects of type T
     */
    private Select(Database db, String sql, Statement statement, ResultSet resultset, Function<ResultSet, Optional<T>> mapper) {
        super(db, statement, sql);
        this.mapper = mapper;
        this.resultset = resultset;
    }

    /**
     * Creates a new Select object with the provided parameters.
     *
     * @param sql       The SQL query string
     * @param db        The database connection
     * @param statement The SQL statement object
     * @param resultset The result set obtained from executing the SQL query
     * @return A new Select object
     */
    private static Select<Object[]> create(String sql, Database db, Statement statement, ResultSet resultset) {
        return new Select<>(db, sql, statement, resultset, Select::defaultMap);
    }

    /**
     * Creates a new Select object with the provided parameters.
     *
     * @param sql       The SQL query string
     * @param db        The database connection
     * @param statement The SQL statement object
     * @param resultset The result set obtained from executing the SQL query
     * @param mapper    The mapper function to convert result set rows to objects of type T
     * @param <T>       The type of the result retrieved by the SELECT statement
     * @return A new Select object
     */
    private static <T> Select<T> create(
            String sql, Database db, Statement statement, ResultSet resultset, Function<ResultSet, Optional<T>> mapper) {
        return new Select<>(db, sql, statement, resultset, mapper);
    }

    /**
     * Maps a ResultSet row to an array of Objects.
     *
     * @param rs The ResultSet containing the row data
     * @return An Optional containing the mapped row as an array of Objects
     * @throws SQLRuntimeException If an SQL error occurs during mapping
     */
    protected static Optional<Object[]> defaultMap(ResultSet rs) {
        try {
            ResultSetMetaData metadata = rs.getMetaData();
            Object[] row = IntStream.range(1, metadata.getColumnCount() + 1).mapToObj(i -> {
                try {
                    return rs.getObject(i);
                } catch (SQLException e) {
                    try {
                        throw new SQLRuntimeException("Cannot read value of: " + metadata.getColumnName(i), e);
                    } catch (SQLException ignored) {
                        throw new SQLRuntimeException("Cannot read value of column index: " + i, e);
                    }

                }
            }).toArray();
            return Optional.of(row);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    /**
     * Establish a mapper of the current {@code Select} {@link ResultSet}.
     *
     * @param mapper The mapper function
     * @param <R>    The mapper function result type
     * @return A new {@link Select} with the given mapper
     */
    public <R> Select<R> map(Function<T, R> mapper) {
        return new Select<>(db, sql, statement, resultset, rs -> this.mapper.apply(rs).map(mapper));
    }

    /**
     * Filters the result set rows based on the specified predicate.
     *
     * @param filter The predicate to apply for filtering
     * @return A new Select object with the specified filter applied
     */
    public Select<T> filter(Predicate<T> filter) {
        return new Select<>(db, sql, statement, resultset, rs -> this.mapper.apply(rs).filter(filter));
    }

    /**
     * Processes the result set with the given reducer to obtain a single result.
     *
     * @param reducer The reducer function
     * @return An Optional containing the result of applying the reducer, or empty if the result set is empty
     */
    public Optional<T> reduce(BinaryOperator<T> reducer) {
        this.reducer = reducer;
        return stream().findFirst();
    }

    /**
     * Performs the given action with the current {@code Select}
     * {@link ResultSet}.
     *
     * @param consumer The action
     * @return The current {@code Select}
     */
    public Select<T> peek(Consumer<ResultSet> consumer) {
        consumer.accept(resultset);
        return this;
    }

    /**
     * Returns an array containing the names of the columns in the result set.
     *
     * @return An array of column names
     */
    public String[] getColumnNames() {
        try {
            ResultSetMetaData md = resultset.getMetaData();
            String[] columns = new String[md.getColumnCount()];
            for (int i = 0; i < columns.length; ++i) {
                columns[i] = md.getColumnName(i + 1);
            }
            return columns;
        } catch (SQLException e) {
            throw new SQLRuntimeException("Cannot read metadata", e);
        }
    }

    /**
     * Returns a stream of the processed {@code Select} result.
     *
     * @return The result stream
     */
    public Stream<T> stream() {
        try {
            List<T> results = new LinkedList<>();
            while (resultset.next()) {
                mapper.apply(resultset).ifPresent(row -> {
                    if (reducer != null && !results.isEmpty()) {
                        results.set(0, reducer.apply(results.get(0), row));
                    } else {
                        traceResultRow(row);
                        results.add(row);
                    }
                });
            }
            return results.stream();
        } catch (SQLException | SQLRuntimeException e) {
            throw new SQLRuntimeException("Error reading result", e);
        }
    }

    /**
     * Iterates the {@link ResultSet} lazily and applies the given action to
     * each mapped row.
     * <p>
     * Unlike {@link #stream()}, this method does not materialize all rows first,
     * so callers can add cooperative cancellation checks between rows.
     *
     * @param consumer action executed for each mapped row
     */
    public void forEachRow(Consumer<T> consumer) {
        try {
            while (resultset.next()) {
                mapper.apply(resultset).ifPresent(row -> {
                    traceResultRow(row);
                    consumer.accept(row);
                });
            }
        } catch (SQLException | SQLRuntimeException e) {
            throw new SQLRuntimeException("Error reading result", e);
        }
    }

    /**
     * Builder class for constructing Select instances.
     */
    public static final class Builder {

        private final Database db;
        private final String sql;
        private Integer queryTimeoutSeconds;

        /**
         * Constructs a new Builder instance with the specified Database and SQL query.
         *
         * @param db  The Database instance to use
         * @param sql The SQL query string
         */
        Builder(Database db, String sql) {
            this.db = db;
            this.sql = sql;
        }

        /**
         * Sets query timeout in seconds for this SELECT execution.
         *
         * @param seconds timeout in seconds; values lower than 1 are clamped to 1
         * @return this builder
         */
        public Builder queryTimeoutSeconds(int seconds) {
            this.queryTimeoutSeconds = Math.max(1, seconds);
            return this;
        }

        /**
         * Executes the SQL query and returns a Select instance with the
         * ResultSet mapped to Object arrays.
         *
         * @return A Select instance with the mapped ResultSet
         * @throws SQLRuntimeException If an SQL error occurs during execution
         */
        public Select<Object[]> get() {
            return execute(Select::defaultMap);
        }

        /**
         * Executes the SQL query and returns a Select instance with the
         * ResultSet mapped using the specified mapper function.
         *
         * @param mapper The mapper function to map ResultSet rows to a custom type
         * @param <R>    The type returned by the mapper function
         * @return A Select instance with the mapped ResultSet
         * @throws SQLRuntimeException If an SQL error occurs during execution
         */
        public <R> Select<R> get(Function<ResultSet, R> mapper) {
            return execute(rs -> Optional.ofNullable(mapper.apply(rs)));
        }

        /**
         * Shared execution path for both default mapping and custom mapping.
         * <p>
         * It guarantees:
         * <ul>
         *   <li>statement timeout is configured before running the query,</li>
         *   <li>statement is closed on failure to avoid resource leaks.</li>
         * </ul>
         *
         * @param mapper row-mapping function
         * @param <R> mapped row type
         * @return open {@link Select} instance containing statement and result set
         */
        private <R> Select<R> execute(Function<ResultSet, Optional<R>> mapper) {
            Statement statement = null;
            try {
                statement = db.connection().createStatement();
                configureStatement(statement);
                return Select.create(sql, db, statement, statement.executeQuery(sql), mapper);
            } catch (SQLException e) {
                closeQuietly(statement);
                throw new SQLRuntimeException("Error executing statement", e);
            }
        }

        /**
         * Applies optional JDBC query timeout to the statement.
         *
         * @param statement JDBC statement to configure
         * @throws SQLException if JDBC driver rejects timeout configuration
         */
        private void configureStatement(Statement statement) throws SQLException {
            if (queryTimeoutSeconds != null) {
                statement.setQueryTimeout(queryTimeoutSeconds);
            }
        }

        /**
         * Best-effort cleanup used only on builder execution failures.
         *
         * @param statement statement to close
         */
        private void closeQuietly(Statement statement) {
            if (statement == null) {
                return;
            }
            try {
                statement.close();
            } catch (SQLException ignored) {
                // best effort
            }
        }
    }

}
