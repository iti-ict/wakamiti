/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.commons.imconfig.internal;


import es.iti.commons.imconfig.*;

import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;


public abstract class AbstractConfiguration implements Configuration {

    protected final ConfigurationFactory builder;
    protected final Map<String, PropertyDefinition> definitions;

    protected AbstractConfiguration(ConfigurationFactory builder, Map<String, PropertyDefinition> definitions) {
        this.builder = builder;
        this.definitions = definitions;
    }

    @Override
    public Configuration append(Configuration otherConfiguration) {
        return builder.merge(this, otherConfiguration);
    }

    @Override
    public Configuration appendFromAnnotation(Class<?> configuredClass) {
        return builder.merge(this, builder.fromAnnotation(configuredClass));
    }

    @Override
    public Configuration appendFromAnnotation(AnnotatedConfiguration annotation) {
        return builder.merge(this, builder.fromAnnotation(annotation));
    }

    @Override
    public Configuration appendFromResource(String resourcePath, ClassLoader classLoader) {
        return builder.merge(this, builder.fromResource(resourcePath, classLoader));
    }

    @Override
    public Configuration appendFromEnvironment() {
        return builder.merge(this, builder.fromEnvironment());
    }

    @Override
    public Configuration appendFromSystem() {
        return builder.merge(this, builder.fromSystem());
    }

    @Override
    public Configuration appendFromMap(Map<String, ?> propertyMap) {
        return builder.merge(this, builder.fromMap(propertyMap));
    }

    @Override
    public Configuration appendFromPath(Path path) {
        return builder.merge(this, builder.fromPath(path));
    }

    @Override
    public Configuration appendFromProperties(Properties properties) {
        return builder.merge(this, builder.fromProperties(properties));
    }

    @Override
    public Configuration appendFromURI(URI uri) {
        return builder.merge(this, builder.fromURI(uri));
    }

    @Override
    public Configuration appendProperty(String property, String value) {
        Map<String, String> singlePropertyMap = new HashMap<>();
        singlePropertyMap.put(property, value);
        return builder.merge(this, builder.fromMap(singlePropertyMap));
    }

    @Override
    public Map<String, PropertyDefinition> getDefinitions() {
        return Collections.unmodifiableMap(definitions);
    }

    @Override
    public Optional<PropertyDefinition> getDefinition(String key) {
        return Optional.ofNullable(definitions.get(key));
    }

    @Override
    public boolean hasDefinition(String key) {
        return definitions.containsKey(key);
    }

    @Override
    public List<String> validations(String key) {
        return getDefinition(key).map(definition -> validations(key, definition)).orElseGet(List::of);
    }

    private List<String> validations(String key, PropertyDefinition definition) {
        List<String> values = definition.multivalue() ?
                getList(key, String.class) :
                get(key, String.class).map(List::of).orElseGet(List::of);
        return values
                .stream()
                .map(definition::validate)
                .flatMap(Optional::stream)
                .collect(toList());
    }

    @Override
    public Map<String, List<String>> validations() {
        var invalidValues = keyStream()
                .map(key -> Map.entry(key, validations(key)))
                .filter(entry -> !entry.getValue().isEmpty());
        var missingValues = definitions.values().stream()
                .filter(PropertyDefinition::required)
                .filter(it -> !this.hasProperty(it.property()))
                .map(it -> Map.entry(
                        it.property(),
                        it.validate(null).map(List::of).orElseGet(List::of)
                ));
        return Stream.concat(invalidValues, missingValues)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Configuration validate() {
        var validations = validations();
        if (!validations.isEmpty()) {
            var message = validations.entrySet().stream()
                    .map(entry -> String.format("%s : %s", entry.getKey(),
                            String.join("\n" + (" ").repeat(entry.getKey().length() + 3), entry.getValue())
                    ))
                    .collect(Collectors.joining("\n\t",
                            "The configuration contains one or more invalid values:\n\t", ""));
            throw new ConfigurationException(message);
        }
        return this;
    }

    @Override
    public Configuration accordingDefinitions(Collection<PropertyDefinition> definitions) {
        return builder.merge(this, builder.accordingDefinitions(definitions));
    }

    @Override
    public Configuration accordingDefinitionsFromPath(Path path) {
        return builder.merge(this, builder.accordingDefinitionsFromPath(path));
    }

    @Override
    public Configuration accordingDefinitionsFromURI(URI uri) {
        return builder.merge(this, builder.accordingDefinitionsFromURI(uri));
    }

    @Override
    public Configuration accordingDefinitionsFromResource(String resource, ClassLoader classLoader) {
        return builder.merge(
                this,
                builder.accordingDefinitionsFromResource(resource, classLoader)
        );
    }

    @Override
    public String getDefinitionsToString() {
        return getDefinitions().values().stream()
                .map(PropertyDefinition::toString)
                .collect(Collectors.joining("\n"));
    }

}
