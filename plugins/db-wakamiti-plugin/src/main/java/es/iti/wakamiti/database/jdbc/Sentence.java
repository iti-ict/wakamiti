/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.jdbc;


import es.iti.wakamiti.database.exception.SQLRuntimeException;

import java.sql.SQLException;
import java.sql.Statement;


/**
 * The {@code Sentence} class represents a SQL statement and provides methods for its execution and cleanup.
 *
 * @param <T> The type of SQL statement (e.g., PreparedStatement, CallableStatement)
 */
public class Sentence<T extends Statement> implements AutoCloseable {

    protected final Database db;
    protected final T statement;
    protected final String sql;

    /**
     * Constructs a new Sentence object with the given database connection, SQL statement, and statement object.
     *
     * @param db        The database connection
     * @param statement The SQL statement object
     * @param sql       The SQL statement string
     */
    public Sentence(Database db, T statement, String sql) {
        this.db = db;
        this.statement = statement;
        this.sql = sql;
    }

    /**
     * Closes the SQL statement.
     * If the statement is not already closed, it will be closed.
     */
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
