/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.amqp;


import java.net.URI;


/**
 * Immutable value object with the minimum information required to open an AMQP connection.
 * <p>
 * The class is intentionally simple:
 * <ul>
 *   <li>{@code uri}: broker endpoint plus optional query parameters (for example vhost settings).</li>
 *   <li>{@code username}/{@code password}: credentials used when the broker requires authentication.</li>
 * </ul>
 */
public class AmqpConnectionParams {

    /**
     * Creates a new immutable set of connection parameters.
     *
     * @param uri broker URI
     * @param username optional username (can be {@code null})
     * @param password optional password (can be {@code null})
     */
    public AmqpConnectionParams(
        URI uri,
        String username,
        String password
    ) {
        this.uri = uri;
        this.username = username;
        this.password = password;
    }

    private final URI uri;
    private final String username;
    private final String password;

    /**
     * @return broker URI used by protocol-specific clients
     */
    public URI uri() {
        return uri;
    }

    /**
     * @return configured username, or {@code null} when anonymous/default auth is used
     */
    public String username() {
        return username;
    }

    /**
     * @return configured password, or {@code null}
     */
    public String password() {
        return password;
    }

}
