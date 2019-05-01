package iti.kukumo.db;

public class ConnectionParameters {

    private String url;
    private String username;
    private String password;
    
    
    public ConnectionParameters(String url, String username, String password) {
        super();
        this.url = url;
        this.username = username;
        this.password = password;
    }
 
    
    public String url() {
        return url;
    }
    
    public String username() {
        return username;
    }
    
    public String password() {
        return password;
    }
    
    
}
