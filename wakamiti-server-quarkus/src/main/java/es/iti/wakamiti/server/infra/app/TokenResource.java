/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.server.infra.app;


import java.util.UUID;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import es.iti.wakamiti.server.spi.TokenAuthentication;


@Path("/tokens")
public class TokenResource {

    @Context
    SecurityContext securityContext;

    @Inject
    TokenAuthentication tokenAuthentication;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
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