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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;


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
    @Operation(
        summary =
            "Obtain the test plan for the given resources",
        description =
            "Analyze the given resources and build the resulting test plan, without executing it. "+
            "The resources can be provided in three ways: single resource directly as text body, " +
            "a dictionary of resources as a json body, or as a local workspace path (and empty body). "
    )
    @Parameters({
        @Parameter(
            name = "resourceType",
            description = "The test resource type passed in the body. Only used when the body is plain text",
            required = false,
            example = "gherkin"
        ),
        @Parameter(
            name = "wokrspace",
            description = "The local workspace path where the resources are located. Only used when the body is empty",
            required = false,
            example = "/home/luis/workspaces/myproject"
        )
    })
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