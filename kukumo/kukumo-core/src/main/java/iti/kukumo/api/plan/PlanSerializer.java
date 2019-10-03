package iti.kukumo.api.plan;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import iti.kukumo.util.ThrowableFunction;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;

public class PlanSerializer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        .enable(SerializationFeature.INDENT_OUTPUT)
    ;


    /**
     * Deserialize the given string into a {@link PlanNodeDescriptor} object
     * @throws IOException
     */
    public PlanNodeDescriptor deserialize(String json) throws IOException {
        return OBJECT_MAPPER.readValue(json, PlanNodeDescriptor.class);
    }


    /**
     * Serialize the given
     * @throws IOException
     */
    public String serialize(PlanNodeDescriptor node) throws IOException {
        return OBJECT_MAPPER.writeValueAsString(node);
    }


    /**
     * Serialize the given iti.kukumo.test.gherkin.plan or iti.kukumo.test.gherkin.plan node into a string
     */
    public String serialize(PlanNode node) throws IOException {
        return serialize(node.obtainDescriptor());
    }


    public void write(Writer writer, PlanNodeDescriptor node) throws IOException {
        OBJECT_MAPPER.writeValue(writer, node);
    }


    public void write(Writer writer, PlanNode node) throws IOException {
        write(writer, node.obtainDescriptor());
    }



    public PlanNodeDescriptor read(InputStream inputStream) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            return read(reader);
        }
    }


    public PlanNodeDescriptor read(File file) throws IOException {
        try (InputStream stream = new FileInputStream(file)) {
            return read(stream);
        }
    }

    public PlanNodeDescriptor read(Path path) throws IOException {
        try (InputStream stream = new FileInputStream(path.toFile())) {
            return read(stream);
        }
    }


    public PlanNodeDescriptor read(Reader reader) throws IOException {
        return OBJECT_MAPPER.readValue(reader, PlanNodeDescriptor.class);
    }


    public Collection<PlanNodeDescriptor> read(Collection<Path> paths) throws IOException {
        return paths.stream().map(ThrowableFunction.unchecked(path -> read(path))).collect(Collectors.toList());
    }
}
