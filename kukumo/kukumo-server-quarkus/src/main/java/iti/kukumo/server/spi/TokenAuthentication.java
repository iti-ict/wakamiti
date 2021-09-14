package iti.kukumo.server.spi;

public interface TokenAuthentication {

    String newToken(String user);

}