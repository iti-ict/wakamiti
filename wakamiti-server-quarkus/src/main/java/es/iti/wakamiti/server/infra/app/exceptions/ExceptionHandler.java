/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.server.infra.app.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.*;

import org.slf4j.*;

import io.jsonwebtoken.JwtException;
import es.iti.wakamiti.api.WakamitiException;

@Provider
public class ExceptionHandler implements ExceptionMapper<Exception> {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    @Override
    public Response toResponse(Exception exception) {
        logger.error("{}", exception.getMessage());
        logger.debug("<cause was>", exception);
        if (exception instanceof WebApplicationException) {
            return ((WebApplicationException) exception).getResponse();
        }
        if (exception instanceof WakamitiException) {
        	return error(Response.Status.BAD_REQUEST, exception);
        } else if (exception instanceof JwtException) {
            return error(Response.Status.UNAUTHORIZED, exception);
        } else {
        	return error(Response.Status.INTERNAL_SERVER_ERROR, exception);
        }
    }

    private Response error(Response.Status status, Exception exception) {
    	return Response.status(status).entity(new ErrorResponse(exception)).build();
    }
}