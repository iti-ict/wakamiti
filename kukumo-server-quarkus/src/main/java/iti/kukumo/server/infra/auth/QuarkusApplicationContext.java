package iti.kukumo.server.infra.auth;

import java.util.Optional;

import javax.enterprise.context.RequestScoped;

import iti.kukumo.server.spi.ApplicationContext;

@RequestScoped
public class QuarkusApplicationContext implements ApplicationContext {

    private String user;

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public Optional<String> user() {
        return Optional.ofNullable(user);
    }

}