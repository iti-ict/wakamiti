/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.wakamiti.examples.launcher;

import java.sql.SQLException;

import org.h2.tools.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

@SpringBootApplication
public class App {
    
    public static void main( String[] args ) throws SQLException {
        Server server = Server.createTcpServer("-tcpPort","9092").start();
        ConfigurableApplicationContext context = SpringApplication.run(App.class, args);
        context.addApplicationListener((ContextClosedEvent event) -> server.stop());
    }
    
    
}