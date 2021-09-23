/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.spring.db;


import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import iti.commons.jext.Extension;
import iti.kukumo.database.ConnectionManager;
import iti.kukumo.database.ConnectionParameters;


@Extension(provider = "iti.kukumo", name = "database-springboot-datasource", version = "1.1", externallyManaged = true, // because
                                                                                                                               // Spring
                                                                                                                               // bean
                                                                                                                               // infrastructure
                                                                                                                               // will
                                                                                                                               // managed
                                                                                                                               // the
                                                                                                                               // lifecycle
                overrides = "iti.kukumo.database.DriverConnectionManager")
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