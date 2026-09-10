/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.report.allure.internal;


import java.io.IOException;
import java.util.function.Function;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;


/**
 * Serializes enum values using a caller-provided value mapping function.
 *
 * @param <T> enum type being serialized
 */
public class EnumValueSerializer<T extends Enum<T>> extends JsonSerializer<T> {

    private final Function<T, String> valueMapper;

    /**
     * Creates a serializer using the given value mapping function.
     *
     * @param valueMapper function used to obtain the serialized value
     */
    public EnumValueSerializer(
            Function<T, String> valueMapper
    ) {
        this.valueMapper = valueMapper;
    }

    /**
     * Serializes an enum value as the string returned by the configured mapper.
     *
     * @param value enum value to serialize
     * @param generator JSON generator receiving the serialized value
     * @param provider serializer provider
     * @throws IOException if the value cannot be written
     */
    @Override
    public void serialize(
            T value,
            JsonGenerator generator,
            SerializerProvider provider
    ) throws IOException {
        generator.writeString(valueMapper.apply(value));
    }

}
