package iti.kukumo.core.plan;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.api.plan.PlanNodeDescriptor;
import iti.kukumo.api.plan.PlanSerializer;

public class DefaultPlanSerializer implements PlanSerializer  {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .enable(SerializationFeature.INDENT_OUTPUT)
            ;


    @Override
    public PlanNodeDescriptor deserialize(String json) throws IOException {
        return OBJECT_MAPPER.readValue(json, PlanNodeDescriptor.class);
    }

    @Override
    public PlanNodeDescriptor read(Reader reader) throws IOException {
        return OBJECT_MAPPER.readValue(reader, PlanNodeDescriptor.class);
    }


    @Override
    public String serialize(PlanNodeDescriptor node) throws IOException {
        return OBJECT_MAPPER.writeValueAsString(node);
    }

    @Override
    public String serialize(PlanNode node) throws IOException {
        return serialize(node.obtainDescriptor());
    }

    @Override
    public void write(Writer writer, PlanNodeDescriptor node) throws IOException {
        OBJECT_MAPPER.writeValue(writer, node);
    }

    @Override
    public void write(Writer writer, PlanNode node) throws IOException {
        write(writer, node.obtainDescriptor());
    }
}
