/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.wakamiti.spring.db;


import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import iti.commons.jext.Extension;
import iti.wakamiti.database.ConnectionManager;
import iti.wakamiti.database.ConnectionParameters;


@Extension(provider = "iti.wakamiti", name = "database-springboot-datasource", version = "1.1", externallyManaged = true, // because
                                                                                                                               // Spring
                                                                                                                               // bean
                                                                                                                               // infrastructure
                                                                                                                               // will
                                                                                                                               // managed
                                                                                                                               // the
                                                                                                                               // lifecycle
                overrides = "iti.wakamiti.database.DriverConnectionManager")
@Component
@ConditionalOnProperty(SpringConnectionProvider.USE_SPRING_DATASOURCE)
public class SpringConnectionProvider implements ConnectionManager {

    public static final String USE_SPRING_DATASOURCE = "wakamiti.database.useSpringDataSource";

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