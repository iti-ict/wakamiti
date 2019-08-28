package iti.kukumo.database;

public class ConnectionParameters {

    private String url;
    private String username;
    private String password;
    private String driver;


    public String url() {
        return url;
    }

    public ConnectionParameters url(String url) {
        this.url = url;
        return this;
    }

    public String username() {
        return username;
    }

    public ConnectionParameters username(String username) {
        this.username = username;
        return this;
    }

    public String password() {
        return password;
    }

    public ConnectionParameters password(String password) {
        this.password = password;
        return this;
    }

    public String driver() {
        return driver;
    }

    public ConnectionParameters driver(String driver) {
        this.driver = driver;
        return this;
    }
}
