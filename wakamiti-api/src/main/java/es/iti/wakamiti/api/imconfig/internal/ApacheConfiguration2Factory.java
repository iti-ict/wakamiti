/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.imconfig.internal;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.configuration2.AbstractConfiguration;
import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.EnvironmentConfiguration;
import org.apache.commons.configuration2.JSONConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.convert.DefaultConversionHandler;

import es.iti.wakamiti.api.imconfig.AnnotatedConfiguration;
import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.ConfigurationException;
import es.iti.wakamiti.api.imconfig.ConfigurationFactory;
import es.iti.wakamiti.api.imconfig.Property;
import es.iti.wakamiti.api.imconfig.PropertyDefinition;


/**
 * Creates and configures Apache Configuration2 instances.
 */
public class ApacheConfiguration2Factory implements ConfigurationFactory {

    private final PropertyDefinitionParser parser = new PropertyDefinitionParser();

    private char separator;

    @Override
    public ConfigurationFactory multiValueSeparator(
            char separator
    ) {
        if (separator == 0) {
            throw new IllegalArgumentException("Invalid separator symbol: " + separator);
        }
        this.separator = separator;
        return this;
    }

    @Override
    public boolean hasMultiValueSeparator() {
        return this.separator != 0;
    }

    @Override
    public char multiValueSeparator() {
        return this.separator;
    }

    @Override
    public Configuration merge(
            Configuration base,
            Configuration delta
    ) {
        AbstractConfiguration result = newBaseConfiguration();
        for (String property : delta.keys()) {
            var existing = base.getList(property, String.class);
            var added = delta.getList(property, String.class);
            if (existing.isEmpty() && added.isEmpty()) {
                result.setProperty(property, "");
            } else if (!added.isEmpty()) {
                added.forEach(value -> result.addProperty(property, value));
            }
        }
        for (String property : base.keys()) {
            if (result.containsKey(property)) {
                continue;
            }
            base.getList(property, String.class).forEach(value -> result.addProperty(property, value));
        }
        Map<String, PropertyDefinition> definitions = new HashMap<>(base.getDefinitions());
        definitions.putAll(delta.getDefinitions());

        return new ApacheConfiguration2(this, definitions, result);
    }

    @Override
    public Configuration empty() {
        return new ApacheConfiguration2(this, newBaseConfiguration());
    }

    @Override
    public Configuration fromAnnotation(
            Class<?> configuredClass
    ) {
        AnnotatedConfiguration annotation = Optional.ofNullable(configuredClass.getAnnotation(AnnotatedConfiguration.class))
                .orElseThrow(
                        () -> new ConfigurationException(
                                configuredClass + " is not annotated with @AnnotatedConfiguration"
                        )
                );
        return fromAnnotation(annotation, configuredClass.getClassLoader());
    }

    @Override
    public Configuration fromAnnotation(
            AnnotatedConfiguration annotation
    ) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = this.getClass().getClassLoader();
        }
        return fromAnnotation(annotation, classLoader);
    }

    private Configuration fromAnnotation(
            AnnotatedConfiguration annotation,
            ClassLoader classLoader
    ) {
        Configuration fileConfiguration = fromAnnotationPath(annotation, classLoader).orElseGet(this::empty);
        Configuration annotatedPropertiesConfiguration = fromAnnotationProperties(annotation);
        return fileConfiguration.append(annotatedPropertiesConfiguration);
    }

    private Configuration fromAnnotationProperties(
            AnnotatedConfiguration annotation
    ) {
        BaseConfiguration configuration = newBaseConfiguration();
        for (Property property : annotation.value()) {
            String[] value = property.value();
            if (value.length == 1) {
                configuration.addProperty(property.key(), value[0]);
            } else {
                configuration.addProperty(property.key(), value);
            }
        }
        return new ApacheConfiguration2(this, configuration);
    }

    private Optional<Configuration> fromAnnotationPath(
            AnnotatedConfiguration annotation,
            ClassLoader classLoader
    ) {
        String path = annotation.path().strip();
        if (path.isEmpty()) {
            return Optional.empty();
        }

        Configuration confFromPath = fromURI(annotationPathToURI(path), classLoader);
        String pathPrefix = annotation.pathPrefix().strip();
        if (!pathPrefix.isEmpty()) {
            confFromPath = confFromPath.inner(pathPrefix);
        }
        return Optional.of(confFromPath);
    }

    private URI annotationPathToURI(
            String path
    ) {
        if (path.startsWith("classpath:")) {
            String resource = path.substring("classpath:".length()).replaceFirst("^/+", "");
            return classpathResource(resource);
        }
        if (path.startsWith("file:") || path.startsWith("http:") || path.startsWith("https:")
                || path.startsWith("jar:")) {
            return URI.create(path);
        }
        return Path.of(path).toUri();
    }

    @Override
    public Configuration fromEnvironment() {
        return new ApacheConfiguration2(this, configure(new EnvironmentConfiguration()));
    }

    @Override
    public Configuration fromSystem() {
        return new ApacheConfiguration2(this, configure(new SystemConfiguration()));
    }

    @Override
    public Configuration fromPath(
            Path path
    ) {
        return fromURI(path.toUri());
    }

    @Override
    public Configuration fromProperties(
            Properties properties
    ) {
        final BaseConfiguration configuration = newBaseConfiguration();
        for (final Entry<Object, Object> property : properties.entrySet()) {
            configuration.addProperty(property.getKey().toString(), property.getValue());
        }
        return new ApacheConfiguration2(this, configuration);
    }

    @Override
    public Configuration fromMap(
            Map<String, ?> properties
    ) {
        final BaseConfiguration configuration = newBaseConfiguration();
        for (final Entry<String, ?> property : properties.entrySet()) {
            configuration.addProperty(property.getKey(), property.getValue());
        }
        return new ApacheConfiguration2(this, configuration);
    }

    private Configuration fromMap(
            Map<String, ?> properties,
            Collection<PropertyDefinition> definitions
    ) {
        final BaseConfiguration configuration = newBaseConfiguration();
        for (final Entry<String, ?> property : properties.entrySet()) {
            configuration.addProperty(property.getKey(), property.getValue());
        }
        var definitionMap = definitions.stream()
                .collect(Collectors.toMap(PropertyDefinition::property, x -> x));
        return new ApacheConfiguration2(this, definitionMap, configuration);
    }

    @Override
    public Configuration fromURI(
            URI uri
    ) {
        return fromURI(uri, null);
    }

    @Override
    public Configuration fromResource(
            String resource,
            ClassLoader classLoader
    ) {
        return fromURI(classpathResource(resource), classLoader);
    }

    private Configuration fromURI(
            URI uri,
            ClassLoader classLoader
    ) {
        try {
            return buildFromURL(adaptURI(uri, classLoader));
        } catch (MalformedURLException e) {
            throw new ConfigurationException(e);
        }
    }

    @Override
    public Configuration accordingDefinitions(
            Collection<PropertyDefinition> definitions
    ) {
        Map<String, String> defaultValues = definitions
                .stream()
                .filter(definition -> definition.defaultValue().isPresent())
                .collect(Collectors.toMap(
                        PropertyDefinition::property,
                        definition -> definition.defaultValue().orElseThrow()
                ));
        return fromMap(defaultValues, definitions);
    }

    @Override
    public Configuration accordingDefinitionsFromURI(
            URI uri
    ) {
        return accordingDefinitionsFromURI(uri, null);
    }

    private Configuration accordingDefinitionsFromURI(
            URI uri,
            ClassLoader classLoader
    ) {
        try (var inputStream = adaptURI(uri, classLoader).openStream()) {
            return accordingDefinitions(parser.read(inputStream));
        } catch (IOException e) {
            throw new ConfigurationException(e);
        }
    }

    @Override
    public Configuration accordingDefinitionsFromPath(
            Path path
    ) {
        return accordingDefinitionsFromURI(path.toUri());
    }

    @Override
    public Configuration accordingDefinitionsFromResource(
            String resource,
            ClassLoader classLoader
    ) {
        return accordingDefinitionsFromURI(classpathResource(resource), classLoader);
    }

    private URI classpathResource(
            String resource
    ) {
        return URI.create(String.format("classpath:///%s", resource));
    }

    private Configuration buildFromURL(
            URL url
    ) {
        Configuration configuration;
        String file = url.getFile();
        if (file.endsWith(".properties")) {
            configuration = buildFromPropertiesFile(url);
        } else if (file.endsWith(".json")) {
            configuration = buildFromJSON(url);
        } else if (file.endsWith(".xml")) {
            configuration = buildFromXML(url);
        } else if (file.endsWith(".yaml") || file.endsWith(".yml")) {
            configuration = buildFromYAML(url);
        } else {
            throw new ConfigurationException("Cannot determine resource type of " + url);
        }
        return configuration;
    }

    private Configuration buildFromJSON(
            URL url
    ) {
        try (InputStream stream = url.openStream()) {
            JSONConfiguration json = configure(new JSONConfiguration());
            json.read(stream);
            return new ApacheConfiguration2(this, Map.of(), json);
        } catch (IOException | org.apache.commons.configuration2.ex.ConfigurationException e) {
            throw new ConfigurationException(e);
        }
    }

    private Configuration buildFromYAML(
            URL url
    ) {
        try (InputStream stream = url.openStream()) {
            YAMLConfiguration yaml = configure(new YAMLConfiguration());
            yaml.read(stream);
            return new ApacheConfiguration2(this, Map.of(), yaml);
        } catch (IOException | org.apache.commons.configuration2.ex.ConfigurationException e) {
            throw new ConfigurationException(e);
        }
    }

    private Configuration buildFromPropertiesFile(
            URL url
    ) {
        try (InputStream stream = url.openStream(); Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            PropertiesConfiguration properties = configure(new PropertiesConfiguration());
            properties.read(reader);
            return new ApacheConfiguration2(this, properties);
        } catch (IOException | org.apache.commons.configuration2.ex.ConfigurationException e) {
            throw new ConfigurationException(e);
        }
    }

    private Configuration buildFromXML(
            URL url
    ) {
        try {
            var configurations = new Configurations();
            XMLConfiguration xml = configure(configurations.xml(url));
            return new ApacheConfiguration2(this, xml);
        } catch (org.apache.commons.configuration2.ex.ConfigurationException e) {
            throw new ConfigurationException(e);
        }
    }

    /**
     * Applies the Commons Configuration customization required by this module.
     *
     * <p>When a multi-value separator is enabled, both the configuration instance and its
     * conversion handler must share the same {@link PatchedListDelimiterHandler}. This is a
     * temporary workaround related to Apache Commons Configuration issue {@code CONFIGURATION-857},
     * and should remain in place only until the upstream behavior is clarified and fixed.</p>
     *
     * @param configuration configuration instance to initialize
     * @param <T>           concrete configuration type
     * @return the initialized configuration
     */
    private <T extends AbstractConfiguration> T configure(
            T configuration
    ) {
        DefaultConversionHandler conversionHandler = new DefaultConversionHandler();
        if (hasMultiValueSeparator()) {
            PatchedListDelimiterHandler listDelimiterHandler = new PatchedListDelimiterHandler(multiValueSeparator());
            configuration.setListDelimiterHandler(listDelimiterHandler);
            conversionHandler.setListDelimiterHandler(listDelimiterHandler);
        }
        configuration.setConversionHandler(conversionHandler);
        return configuration;
    }

    BaseConfiguration newBaseConfiguration() {
        return configure(new BaseConfiguration());
    }

    private URL adaptURI(
            URI uri,
            ClassLoader classLoader
    ) throws MalformedURLException {
        if ("classpath".equals(uri.getScheme())) {
            String resource = uri.getPath().replaceFirst("^/+", "");
            ClassLoader loader = classLoader != null ? classLoader : getClass().getClassLoader();
            URL resolved = loader.getResource(resource);
            if (resolved == null) {
                throw new ConfigurationException("Resource not found in classpath: " + resource);
            }
            return resolved;
        }
        return uri.toURL();
    }

}
