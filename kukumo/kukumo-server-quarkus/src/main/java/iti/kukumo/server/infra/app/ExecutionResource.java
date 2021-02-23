package iti.kukumo.server.infra.app;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.fasterxml.jackson.databind.ObjectMapper;

import iti.kukumo.server.domain.ExecutionService;
import iti.kukumo.server.domain.model.KukumoExecution;

@Path("/executions")
public class ExecutionResource {

    private final ExecutionService executionManager;
    private final ObjectMapper mapper = new ObjectMapper();


    @Inject
    public ExecutionResource(ExecutionService executionManager) {
        this.executionManager = executionManager;
    }


    @POST
    @Consumes("text/plain;charset=UTF-8,*/*")
    @Produces("application/json;charset=UTF-8")
    @SuppressWarnings("unchecked")
	public KukumoExecution run(
        @QueryParam("resourceType") String resourceType,
        @QueryParam("workspace") String workspace,
        String body
    ) throws IOException {
        if (body.startsWith("{")) {
            Map<String,String> files = mapper.readValue(body, HashMap.class);
            return executionManager.runMultipleResources(files);
        } else if (workspace != null && !workspace.isBlank()) {
            return executionManager.runWorkspace(workspace);
        } else {
            Objects.requireNonNull(resourceType);
            return executionManager.runSingleResource(resourceType, body);
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
    public KukumoExecution getExecutionData(@PathParam("executionID") String executionID) throws IOException {
        Objects.requireNonNull(executionID);
        return executionManager
            .getExecution(executionID)
            .orElseThrow(NotFoundException::new);
    }

}