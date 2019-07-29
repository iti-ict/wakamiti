package iti.kukumo.db;

import iti.commons.jext.Extension;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Extension(provider="iti.kukumo", name="kukumo-db-driver-connection", version="1.0")
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
