/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.amqp;



public class AmqpConnectionParams {

    public AmqpConnectionParams(
        String host,
        String username,
        String password
    ) {
        this.host = host;
        this.username = username;
        this.password = password;
    }

    private String host;
    private String username;
    private String password;


    public String host() {
        return host;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }


}