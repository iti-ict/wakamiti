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
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
@Extension(provider =  "es.iti.wakamiti", name = "database-driver-connection", version = "1.1")
public class DriverConnectionManager implements ConnectionManager {

    @Override
    public Connection obtainConnection(ConnectionParameters parameters) throws SQLException {
        validateParameters(parameters);
        if (parameters.driver() != null) {
            try {
                Class.forName(parameters.driver());
            } catch (ClassNotFoundException e) {
                throw new SQLException(
                    "JDBC Driver " + parameters.driver() + " not found in classpath"
                );
            }
        } else {
            parameters.driver(DriverManager.getDriver(parameters.url()).getClass().getName());
        }
        return DriverManager
            .getConnection(parameters.url(), parameters.username(), parameters.password());
    }


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