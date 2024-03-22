/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.plan;


import es.iti.wakamiti.api.util.ThrowableFunction;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;


/**
 * This interface defines methods for the serialization and deserialization
 * of {@link PlanNodeSnapshot} objects.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public interface PlanSerializer {

    /**
     * Deserializes the given string into a {@link PlanNodeSnapshot} object.
     *
     * @param json The JSON string to deserialize.
     * @return The deserialized {@link PlanNodeSnapshot} object.
     * @throws IOException If an I/O error occurs during deserialization.
     */
    PlanNodeSnapshot deserialize(String json) throws IOException;

    /**
     * Serializes the given {@link PlanNodeSnapshot} object into a JSON string.
     *
     * @param node The {@link PlanNodeSnapshot} to serialize.
     * @return The serialized JSON string.
     * @throws IOException If an I/O error occurs during serialization.
     */
    String serialize(PlanNodeSnapshot node) throws IOException;

    /**
     * Writes the serialized representation of a {@link PlanNodeSnapshot} object
     * to a {@link Writer}.
     *
     * @param writer The {@link Writer} to write to.
     * @param node   The {@link PlanNodeSnapshot} to write.
     * @throws IOException If an I/O error occurs during writing.
     */
    void write(Writer writer, PlanNodeSnapshot node) throws IOException;

    /**
     * Reads the serialized representation of a {@link PlanNodeSnapshot} object
     * from a {@link Reader}.
     *
     * @param reader The {@link Reader} to read from.
     * @return The deserialized {@link PlanNodeSnapshot} object.
     * @throws IOException If an I/O error occurs during reading.
     */
    PlanNodeSnapshot read(Reader reader) throws IOException;

    /**
     * Serializes the given {@link PlanNode} object into a JSON string.
     *
     * @param node The {@link PlanNode} to serialize.
     * @return The serialized JSON string.
     * @throws IOException If an I/O error occurs during serialization.
     */
    default String serialize(PlanNode node) throws IOException {
        return serialize(new PlanNodeSnapshot(node));
    }

    /**
     * Writes the serialized representation of a {@link PlanNode} object
     * to a {@link Writer}.
     *
     * @param writer The {@link Writer} to write to.
     * @param node   The {@link PlanNode} to write.
     * @throws IOException If an I/O error occurs during writing.
     */
    default void write(Writer writer, PlanNode node) throws IOException {
        write(writer, new PlanNodeSnapshot(node));
    }

    /**
     * Reads the serialized representation of a {@link PlanNodeSnapshot} object
     * from an {@link InputStream}.
     *
     * @param inputStream The {@link InputStream} to read from.
     * @return The deserialized {@link PlanNodeSnapshot} object.
     * @throws IOException If an I/O error occurs during reading.
     */
    default PlanNodeSnapshot read(InputStream inputStream) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(
                inputStream, StandardCharsets.UTF_8
        )) {
            return read(reader);
        }
    }

    /**
     * Reads the serialized representation of a {@link PlanNodeSnapshot} object
     * from a {@link File}.
     *
     * @param file The {@link File} to read from.
     * @return The deserialized {@link PlanNodeSnapshot} object.
     * @throws IOException If an I/O error occurs during reading.
     */
    default PlanNodeSnapshot read(File file) throws IOException {
        try (InputStream stream = new FileInputStream(file)) {
            return read(stream);
        }
    }

    /**
     * Reads the serialized representation of a {@link PlanNodeSnapshot} object
     * from a {@link Path}.
     *
     * @param path The {@link Path} to read from.
     * @return The deserialized {@link PlanNodeSnapshot} object.
     * @throws IOException If an I/O error occurs during reading.
     */
    default PlanNodeSnapshot read(Path path) throws IOException {
        try (InputStream stream = new FileInputStream(path.toFile())) {
            return read(stream);
        }
    }

    /**
     * Reads the serialized representations of {@link PlanNodeSnapshot} objects
     * from a collection of {@link Path} instances.
     *
     * @param paths The collection of {@link Path} instances to read from.
     * @return The deserialized {@link PlanNodeSnapshot} objects.
     * @throws IOException If an I/O error occurs during reading.
     */
    default Collection<PlanNodeSnapshot> read(Collection<Path> paths) throws IOException {
        return paths.stream().map(ThrowableFunction.unchecked(this::read))
                .collect(Collectors.toList());
    }

}