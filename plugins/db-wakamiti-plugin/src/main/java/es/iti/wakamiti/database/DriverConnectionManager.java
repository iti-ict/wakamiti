/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import es.iti.commons.jext.Extension;


/**
 * Manages JDBC connections using a driver-based approach.
 */
@Extension(provider =  "es.iti.wakamiti", name = "database-driver-connection", version = "2.6")
public class DriverConnectionManager implements ConnectionManager {

    /**
     * Obtains a JDBC connection based on the provided connection parameters.
     *
     * @param parameters The connection parameters
     * @return A JDBC connection
     * @throws SQLException If an SQL exception occurs
     */
    @Override
    public Connection obtainConnection(ConnectionParameters parameters) throws SQLException {
        validateParameters(parameters);
        if (parameters.driver() != null) {
            try {
                Class.forName(parameters.driver());
            } catch (ClassNotFoundException e) {
                throw new SQLException("JDBC Driver " + parameters.driver() + " not found in classpath");
            }
        } else {
            parameters.driver(DriverManager.getDriver(parameters.url()).getClass().getName());
        }
        return DriverManager.getConnection(parameters.url(), parameters.username(), parameters.password());
    }

    /**
     * Releases the given JDBC connection.
     *
     * @param connection The JDBC connection to release
     * @throws SQLException If an SQL exception occurs
     */
    @Override
    public void releaseConnection(Connection connection) throws SQLException {
        try {
            if (!connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException | RuntimeException e) {
            throw new SQLException("Problem releasing JDBC connection: " + e.getMessage());
        }
    }

    /**
     * Validates the provided connection parameters.
     *
     * @param parameters The connection parameters to validate
     * @throws IllegalArgumentException If any of the connection parameters are null
     */
    private void validateParameters(ConnectionParameters parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("Database connection parameters have not been set");
        }
        if (parameters.url() == null) {
            throw new IllegalArgumentException("Database connection url has not been set");
        }
        if (parameters.username() == null) {
            throw new IllegalArgumentException("Database connection username has not been set");
        }
        if (parameters.password() == null) {
            throw new IllegalArgumentException("Database connection password has not been set");
        }
    }

}