package iti.kukumo.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import iti.commons.jext.Extension;

@Extension(provider="iti.kukumo", name="kukumo-db-driver-connection", version="1.0-SNAPSHOT")
public class DriverConnectionManager implements ConnectionManager {

    
    @Override
    public Connection obtainConnection(ConnectionParameters parameters) throws SQLException {
        return DriverManager.getConnection(parameters.url(), parameters.username(), parameters.password());
    }

    @Override
    public void releaseConnection(Connection connection) throws SQLException {
        connection.close();
    }

    
}
