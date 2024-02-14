/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.jdbc;


import es.iti.wakamiti.database.exception.SQLRuntimeException;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static es.iti.wakamiti.database.DatabaseHelper.formatToMap;
import static es.iti.wakamiti.database.DatabaseHelper.toMap;
import static es.iti.wakamiti.database.jdbc.LogUtils.debugRows;
import static es.iti.wakamiti.database.jdbc.LogUtils.warn;


public class Call<T> extends Sentence<PreparedStatement> {

    private final Function<ResultSet, Optional<T>> mapper;

    private final List<List<T>> resultSets = new LinkedList<>();

    private Call(Database db, String sql, PreparedStatement statement, Function<ResultSet, Optional<T>> mapper) {
        super(db, statement, sql);
        this.mapper = mapper;
    }

    private static Call<Object[]> create(Database db, String sql, PreparedStatement statement) {
        return new Call<>(db, sql, statement, Select::defaultMap);
    }

    private static <T> Call<T> create(Database db, String sql, PreparedStatement statement, Function<ResultSet, Optional<T>> mapper) {
        return new Call<>(db, sql, statement, mapper);
    }

    public <R> Call<R> map(Function<T, R> mapper) {
        return new Call<>(db, sql, statement, rs -> this.mapper.apply(rs).map(mapper));
    }

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

    public Stream<List<T>> stream() {
        return resultSets.stream();
    }

    public static final class Builder {

        private final Database db;
        private final String sql;

        Builder(Database db, String sql) {
            this.db = db;
            this.sql = sql;
        }

        public Call<Object[]> get() {
            try {
                return Call.create(db, sql,
                        db.connection().prepareStatement(sql));
            } catch (SQLException e) {
                throw new SQLRuntimeException("Error executing statement", e);
            }
        }

        public <R> Call<R> get(Function<ResultSet, R> mapper) {
            try {
                return Call.create(db, sql,
                        db.connection().prepareStatement(sql), rs -> Optional.ofNullable(mapper.apply(rs)));
            } catch (SQLException e) {
                throw new SQLRuntimeException("Error executing statement", e);
            }
        }

        public Call<Object[]> execute() {
            return get().execute();
        }

    }
}
