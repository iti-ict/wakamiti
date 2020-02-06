package iti.kukumo.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import iti.kukumo.api.Kukumo;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
public class KukumoServerTest {

    @Autowired
    private MockMvc mockMvc;

    private String executionID;

    @Test
    public void runKukumo() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
           .post("/kukumo/run?contentType=gherkin")
           .contentType(MediaType.TEXT_PLAIN)
           .content(Files.readString(Path.of("src/test/resources/simpleScenario.feature")))
        )
        .andDo(MockMvcResultHandlers.print())
        .andDo(this::extractExecutionId)
        .andExpect(MockMvcResultMatchers.status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/kukumo/executions/"+executionID))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(MockMvcResultMatchers.status().isOk());
    }

    private void extractExecutionId(MvcResult mvcResult) throws IOException {
        var data = mvcResult.getResponse().getContentAsString();
        var json = new ObjectMapper().readValue(data, HashMap.class);
        this.executionID = (String)((Map<?,?>)json.get("metadata")).get("executionId");
    }


}
