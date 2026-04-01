/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.examples.spring.junit;


import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;


@Configuration
public class AppTestConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server h2TcpServer(@Value("${h2.tcp.port}") int h2TcpPort) throws SQLException {
        return Server.createTcpServer(
                "-tcp",
                "-ifNotExists",
                "-tcpPort", String.valueOf(h2TcpPort)
        );
    }

}
