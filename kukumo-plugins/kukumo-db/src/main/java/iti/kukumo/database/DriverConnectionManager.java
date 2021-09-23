/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.database;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import iti.commons.jext.Extension;


@Extension(provider = "iti.kukumo", name = "database-driver-connection", version = "1.1")
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
            throw new IllegalArgumentException("Database connection paramaters have not been set");
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