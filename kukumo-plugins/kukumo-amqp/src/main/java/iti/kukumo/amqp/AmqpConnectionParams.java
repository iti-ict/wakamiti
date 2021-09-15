package iti.kukumo.amqp;

import java.util.List;

import static java.util.Objects.requireNonNull;

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