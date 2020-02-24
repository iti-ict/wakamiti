package iti.kukumo.server.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import iti.commons.jext.Extension;
import iti.kukumo.api.Kukumo;
import iti.kukumo.server.ExecutionCriteria;
import iti.kukumo.server.ExecutionManager;
import iti.kukumo.server.KukumoExecution;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "kukumo", produces =  MediaType.APPLICATION_JSON_VALUE)
public class KukumoController {

    private final ExecutionManager executionManager;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public KukumoController(ExecutionManager executionManager) {
        this.executionManager = executionManager;
    }


    @PostMapping(value="run", consumes = MediaType.TEXT_PLAIN_VALUE)
    public KukumoExecution run(@RequestParam String contentType, @RequestBody String content) {
        return executionManager.run(contentType, IOUtils.toInputStream(content,StandardCharsets.UTF_8));
    }


    @GetMapping("running")
    public List<KukumoExecution> getAliveExecutions()  {
        return executionManager.getAliveExecutions();
    }


    @GetMapping("executions/{executionID}")
    public KukumoExecution getExecutionData(@PathVariable String executionID) throws IOException {
        return executionManager.getExecution(executionID);
    }

    @GetMapping("executions")
    public List<KukumoExecution> getExecutionData(@PathVariable Map<String,String> parameters) throws IOException {
        ExecutionCriteria criteria = mapper.treeToValue(mapper.valueToTree(parameters),ExecutionCriteria.class);
        return executionManager.searchExecutionHistory(criteria);
    }


    @GetMapping("configuration")
    public Map<String,String> getConfiguration() {
        return Kukumo.contributors().globalDefaultConfiguration().asMap();
    }


    @GetMapping("contributors")
    public Map<String, List<Map<String,Object>>> getContributors() throws JsonProcessingException {
        Map<String, List<Map<String,Object>>> output = new LinkedHashMap<>();
        for (var extensionPoint : Kukumo.contributors().allContributors().entrySet()) {
            List<Map<String,Object>> contributors = new ArrayList<>();
            for (var contributor : extensionPoint.getValue()) {
                var extension = contributor.extensionMetadata();
                Map<String,Object> value = new LinkedHashMap<>();
                value.put("name", extension.name());
                value.put("version", extension.version());
                value.put("provider", extension.provider());
                value.put("extensionPointVersion", extension.extensionPointVersion());
                value.put("externallyManaged", extension.externallyManaged());
                value.put("overridable", extension.overridable());
                value.put("overrides", extension.overrides());
                value.put("implementationClass", contributor.getClass().getCanonicalName());
                value.put("implementationModule",contributor.getClass().getModule().getName());
                contributors.add(value);
            }
            output.put(extensionPoint.getKey().getSimpleName(),contributors);
        }
        return output;
    }




}
