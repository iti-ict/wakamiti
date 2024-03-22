/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.jdbc;


import es.iti.wakamiti.database.exception.SQLRuntimeException;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.IntConsumer;

import static es.iti.wakamiti.database.jdbc.LogUtils.debugRows;
import static es.iti.wakamiti.database.jdbc.LogUtils.traceSQL;


/**
 * Represents a database update operation, used to execute SQL statements
 * that modify data in a database.
 */
public class Update extends Sentence<PreparedStatement> {

    private Update(String sql, Database db, PreparedStatement statement) {
        super(db, statement, sql);
    }

    /**
     * Executes the update operation without any additional action.
     *
     * @return The Update instance
     */
    public Update execute() {
        return execute(null);
    }

    /**
     * Executes the update operation and performs the specified action with
     * the result count.
     *
     * @param action The action to perform with the result count
     * @return The Update instance
     */
    public Update execute(IntConsumer action) {
        try {
            traceSQL(sql);
            int result = statement.executeUpdate();
            debugRows(result);
            if (action != null) action.accept(result);
            return this;
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    /**
     * Builder class for constructing instances of Update.
     */
    public static final class Builder {

        private final Database db;
        private final String sql;

        Builder(Database db, String sql) {
            this.db = db;
            this.sql = sql;
        }

        /**
         * Prepares the Update operation by creating a PreparedStatement.
         *
         * @return The prepared Update instance
         */
        public Update prepare() {
            try {
                return new Update(sql, db, db.connection().prepareStatement(sql, Statement.NO_GENERATED_KEYS));
            } catch (SQLException e) {
                throw new SQLRuntimeException(e);
            }
        }

        /**
         * Executes the Update operation after preparation.
         *
         * @return The Update instance after execution
         */
        public Update execute() {
            return prepare().execute();
        }

        /**
         * Executes the Update operation after preparation and performs the
         * specified action with the result count.
         *
         * @param action The action to perform with the result count
         * @return The Update instance after execution
         */
        public Update execute(IntConsumer action) {
            return prepare().execute(action);
        }

    }

}
