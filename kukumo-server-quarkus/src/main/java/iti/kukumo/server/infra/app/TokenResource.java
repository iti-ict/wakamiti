package iti.kukumo.server.infra.app;


import java.util.UUID;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import iti.kukumo.server.spi.TokenAuthentication;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/tokens")
public class TokenResource {

    @Context
    SecurityContext securityContext;

    @Inject
    TokenAuthentication tokenAuthentication;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @PermitAll
    @Operation(
        summary =
            "Obtain an execution token",
        description =
            "Creates a new token for execution operations. If a token is passed to this "+
            "endpoint, the returned token would be a renewed one but bound to the same session."
    )
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