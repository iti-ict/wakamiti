package iti.kukumo.spring.sample.app;

import java.sql.SQLException;

import org.h2.tools.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class App {
    
    public static void main( String[] args ) throws SQLException {
        Server server = Server.createTcpServer("-tcpPort","9092").start();
        ConfigurableApplicationContext context = SpringApplication.run(App.class, args);
        context.addApplicationListener(event -> server.stop());
    }
    
    
}
