/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.kukumo.server.infra.app;

import java.io.IOException;
import java.util.*;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.security.Authenticated;
import iti.kukumo.server.domain.ExecutionService;
import iti.kukumo.server.domain.model.KukumoExecution;


@Path("/executions")
@Authenticated
public class ExecutionResource {

    @Inject
    ExecutionService executionManager;

    @Context
    SecurityContext securityContext;

    private final ObjectMapper mapper = new ObjectMapper();


    @POST
    @Consumes("text/plain;charset=UTF-8,*/*")
    @Produces("application/json;charset=UTF-8")
    @SuppressWarnings("unchecked")
	public KukumoExecution run(
        @QueryParam("resourceType") String resourceType,
        @QueryParam("workspace") String workspace,
        @QueryParam("async") Boolean async,
        String body
    ) throws IOException {

    	if (body == null || body.isEmpty()) {
    		return executionManager.runWorkspace(
				Objects.requireNonNull(workspace),
				Objects.requireNonNullElse(async, false)
			);
    	} else if (body.startsWith("{")) {
            Map<String,String> files = mapper.readValue(body, HashMap.class);
            return executionManager.runMultipleResources(
        		files,
        		Objects.requireNonNullElse(async, false)
    		);
        } else {
            Objects.requireNonNull(resourceType);
            return executionManager.runSingleResource(
        		resourceType,
        		body,
        		Objects.requireNonNullElse(async, false)
    		);
        }
    }



    @GET
    @Produces("application/json;charset=UTF-8")
    public List<KukumoExecution> getAliveExecutions()  {
        return executionManager.getAliveExecutions();
    }


    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{executionID}")
    public KukumoExecution getExecutionData(@PathParam("executionID") String executionID) {
        Objects.requireNonNull(executionID);
        return executionManager
            .getExecution(executionID)
            .orElseThrow(NotFoundException::new);
    }




}