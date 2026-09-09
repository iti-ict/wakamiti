/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.jdbc;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;

import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.database.ConnectionManager;
import es.iti.wakamiti.database.ConnectionParameters;


/**
 * Provides Connection services to the surrounding component.
 */
public class ConnectionProvider implements AutoCloseable {

    private static final Logger LOGGER = WakamitiLogger.forClass(ConnectionProvider.class);
    private static final ConnectionManager CONNECTION_MANAGER = WakamitiAPI.instance().extensionManager()
            .getExtension(ConnectionManager.class)
            .orElseThrow(() -> new WakamitiException("Cannot find a connection manager"));
    private final ConnectionParameters parameters;
    private Connection connection;

    /**
     * Constructs a ConnectionProvider with the given connection parameters.
     *
     * @param parameters The connection parameters.
     */
    public ConnectionProvider(
            ConnectionParameters parameters
    ) {
        this.parameters = parameters;
    }

    /**
     * Retrieves the connection parameters.
     *
     * @return The connection parameters.
     */
    public ConnectionParameters parameters() {
        return parameters;
    }

    /**
     * Obtains a JDBC connection.
     *
     * @return The JDBC connection.
     * @throws WakamitiException if obtaining the connection fails.
     */
    public Connection get() {
        try {
            if (connection == null) {
                connection = CONNECTION_MANAGER.obtainConnection(parameters);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(
                            "Using database connection of type {} provided by {contributor}",
                            connection.getClass().getSimpleName(),
                            CONNECTION_MANAGER.info()
                    );
                }
            } else {
                connection = CONNECTION_MANAGER.refreshConnection(connection, parameters);
            }
            if (connection == null) {
                throw new WakamitiException("Connection manager returned a null connection");
            }
            return connection;
        } catch (SQLException e) {
            throw new WakamitiException("Connection has failed", e);
        }
    }

    /**
     * Tests the connection.
     *
     * @throws WakamitiException if the connection test fails.
     */
    public void test() {
        String sql = DatabaseType.fromUrl(parameters.url()).healthCheck();
        LOGGER.trace("Testing connection | {sql}", sql);
        try (Statement statement = get().createStatement()) {
            statement.executeQuery(sql).close();
        } catch (SQLException e) {
            throw new WakamitiException("Connection test failed", e);
        }
    }

    /**
     * Closes the connection provider, releasing the associated database connection.
     *
     * @throws WakamitiException if an SQL exception occurs while closing the connection
     */
    @Override
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                CONNECTION_MANAGER.releaseConnection(connection);
            }
        } catch (SQLException e) {
            throw new WakamitiException("Connection closure has failed", e);
        }
    }

}
