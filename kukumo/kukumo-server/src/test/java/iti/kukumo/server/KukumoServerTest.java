package iti.kukumo.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import iti.kukumo.api.KukumoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class KukumoServerTest {

    @Autowired
    private MockMvc mockMvc;

    private String executionID;

    @Test
    public void runKukumo() throws Exception {
        runKukumoExecution()
        .andDo(MockMvcResultHandlers.print())
        .andDo(this::extractExecutionId)
        .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/kukumo/executions/"+executionID))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.status().isOk());
    }


    @Test
    public void searchExecutions() throws Exception {
        runKukumoExecution();
        runKukumoExecution();
        runKukumoExecution();
        mockMvc.perform(MockMvcRequestBuilders.get("/kukumo/executions?size=1&page=2"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testConfiguration() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/kukumo/configuration"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testContributors() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/kukumo/contributors"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk());
    }


    private ResultActions runKukumoExecution() throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders
            .post("/kukumo/run?contentType=gherkin")
            .contentType(MediaType.TEXT_PLAIN)
            .content(Files.readString(Path.of("src/test/resources/simpleScenario.feature")))
        );
    }


    private void extractExecutionId(MvcResult mvcResult) throws IOException {
        var data = mvcResult.getResponse().getContentAsString();
        var json = new ObjectMapper().readValue(data, HashMap.class);
        this.executionID = (String)((Map<?,?>)json.get("data")).get(KukumoConfiguration.EXECUTION_ID);
    }


}
