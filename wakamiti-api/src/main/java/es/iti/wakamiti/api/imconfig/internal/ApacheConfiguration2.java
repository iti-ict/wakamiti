/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.imconfig.internal;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.configuration2.BaseConfiguration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.PropertyDefinition;


/**
 * Provides the Apache Configuration2 functionality used by Wakamiti.
 */
public class ApacheConfiguration2 extends AbstractConfiguration {

    protected final org.apache.commons.configuration2.Configuration conf;
    private final ApacheConfiguration2Factory configurationFactory;

    protected ApacheConfiguration2(
            ApacheConfiguration2Factory builder,
            Map<String, PropertyDefinition> definitions,
            org.apache.commons.configuration2.Configuration conf
    ) {
        super(builder, definitions);
        this.configurationFactory = builder;
        this.conf = conf;
    }

    protected ApacheConfiguration2(
            ApacheConfiguration2Factory builder,
            org.apache.commons.configuration2.Configuration conf
    ) {
        this(builder, Map.of(), conf);
    }

    @Override
    public Configuration withPrefix(
            String keyPrefix
    ) {
        BaseConfiguration innerConf = prepare(configurationFactory.newBaseConfiguration());
        conf.getKeys().forEachRemaining(key -> innerConf.addProperty(keyPrefix + "." + key, conf.getProperty(key)));
        return new ApacheConfiguration2(configurationFactory, definitions, innerConf);
    }

    @Override
    public Configuration filtered(
            String keyPrefix
    ) {
        BaseConfiguration innerConf = prepare(configurationFactory.newBaseConfiguration());
        conf.getKeys(keyPrefix).forEachRemaining(key -> {
            if (key.startsWith(keyPrefix)) {
                innerConf.addProperty(key, conf.getProperty(key));
            }
        });
        return new ApacheConfiguration2(configurationFactory, definitions, innerConf);
    }

    @Override
    public Configuration inner(
            String keyPrefix
    ) {
        if (keyPrefix == null || keyPrefix.isEmpty()) {
            return this;
        }
        var subset = conf.subset(keyPrefix);
        if (subset instanceof org.apache.commons.configuration2.AbstractConfiguration configuration) {
            prepare(configuration);
        }
        return new ApacheConfiguration2(configurationFactory, definitions, subset);
    }

    @Override
    public boolean isEmpty() {
        return conf.isEmpty();
    }

    @Override
    public boolean hasProperty(
            String key
    ) {
        return conf.containsKey(key);
    }

    @Override
    public Iterable<String> keys() {
        return keyList();
    }

    @Override
    public Iterator<String> keyIterator() {
        return conf.getKeys();
    }

    @Override
    public Stream<String> keyStream() {
        return keyList().stream();
    }

    private List<String> keyList() {
        List<String> keys = new ArrayList<>();
        conf.getKeys().forEachRemaining(keys::add);
        return keys;
    }

    @Override
    public <T> Optional<T> get(
            String key,
            Class<T> type
    ) {
        var definition = definitions.get(key);
        if (isMissingOrEmpty(key)) {
            return definition == null
                    ? Optional.empty()
                    : definition.defaultValue().map(value -> convert(value, type));
        }
        return Optional.ofNullable(conf.get(type, key, null));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(
            String key,
            TypeReference<T> type
    ) {
        JavaType jt = TypeFactory.defaultInstance().constructType(type.getType());
        Class<?> rawType = jt.getRawClass();
        if (Map.class.isAssignableFrom(rawType)) {
            Class<?> keyType = containedRawType(jt, 0);
            Class<?> valueType = containedRawType(jt, 1);
            return (Optional<T>) Optional.of(inner(key).asMap().entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> convert(entry.getKey(), keyType),
                            entry -> convert(entry.getValue(), valueType),
                            (first, ignored) -> first,
                            LinkedHashMap::new
                    )));
        }
        if (!jt.hasGenericTypes()) {
            return get(key, (Class<T>) rawType);
        }
        Class<?> elementType = containedRawType(jt, 0);
        if (List.class.isAssignableFrom(rawType)) {
            return (Optional<T>) Optional.of(getList(key, elementType));
        }
        if (Set.class.isAssignableFrom(rawType)) {
            return (Optional<T>) Optional.of(getSet(key, elementType));
        }
        if (Stream.class.isAssignableFrom(rawType)) {
            return (Optional<T>) Optional.of(getStream(key, elementType));
        }
        return get(key, (Class<T>) rawType);
    }

    private Class<?> containedRawType(
            JavaType type,
            int index
    ) {
        JavaType containedType = type.containedType(index);
        return containedType == null ? String.class : containedType.getRawClass();
    }

    @Override
    public <T> List<T> getList(
            String key,
            Class<T> type
    ) {
        return conf.getList(type, key, List.of());
    }

    @Override
    public <T> Set<T> getSet(
            String key,
            Class<T> type
    ) {
        return new HashSet<>(getList(key, type));
    }

    @Override
    public <T> Stream<T> getStream(
            String key,
            Class<T> type
    ) {
        return getList(key, type).stream();
    }

    @Override
    public Properties asProperties() {
        Properties properties = new Properties();
        conf.getKeys().forEachRemaining(key -> properties.put(key, conf.getString(key)));
        return properties;
    }

    @Override
    public Map<String, String> asMap() {
        Map<String, String> map = new LinkedHashMap<>();
        conf.getKeys().forEachRemaining(key -> map.put(key, conf.getString(key)));
        return map;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("configuration:\n---------------\n");
        conf.getKeys().forEachRemaining(key -> {
            final List<String> values = getList(key, String.class);
            String value = "<undefined>";
            if (values.size() == 1) {
                value = values.get(0);
            } else if (!values.isEmpty()) {
                value = values.toString();
            }
            string
                    .append(key)
                    .append(" : ")
                    .append(value)
                    .append("\n");
        });
        return string.append("---------------").toString();
    }

    @Override
    public void forEach(
            BiConsumer<String, String> consumer
    ) {
        conf.getKeys().forEachRemaining(key -> consumer.accept(key, get(key, String.class).orElse(null)));
    }

    private boolean isMissingOrEmpty(
            String key
    ) {
        Object raw = conf.getProperty(key);
        return raw == null || raw instanceof String value && value.isEmpty();
    }

    private <T> T convert(
            Object raw,
            Class<T> type
    ) {
        var configuration = (org.apache.commons.configuration2.AbstractConfiguration) conf;
        return configuration.getConversionHandler().to(raw, type, configuration.getInterpolator());
    }

    private <T extends org.apache.commons.configuration2.AbstractConfiguration> T prepare(
            T configuration
    ) {
        var current = (org.apache.commons.configuration2.AbstractConfiguration) conf;
        configuration.setConversionHandler(current.getConversionHandler());
        configuration.setListDelimiterHandler(current.getListDelimiterHandler());
        return configuration;
    }

}
