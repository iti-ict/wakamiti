/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.imconfig.internal;


import es.iti.wakamiti.api.imconfig.Configuration;
import es.iti.wakamiti.api.imconfig.*;
import org.apache.commons.configuration2.AbstractConfiguration;
import org.apache.commons.configuration2.*;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.convert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;


public class ApacheConfiguration2Factory implements ConfigurationFactory {

    private final ConversionHandler conversionHandler = new ApacheConfiguration2ConversionHandler();
    private final PropertyDefinitionParser parser = new PropertyDefinitionParser();

    private char separator = 0;


    @Override
    public ConfigurationFactory multiValueSeparator(char separator) {
        if (separator == 0) {
            throw new IllegalArgumentException("Invalid separator symbol: "+separator);
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
    public Configuration merge(Configuration base, Configuration delta) {

        AbstractConfiguration result = new BaseConfiguration();
        for (String property : delta.keys()) {
            var existing = base.getList(property,String.class);
            var added = delta.getList(property,String.class);
            if (existing.isEmpty() && added.isEmpty()) {
                result.setProperty(property,"");
            } else if (!added.isEmpty()) {
                added.forEach(value -> result.addProperty(property,value));
            }
        }
        for (String property : base.keys()) {
            if (result.containsKey(property)) {
                continue;
            }
            base.getList(property,String.class).forEach(value -> result.addProperty(property,value));
        }
        Map<String, PropertyDefinition> definitions = new HashMap<>(base.getDefinitions());
        definitions.putAll(delta.getDefinitions());

        return new ApacheConfiguration2(this, definitions, result);
    }





    @Override
    public Configuration empty() {
        return new ApacheConfiguration2(this, new BaseConfiguration());
    }


    @Override
    public Configuration fromAnnotation(Class<?> configuredClass) {
        return Optional.ofNullable(configuredClass.getAnnotation(AnnotatedConfiguration.class))
            .map(this::fromAnnotation)
            .orElseThrow(
                () -> new ConfigurationException(
                    configuredClass + " is not annotated with @Configurator"
                )
            );
    }


    @Override
    public Configuration fromAnnotation(AnnotatedConfiguration annotation) {
        BaseConfiguration configuration = configure(new BaseConfiguration());
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


    @Override
    public Configuration fromEnvironment() {
        return new ApacheConfiguration2(this, new EnvironmentConfiguration());
    }


    @Override
    public Configuration fromSystem() {
        return new ApacheConfiguration2(this, new SystemConfiguration());
    }


    @Override
    public Configuration fromPath(Path path) {
        return fromURI(path.toUri());
    }



    @SuppressWarnings("CollectionDeclaredAsConcreteClass")
    @Override
    public Configuration fromProperties(Properties properties) {
        final BaseConfiguration configuration = configure(new BaseConfiguration());
        for (final Entry<Object, Object> property : properties.entrySet()) {
            configuration.addProperty(property.getKey().toString(), property.getValue());
        }
        return new ApacheConfiguration2(this, configuration);
    }


    @Override
    public Configuration fromMap(Map<String, ?> properties) {
        final BaseConfiguration configuration = configure(new BaseConfiguration());
        for (final Entry<String, ?> property : properties.entrySet()) {
            configuration.addProperty(property.getKey(), property.getValue());
        }
        return new ApacheConfiguration2(this, configuration);
    }



    private Configuration fromMap(Map<String, ?> properties, Collection<PropertyDefinition> definitions) {
        final BaseConfiguration configuration = configure(new BaseConfiguration());
        for (final Entry<String, ?> property : properties.entrySet()) {
            configuration.addProperty(property.getKey(), property.getValue());
        }
        var definitionMap = definitions.stream()
            .collect(Collectors.toMap(PropertyDefinition::property,x->x));
        return new ApacheConfiguration2(this, definitionMap, configuration);
    }



    @Override
    public Configuration fromURI(URI uri) {
        return fromURI(uri,null);
    }


    @Override
    public Configuration fromResource(String resource, ClassLoader classLoader) {
        return fromURI(URI.create("classpath:///"+resource),classLoader);
    }



    private Configuration fromURI(URI uri, ClassLoader classLoader) {
       try {
           return buildFromURL(adaptURI(uri, classLoader));
        } catch (MalformedURLException e) {
           throw new ConfigurationException(e);
        }
    }



    @Override
    public Configuration accordingDefinitions(Collection<PropertyDefinition> definitions) {
        Map<String,String> defaultValues = definitions
            .stream()
            .filter(definition -> definition.defaultValue().isPresent())
            .collect(Collectors.toMap(
                PropertyDefinition::property,
                definition->definition.defaultValue().orElseThrow()
            ));
        return fromMap(defaultValues,definitions);
    }



    @Override
    public Configuration accordingDefinitionsFromURI(URI uri) {
        return accordingDefinitionsFromURI(uri,null);
    }


    private Configuration accordingDefinitionsFromURI(URI uri, ClassLoader classLoader) {
        try (var inputStream = adaptURI(uri, classLoader).openStream()) {
            return accordingDefinitions(parser.read(inputStream));
        } catch (IOException e) {
            throw new ConfigurationException(e);
        }
    }



    @Override
    public Configuration accordingDefinitionsFromPath(Path path) {
        return accordingDefinitionsFromURI(path.toUri());
    }


    @Override
    public Configuration accordingDefinitionsFromResource(String resource,ClassLoader classLoader) {
        return accordingDefinitionsFromURI(URI.create("classpath:///"+resource),classLoader);
    }





    private Configuration buildFromURL(URL url) {
        Configuration configuration;
        String file = url.getFile();
        if (file.endsWith(".properties")) {
            configuration = buildFromPropertiesFile(url);
        } else if (file.endsWith(".json")) {
            configuration = buildFromJSON(url);
        } else if (file.endsWith(".xml")) {
            configuration = buildFromXML(url);
        } else if (file.endsWith(".yaml")) {
            configuration = buildFromYAML(url);
        } else {
            throw new ConfigurationException("Cannot determine resource type of " + url);
        }
        return configuration;
    }



    private Configuration buildFromJSON(URL url) {
        try (InputStream stream = url.openStream()) {
            JSONConfiguration json = configure(new JSONConfiguration());
            json.read(stream);
            return new ApacheConfiguration2(this, Map.of(), json);
        } catch (IOException | org.apache.commons.configuration2.ex.ConfigurationException e) {
            throw new ConfigurationException(e);
        }
    }


    private Configuration buildFromYAML(URL url) {
        try (InputStream stream = url.openStream()) {
            YAMLConfiguration yaml = configure(new YAMLConfiguration());
            yaml.read(stream);
            return new ApacheConfiguration2(this, Map.of(), yaml);
        } catch (IOException | org.apache.commons.configuration2.ex.ConfigurationException e) {
            throw new ConfigurationException(e);
        }
    }


    private Configuration buildFromPropertiesFile(URL url) {
        try (InputStream stream = url.openStream(); Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            PropertiesConfiguration properties = configure(new PropertiesConfiguration());
            properties.read(reader);
            return new ApacheConfiguration2(this, properties);
        } catch (IOException | org.apache.commons.configuration2.ex.ConfigurationException e) {
            throw new ConfigurationException(e);
        }
    }


    private Configuration buildFromXML(URL url) {
        try {
            var configurations = new Configurations();
            XMLConfiguration xml = configure(configurations.xml(url));
            return new ApacheConfiguration2(this, xml);
        } catch (org.apache.commons.configuration2.ex.ConfigurationException e) {
            throw new ConfigurationException(e);
        }
    }




    private <T extends AbstractConfiguration> T configure(T configuration) {
        configuration.setConversionHandler(conversionHandler);
        if (hasMultiValueSeparator()) {
            configuration.setListDelimiterHandler(new DefaultListDelimiterHandler(multiValueSeparator()));
        }
        return configuration;
    }



    private URL adaptURI(URI uri, ClassLoader classLoader) throws MalformedURLException {
        if ("classpath".equals(uri.getScheme())) {
            return new URL("classpath",null, -1, uri.getPath(), new ClasspathURLStreamHandler(classLoader));
        } else {
            return uri.toURL();
        }
    }

}
