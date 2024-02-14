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
import java.util.function.Consumer;

import static es.iti.wakamiti.database.jdbc.LogUtils.debugRows;
import static es.iti.wakamiti.database.jdbc.LogUtils.traceSQL;


public class Update extends Sentence<PreparedStatement> {


    private Update(String sql, Database db, PreparedStatement statement) {
        super(db, statement, sql);
    }

    public Update execute() {
        return execute(null);
    }

    public Update execute(Consumer<Integer> action) {
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

    public static final class Builder {

        private final Database db;
        private final String sql;

        Builder(Database db, String sql) {
            this.db = db;
            this.sql = sql;
        }

        public Update prepare() {
            try {
                return new Update(sql, db, db.connection().prepareStatement(sql, Statement.NO_GENERATED_KEYS));
            } catch (SQLException e) {
                throw new SQLRuntimeException(e);
            }
        }

        public Update execute() {
            return prepare().execute();
        }

        public Update execute(Consumer<Integer> action) {
            return prepare().execute(action);
        }


    }

}
