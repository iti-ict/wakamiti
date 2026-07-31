/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core;


import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.plan.PlanSerializer;


/**
 * Jackson-based JSON serializer for {@link PlanNodeSnapshot}.
 * <p>
 * Serialization uses:
 * {@link JsonInclude.Include#NON_EMPTY} (omit empty values),
 * {@link JavaTimeModule} (Java time support), and
 * {@link SerializationFeature#INDENT_OUTPUT} (pretty-printed JSON).
 * The same mapper configuration is used for both string and stream APIs.
 * </p>
 */
public class JsonPlanSerializer implements PlanSerializer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY)
            .enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * Deserializes a JSON payload into a plan snapshot.
     *
     * @param json serialized plan snapshot
     * @return parsed snapshot
     * @throws IOException when JSON is malformed or incompatible
     */
    @Override
    public PlanNodeSnapshot deserialize(
            String json
    ) throws IOException {
        return OBJECT_MAPPER.readValue(json, PlanNodeSnapshot.class);
    }

    /**
     * Serializes a plan snapshot to JSON.
     *
     * @param node snapshot to serialize
     * @return pretty-printed JSON payload
     * @throws IOException when serialization fails
     */
    @Override
    public String serialize(
            PlanNodeSnapshot node
    ) throws IOException {
        return OBJECT_MAPPER.writeValueAsString(node);
    }

    /**
     * Writes a serialized snapshot to a character stream.
     *
     * @param writer destination writer
     * @param node   snapshot to serialize
     * @throws IOException when writing fails
     */
    @Override
    public void write(
            Writer writer,
            PlanNodeSnapshot node
    ) throws IOException {
        OBJECT_MAPPER.writeValue(writer, node);
    }

    /**
     * Reads and deserializes a snapshot from a character stream.
     *
     * @param reader source reader
     * @return parsed snapshot
     * @throws IOException when JSON cannot be read or parsed
     */
    @Override
    public PlanNodeSnapshot read(
            Reader reader
    ) throws IOException {
        return OBJECT_MAPPER.readValue(reader, PlanNodeSnapshot.class);
    }

}
