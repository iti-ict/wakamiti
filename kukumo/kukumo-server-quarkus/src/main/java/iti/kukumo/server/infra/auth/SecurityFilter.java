package iti.kukumo.server.infra.auth;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.container.*;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.Provider;

@Provider
@PreMatching
public class SecurityFilter implements ContainerRequestFilter {

    @Inject
    QuarkusApplicationContext applicationContext;

    @Context
    SecurityContext securityContext;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (securityContext.getUserPrincipal() == null) {
            applicationContext.setUser("anonymous");
        } else {
            applicationContext.setUser(securityContext.getUserPrincipal().getName());
        }
    }
}