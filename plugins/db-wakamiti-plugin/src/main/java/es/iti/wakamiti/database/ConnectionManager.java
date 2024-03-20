package es.iti.wakamiti.database;


import es.iti.commons.jext.ExtensionPoint;
import es.iti.wakamiti.api.extensions.Contributor;

import java.sql.Connection;
import java.sql.SQLException;


/**
 * This is a simple interface that provides a valid database connection.
 * Implementations can use any mechanism: connection pools, manually
 * creation, etc.
 */
@ExtensionPoint
public interface ConnectionManager extends Contributor {

    /**
     * Obtain a valid connection ready to accept requests
     *
     * @param parameters The connection parameters
     * @return a valid connection
     * @throws SQLException when the connection was not successfully retrieved
     */
    Connection obtainConnection(ConnectionParameters parameters) throws SQLException;


    /**
     * Release the connection because it is not required anymore.
     *
     * @param connection The connection to release
     * @throws SQLException when the connection was not successfully retrieved
     */
    void releaseConnection(Connection connection) throws SQLException;


    /**
     * Obtains a valid connection according to an existing connection. If the
     * current connection is closed or invalid, a new one will be retrieved;
     * otherwise, the current connection is returned.
     *
     * @param connection The current connection
     * @return a valid connection
     * @throws SQLException when the connection was not successfully retrieved 
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
