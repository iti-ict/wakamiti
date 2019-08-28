package iti.kukumo.database;

import iti.commons.jext.Extension;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Extension(provider="iti.kukumo", name="kukumo-database-driver-connection", version="1.0")
public class DriverConnectionManager implements ConnectionManager {


    @Override
    public Connection obtainConnection(ConnectionParameters parameters) throws SQLException {
        validateParameters(parameters);
        if (parameters.driver() != null) {
            try {
                Class.forName(parameters.driver());
            } catch (ClassNotFoundException e) {
                throw new SQLException("JDBC Driver "+parameters.driver()+" not found in classpath");
            }
        }
        return DriverManager.getConnection(parameters.url(), parameters.username(), parameters.password());
    }


    @Override
    public void releaseConnection(Connection connection) throws SQLException {
        if (!connection.isClosed()) {
            connection.close();
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
