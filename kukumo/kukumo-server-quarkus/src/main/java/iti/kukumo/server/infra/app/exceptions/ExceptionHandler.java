package iti.kukumo.server.infra.app.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import iti.kukumo.api.KukumoException;

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
        if (exception instanceof KukumoException) {
        	return error(Response.Status.BAD_REQUEST, exception);
        } else {
        	return error(Response.Status.INTERNAL_SERVER_ERROR, exception);
        }
    }

    private Response error(Response.Status status, Exception exception) {
    	return Response.status(status).entity(new ErrorResponse(exception)).build();
    }
}
