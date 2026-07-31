/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.spring.db;


import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.database.ConnectionManager;
import es.iti.wakamiti.database.ConnectionParameters;


/**
 * {@link ConnectionManager} backed by a Spring-managed {@link DataSource}.
 * <p>
 * When enabled through {@link #USE_SPRING_DATASOURCE}, this implementation
 * overrides the default JDBC driver-based manager and delegates connection
 * creation/release to the application {@link DataSource}.
 * </p>
 */
@Extension(
        provider = "es.iti.wakamiti",
        name = "database-springboot-datasource",
        version = "2.13",
        externallyManaged = true, // because Spring bean infrastructure will managed the lifecycle
        overrides = "es.iti.wakamiti.database.DriverConnectionManager"
)
@Component
@ConditionalOnProperty(SpringConnectionProvider.USE_SPRING_DATASOURCE)
public class SpringConnectionProvider implements ConnectionManager {

    /** Property that enables the Spring-managed data source. */
    public static final String USE_SPRING_DATASOURCE = "wakamiti.database.useSpringDataSource";

    @Autowired
    private DataSource dataSource;

    /**
     * Obtains a connection from the injected Spring {@link DataSource}.
     * <p>
     * The {@code parameters} argument is ignored because DataSource credentials
     * and URL are resolved by Spring configuration.
     * </p>
     *
     * @param parameters unused connection parameters
     * @return an open JDBC connection
     * @throws SQLException when the DataSource cannot provide a connection
     */
    @Override
    public Connection obtainConnection(
            ConnectionParameters parameters
    ) throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Releases a connection previously obtained from this provider.
     *
     * @param connection connection to close
     * @throws SQLException when close fails
     */
    @Override
    public void releaseConnection(
            Connection connection
    ) throws SQLException {
        connection.close();
    }

}
