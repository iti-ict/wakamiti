package iti.kukumo.server.infra.app;


import java.util.UUID;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import iti.kukumo.server.spi.TokenAuthentication;

@Path("/tokens")
public class TokenResource {

    @Context
    SecurityContext securityContext;

    @Inject
    TokenAuthentication tokenAuthentication;

    @GET
    @Produces("text/plain")
    @PermitAll
    public String requestToken() {
        if (securityContext.getUserPrincipal() == null) {
           return tokenAuthentication.newToken(randomUser());
        } else {
           return tokenAuthentication.newToken(securityContext.getUserPrincipal().getName());
        }
    }



    private String randomUser() {
        return "u"+UUID.randomUUID().toString().substring(0,18);
    }

}