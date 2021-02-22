package iti.kukumo.server.infra.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import iti.kukumo.server.domain.ExecutionService;
import iti.kukumo.server.domain.model.KukumoExecution;
import org.apache.commons.io.IOUtils;

import javax.inject.Inject;
import javax.validation.Validation;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    @Produces(MediaType.APPLICATION_JSON)
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
    @Produces(MediaType.APPLICATION_JSON)
    public List<KukumoExecution> getAliveExecutions()  {
        return executionManager.getAliveExecutions();
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{executionID}")
    public KukumoExecution getExecutionData(@PathParam("executionID") String executionID) throws IOException {
        Objects.requireNonNull(executionID);
        return executionManager
            .getExecution(executionID)
            .orElseThrow(NotFoundException::new);
    }

}