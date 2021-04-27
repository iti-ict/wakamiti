package iti.kukumo.server.infra.app;

import java.io.IOException;
import java.util.*;

import javax.inject.Inject;
import javax.ws.rs.*;

import org.jboss.resteasy.annotations.Body;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;

import iti.kukumo.api.Kukumo;
import iti.kukumo.api.plan.*;
import iti.kukumo.server.domain.ExecutionService;
import iti.kukumo.server.domain.model.KukumoExecution;

@Path("/plans")
public class TestPlanResource {


    private final ExecutionService executionManager;
    private final ObjectMapper mapper = new ObjectMapper();
    private final PlanSerializer serializer = Kukumo.planSerializer();

	@Inject
    public TestPlanResource(ExecutionService executionManager) {
        this.executionManager = executionManager;
    }


    @POST
    @Consumes("text/plain;charset=UTF-8,*/*")
    @Produces("application/json;charset=UTF-8")
    public String analyze(
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
