/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.wakamiti.database;


import java.sql.Connection;
import java.sql.SQLException;

import iti.commons.jext.ExtensionPoint;
import iti.wakamiti.api.extensions.Contributor;


@ExtensionPoint
/** The <tt>ConnectionManager</tt> is a simply interface that provides a valid * database connection. Implementations can use any mechanism: connection * pools, manually creation, etc. */
public interface ConnectionManager extends Contributor {

    /**
     * Obtain a valid connection ready to accept requests
     *
     * @param parameters The connection parameters
     * @return A valid connection
     * @throws SQLException when the connection was not successfully retrieved
     */
    Connection obtainConnection(ConnectionParameters parameters) throws SQLException;


    /**
     * Release the connection because it is not required anymore
     *
     * @param connection
     * @throws SQLException
     */
    void releaseConnection(Connection connection) throws SQLException;


    /**
     * Obtain a valid connection according to an existing connection. If the
     * current connection is closed or invalid, a new one will be retrieved;
     * otherwise, the current connection is returned.
     *
     * @param connection The current connection
     * @return A valid connection
     * @throws SQLException when the connection was not successfully retrieved *
     */
    default Connection refreshConnection(
        Connection connection,
        ConnectionParameters connectionParameters
    ) throws SQLException {
        if (connection.isClosed() || !connection.isValid(0)) {
            return obtainConnection(connectionParameters);
        } else {
            return connection;
        }
    }

}