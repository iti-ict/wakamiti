package iti.kukumo.server.infra.app;

import java.io.IOException;
import java.util.*;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.security.Authenticated;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.plan.*;
import iti.kukumo.server.domain.ExecutionService;


@Path("/plans")
@Authenticated
public class TestPlanResource {


    @Inject
    ExecutionService executionManager;

    private final ObjectMapper mapper = new ObjectMapper();
    private final PlanSerializer serializer = Kukumo.planSerializer();



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