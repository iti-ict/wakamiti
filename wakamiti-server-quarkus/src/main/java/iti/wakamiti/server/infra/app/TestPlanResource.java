/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.wakamiti.server.infra.app;

import java.io.IOException;
import java.util.*;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.security.Authenticated;
import iti.wakamiti.api.plan.PlanSerializer;
import iti.wakamiti.core.Wakamiti;
import iti.wakamiti.api.plan.PlanNodeSnapshot;
import iti.wakamiti.server.domain.ExecutionService;


@Path("/plans")
@Authenticated
public class TestPlanResource {


    @Inject
    ExecutionService executionManager;

    private final ObjectMapper mapper = new ObjectMapper();
    private final PlanSerializer serializer = Wakamiti.planSerializer();



    @POST
    @Consumes("text/plain;charset=UTF-8,*/*")
    @Produces("application/json;charset=UTF-8")
    public String analyze(
        @Context SecurityContext securityContext,
        @QueryParam("resourceType") String resourceType,
        @QueryParam("workspace") String workspace,
        String body
    ) throws IOException {
        return serializer.serialize(analyzePlan(resourceType, workspace, body));
    }




    @SuppressWarnings("unchecked")
    private PlanNodeSnapshot analyzePlan(String resourceType, String workspace, String body)
    throws IOException {
        if (body == null || body.isEmpty()) {
            return executionManager.analyzeWorkspace(
                Objects.requireNonNull(workspace, "Request param 'workspace' is required")
            );
        } else if (body.startsWith("{")) {
            Map<String,String> files = mapper.readValue(body, HashMap.class);
            return executionManager.analyzeMultipleResources(files);
        } else {
            Objects.requireNonNull(resourceType, "Request param 'resourceType' is required");
            return executionManager.analyzeSingleResource(resourceType, body);
        }
    }
}