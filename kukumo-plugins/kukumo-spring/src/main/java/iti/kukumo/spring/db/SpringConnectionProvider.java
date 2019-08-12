package iti.kukumo.spring.db;

import iti.commons.jext.Extension;
import iti.kukumo.db.ConnectionManager;
import iti.kukumo.db.ConnectionParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Extension(
    provider="iti.kukumo",
    name="kukumo-db-springboot-datasource",
    version="1.0",
    externallyManaged = true, // because Spring bean infrastructure will managed the lifecycle
    overrides = "iti.kukumo.db.DriverConnectionManager"
)
@Component
@ConditionalOnProperty(SpringConnectionProvider.USE_SPRING_DATASOURCE)
public class SpringConnectionProvider implements ConnectionManager {

    public static final String USE_SPRING_DATASOURCE = "kukumo.database.useSpringDataSource";

    @Autowired
    private DataSource dataSource;

    @Override
    public Connection obtainConnection(ConnectionParameters parameters) throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseConnection(Connection connection) throws SQLException {
        connection.close();
    }
}
