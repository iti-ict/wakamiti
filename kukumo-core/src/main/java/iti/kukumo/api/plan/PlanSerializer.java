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

import iti.kukumo.util.ThrowableFunction;


public interface PlanSerializer {

    /**
     * Serialize the given plan or plan node into a string
     */
    String serialize(PlanNode node) throws IOException;

    /**
     * Serialize the given
     * @throws IOException
     */
    String serialize(PlanNodeDescriptor node) throws IOException;


    /**
     * Deserialize the given string into a {@link PlanNodeDescriptor} object
     * @throws IOException
     */
    PlanNodeDescriptor deserialize(String json) throws IOException;

    PlanNodeDescriptor read(Reader reader) throws IOException;


    default PlanNodeDescriptor read(InputStream inputStream) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            return read(reader);
        }
    }


    default PlanNodeDescriptor read(File file) throws IOException {
        try (InputStream stream = new FileInputStream(file)) {
            return read(stream);
        }
    }

    default PlanNodeDescriptor read(Path path) throws IOException {
        try (InputStream stream = new FileInputStream(path.toFile())) {
            return read(stream);
        }
    }

    default Collection<PlanNodeDescriptor> read(Collection<Path> paths) throws IOException {
        return paths.stream().map(ThrowableFunction.unchecked(path -> read(path))).collect(Collectors.toList());
    }




    void write(Writer writer, PlanNodeDescriptor node) throws IOException;

    void write(Writer writer, PlanNode node) throws IOException;


}
