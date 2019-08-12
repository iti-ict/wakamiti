package iti.kukumo.db;

import iti.commons.jext.Extension;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Extension(provider="iti.kukumo", name="kukumo-db-driver-connection", version="1.0")
public class DriverConnectionManager implements ConnectionManager {


    @Override
    public Connection obtainConnection(ConnectionParameters parameters) throws SQLException {
        validateParameters(parameters);
        return DriverManager.getConnection(parameters.url(), parameters.username(), parameters.password());
    }


    @Override
    public void releaseConnection(Connection connection) throws SQLException {
        connection.close();
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
