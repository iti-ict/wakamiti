package iti.kukumo.server.app;

import iti.kukumo.api.Kukumo;
import iti.kukumo.server.ExecutionManager;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("kukumo")
public class KukumoController {

    private final ExecutionManager executionManager;

    @Autowired
    public KukumoController(ExecutionManager executionManager) {
        this.executionManager = executionManager;
    }

    @PostMapping(value="run", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String run(@RequestParam String contentType, @RequestBody String content) throws IOException {
        return executionManager.run(contentType, IOUtils.toInputStream(content,StandardCharsets.UTF_8)).serialize();
    }


    @GetMapping(value="executions/{executionID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getExecutionData(@PathVariable String executionID) throws IOException {
        return executionManager.getExecutionData(executionID).serialize();
    }


}
