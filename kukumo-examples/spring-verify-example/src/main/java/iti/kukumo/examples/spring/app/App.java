/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.examples.spring.app;


import org.h2.tools.DeleteDbFiles;
import org.h2.tools.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PreDestroy;
import java.sql.SQLException;


@SpringBootApplication
public class App {

    private static Server server;

    public static void main(String[] args) throws SQLException {
        server = Server.createTcpServer("-tcpPort", "9092").start();
        SpringApplication.run(App.class, args);
    }

    @PreDestroy
    public void shutdown() {
        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) { }
            server.stop();
            DeleteDbFiles.execute("~", "test", false);
        }).start();
    }
}