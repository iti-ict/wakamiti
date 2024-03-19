/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.jdbc;


import es.iti.wakamiti.database.exception.SQLRuntimeException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static es.iti.wakamiti.database.jdbc.LogUtils.debugRows;


/**
 * Represents a database call and provides methods to execute stored procedures
 * or functions and map their results.
 *
 * @param <T> The type of the result returned by the call
 */
public class Call<T> extends Sentence<PreparedStatement> {

    private final Function<ResultSet, Optional<T>> mapper;
    private final List<List<T>> resultSets = new LinkedList<>();

    /**
     * Constructs a {@code Call} object with the specified database, SQL statement, prepared statement,
     * and result mapper function.
     *
     * @param db        The database instance
     * @param sql       The SQL statement
     * @param statement The prepared statement
     * @param mapper    The result mapper function
     */
    private Call(Database db, String sql, PreparedStatement statement, Function<ResultSet, Optional<T>> mapper) {
        super(db, statement, sql);
        this.mapper = mapper;
    }

    /**
     * Creates a {@code Call} object with the specified database, SQL statement, and prepared statement.
     *
     * @param db        The database instance
     * @param sql       The SQL statement
     * @param statement The prepared statement
     * @return The created {@code Call} object
     */
    private static Call<Object[]> create(Database db, String sql, PreparedStatement statement) {
        return new Call<>(db, sql, statement, Select::defaultMap);
    }

    /**
     * Creates a {@code Call} object with the specified database, SQL statement, prepared statement,
     * and result mapper function.
     *
     * @param db        The database instance
     * @param sql       The SQL statement
     * @param statement The prepared statement
     * @param mapper    The result mapper function
     * @param <T>       The type of the result
     * @return The created {@code Call} object
     */
    private static <T> Call<T> create(Database db, String sql, PreparedStatement statement, Function<ResultSet, Optional<T>> mapper) {
        return new Call<>(db, sql, statement, mapper);
    }

    /**
     * Maps the result of the call using the specified mapper function.
     *
     * @param mapper The result mapper function
     * @param <R>    The type of the mapped result
     * @return A new {@code Call} object with the mapped result
     */
    public <R> Call<R> map(Function<T, R> mapper) {
        return new Call<>(db, sql, statement, rs -> this.mapper.apply(rs).map(mapper));
    }

    /**
     * Executes the database call.
     *
     * @return This {@code Call} object after execution
     * @throws SQLRuntimeException If an SQL error occurs during execution
     */
    public Call<T> execute() {
        try {
            boolean available = statement.execute();
            int processed = 0;
            int total = 0;
            int updateCount;
            while (true) {
                if (processed > 0) {
                    available = statement.getMoreResults();
                }
                updateCount = statement.getUpdateCount();
                processed++;
                if (!available && updateCount == -1) {
                    break;
                }
                if (available) {
                    try (ResultSet rs = statement.getResultSet()) {
                        List<T> result = new LinkedList<>();
                        while (rs.next()) {
                            mapper.apply(rs).ifPresent(result::add);
                        }
                        resultSets.add(result);
                    }
                } else {
                    total += updateCount;
                }
            }
            debugRows(total);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
        return this;
    }

    /**
     * Returns a stream of result sets obtained from the executed call.
     *
     * @return A stream of result sets
     */
    public Stream<List<T>> stream() {
        return resultSets.stream();
    }

    /**
     * The {@code Builder} class provides methods to construct {@code Call} objects.
     */
    public static final class Builder {

        private final Database db;
        private final String sql;

        Builder(Database db, String sql) {
            this.db = db;
            this.sql = sql;
        }

        /**
         * Constructs and returns a {@code Call} object with the specified database and SQL statement.
         *
         * @return The constructed {@code Call} object
         * @throws SQLRuntimeException If an SQL error occurs
         */
        public Call<Object[]> get() {
            try {
                return Call.create(db, sql, db.connection().prepareStatement(sql));
            } catch (SQLException e) {
                throw new SQLRuntimeException("Error executing statement", e);
            }
        }

        /**
         * Constructs and returns a {@code Call} object with the specified
         * database, SQL statement, and result mapper function.
         *
         * @param mapper The result mapper function
         * @param <R>    The type of the mapped result
         * @return The constructed {@code Call} object
         * @throws SQLRuntimeException If an SQL error occurs
         */
        public <R> Call<R> get(Function<ResultSet, R> mapper) {
            try {
                return Call.create(db, sql,
                        db.connection().prepareStatement(sql), rs -> Optional.ofNullable(mapper.apply(rs)));
            } catch (SQLException e) {
                throw new SQLRuntimeException("Error executing statement", e);
            }
        }

        /**
         * Executes the database call and returns the result as an array of objects.
         *
         * @return The result of the executed call as an array of objects
         * @throws SQLRuntimeException If an SQL error occurs during execution
         */
        public Call<Object[]> execute() {
            return get().execute();
        }

    }

}
