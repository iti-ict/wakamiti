/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.database.jdbc;


import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.database.ConnectionManager;
import es.iti.wakamiti.database.ConnectionParameters;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Provides JDBC connection management including obtaining, testing, and releasing connections.
 */
public class ConnectionProvider implements AutoCloseable {

    private static final Logger LOGGER = WakamitiLogger.forClass(ConnectionProvider.class);
    private static final ConnectionManager connectionManager = WakamitiAPI.instance().extensionManager()
            .getExtension(ConnectionManager.class)
            .orElseThrow(() -> new WakamitiException("Cannot find a connection manager"));
    private final ConnectionParameters parameters;
    private Connection connection;

    /**
     * Constructs a ConnectionProvider with the given connection parameters.
     *
     * @param parameters The connection parameters.
     */
    public ConnectionProvider(ConnectionParameters parameters) {
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
                connection = connectionManager.obtainConnection(parameters);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(
                            "Using database connection of type {} provided by {contributor}",
                            connection.getClass().getSimpleName(),
                            connectionManager.info()
                    );
                }
            } else {
                connection = connectionManager.refreshConnection(connection, parameters);
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
                connectionManager.releaseConnection(connection);
            }
        } catch (SQLException e) {
            throw new WakamitiException("Connection closure has failed", e);
        }
    }
}
