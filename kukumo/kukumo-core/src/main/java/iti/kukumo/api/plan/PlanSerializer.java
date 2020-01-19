/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api.plan;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import iti.kukumo.util.ThrowableFunction;


public class PlanSerializer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        .enable(SerializationFeature.INDENT_OUTPUT);


    /**
     * Deserialize the given string into a {@link PlanNodeSnapshot} object
     *
     * @throws IOException
     */
    public PlanNodeSnapshot deserialize(String json) throws IOException {
        return OBJECT_MAPPER.readValue(json, PlanNodeSnapshot.class);
    }


    /**
     * Serialize the given
     *
     * @throws IOException
     */
    public String serialize(PlanNodeSnapshot node) throws IOException {
        return OBJECT_MAPPER.writeValueAsString(node);
    }


    /**
     * Serialize the given iti.kukumo.test.gherkin.plan or
     * iti.kukumo.test.gherkin.plan node into a string
     */
    public String serialize(PlanNode node) throws IOException {
        return serialize(new PlanNodeSnapshot(node));
    }


    public void write(Writer writer, PlanNodeSnapshot node) throws IOException {
        OBJECT_MAPPER.writeValue(writer, node);
    }


    public void write(Writer writer, PlanNode node) throws IOException {
        write(writer, new PlanNodeSnapshot(node));
    }


    public PlanNodeSnapshot read(InputStream inputStream) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(
            inputStream, StandardCharsets.UTF_8
        )) {
            return read(reader);
        }
    }


    public PlanNodeSnapshot read(File file) throws IOException {
        try (InputStream stream = new FileInputStream(file)) {
            return read(stream);
        }
    }


    public PlanNodeSnapshot read(Path path) throws IOException {
        try (InputStream stream = new FileInputStream(path.toFile())) {
            return read(stream);
        }
    }


    public PlanNodeSnapshot read(Reader reader) throws IOException {
        return OBJECT_MAPPER.readValue(reader, PlanNodeSnapshot.class);
    }


    public Collection<PlanNodeSnapshot> read(Collection<Path> paths) throws IOException {
        return paths.stream().map(ThrowableFunction.unchecked(path -> read(path)))
            .collect(Collectors.toList());
    }
}
