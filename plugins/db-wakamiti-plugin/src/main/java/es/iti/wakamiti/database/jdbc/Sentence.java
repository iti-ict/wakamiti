/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.jdbc;


import es.iti.wakamiti.database.exception.SQLRuntimeException;

import java.sql.SQLException;
import java.sql.Statement;


public class Sentence<T extends Statement> implements AutoCloseable {

    protected final Database db;
    protected final T statement;
    protected final String sql;

    public Sentence(Database db, T statement, String sql) {
        this.db = db;
        this.statement = statement;
        this.sql = sql;
    }

    @Override
    public void close() {
        try {
            if (!statement.isClosed()) {
                statement.close();
            }
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

}
