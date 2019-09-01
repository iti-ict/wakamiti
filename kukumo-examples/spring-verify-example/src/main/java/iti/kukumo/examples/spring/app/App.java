package iti.kukumo.examples.spring.app;

import java.sql.SQLException;

import org.h2.tools.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {

    public static void main( String[] args ) throws SQLException {
        Server.createTcpServer("-tcpPort","9092").start();
        SpringApplication.run(App.class, args);
    }


}
