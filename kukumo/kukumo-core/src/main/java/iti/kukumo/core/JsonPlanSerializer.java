/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.core;


import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import iti.kukumo.api.plan.PlanSerializer;
import iti.kukumo.api.plan.PlanNodeSnapshot;


public class JsonPlanSerializer implements PlanSerializer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        .enable(SerializationFeature.INDENT_OUTPUT);


    @Override
    public PlanNodeSnapshot deserialize(String json) throws IOException {
        return OBJECT_MAPPER.readValue(json, PlanNodeSnapshot.class);
    }


    @Override
    public String serialize(PlanNodeSnapshot node) throws IOException {
        return OBJECT_MAPPER.writeValueAsString(node);
    }


    @Override
    public void write(Writer writer, PlanNodeSnapshot node) throws IOException {
        OBJECT_MAPPER.writeValue(writer, node);
    }


    @Override
    public PlanNodeSnapshot read(Reader reader) throws IOException {
        return OBJECT_MAPPER.readValue(reader, PlanNodeSnapshot.class);
    }


}