/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.imconfig;


import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;


/**
 * Read-only view of runtime configuration values plus fluent operations that
 * create derived configurations.
 * <p>
 * Implementations are expected to return new instances for transformation
 * methods (for example, {@code append*}, {@link #filtered(String)},
 * {@link #inner(String)}), leaving the original instance unchanged unless a
 * specific implementation documents otherwise.
 * </p>
 */
public interface Configuration {

    /**
     * Returns the default configuration factory.
     *
     * @return shared factory used to create configuration instances
     */
    static ConfigurationFactory factory() {
        return ConfigurationFactory.instance();
    }

    /**
     * Creates a derived configuration that prepends a key prefix when querying
     * values.
     *
     * @param keyPrefix prefix applied to lookup keys
     * @return prefixed configuration view
     */
    Configuration withPrefix(
            String keyPrefix
    );

    /**
     * Creates a derived configuration containing only keys that start with the
     * supplied prefix.
     *
     * @param keyPrefix key prefix used as inclusion filter
     * @return filtered configuration view
     */
    Configuration filtered(
            String keyPrefix
    );

    /**
     * Creates an inner configuration for a prefix namespace.
     * <p>
     * Only keys starting with {@code keyPrefix} are retained and the prefix is
     * removed from keys in the returned view.
     * </p>
     *
     * @param keyPrefix key prefix that defines the namespace
     * @return namespace-scoped configuration view
     */
    Configuration inner(
            String keyPrefix
    );

    /**
     * @return <code>true</code> if there are no properties in this configuration
     */
    boolean isEmpty();

    /**
     * Checks whether a key exists and has an effective value.
     *
     * @param key property key
     * @return {@code true} when the key resolves to a value
     */
    boolean hasProperty(
            String key
    );

    /**
     * @return An iterable object over all the keys of the configuration,
     * even for those which have no value
     */
    Iterable<String> keys();

    /**
     * @return An iterator over all the keys of the configuration,
     * even for those which have no value
     */
    Iterator<String> keyIterator();

    /**
     * @return A stream from all the keys of the configuration,
     * even for those which have no value
     */
    Stream<String> keyStream();

    /**
     * Retrieves and converts a scalar configuration value.
     *
     * @param key  the property key
     * @param type the target Java class
     * @param <T>  the requested value type
     * @return the converted value, or an empty optional when the key has no
     * value
     */
    <T> Optional<T> get(
            String key,
            Class<T> type
    );

    /**
     * Retrieves a configuration value using a Jackson type token.
     * <p>
     * This overload preserves nested generic information, making it suitable
     * for values such as {@code List<MyType>} that cannot be represented by a
     * raw {@link Class}.
     * </p>
     *
     * @param key  the property key
     * @param type the complete target type, including generic parameters
     * @param <T>  the requested value type
     * @return the converted value, or an empty optional when the key has no
     * value
     */
    <T> Optional<T> get(
            String key,
            TypeReference<T> type
    );

    /**
     * Retrieves a multi-valued property as a typed list.
     *
     * @param key  property key
     * @param type element type
     * @param <T>  element type parameter
     * @return converted values, or an empty list when the key has no value
     */
    <T> List<T> getList(
            String key,
            Class<T> type
    );

    /**
     * Retrieves a multi-valued property as a typed set.
     *
     * @param key  property key
     * @param type element type
     * @param <T>  element type parameter
     * @return converted values, or an empty set when the key has no value
     */
    <T> Set<T> getSet(
            String key,
            Class<T> type
    );

    /**
     * Retrieves a multi-valued property as a typed stream.
     *
     * @param key  property key
     * @param type element type
     * @param <T>  element type parameter
     * @return converted values, or an empty stream when the key has no value
     */
    <T> Stream<T> getStream(
            String key,
            Class<T> type
    );

    /**
     * @return The configuration represented as a {@link Properties} object
     */
    Properties asProperties();

    /**
     * @return The configuration represented as a {@link Map} object
     */
    Map<String, String> asMap();

    /**
     * Executes an action for each key/value pair with an effective value.
     *
     * @param consumer action invoked per resolved property
     */
    void forEach(
            BiConsumer<String, String> consumer
    );

    /**
     * Appends configuration declared by an {@link AnnotatedConfiguration}
     * present on the supplied class.
     *
     * @param configuredClass class annotated with
     *                        {@link AnnotatedConfiguration}
     * @return merged configuration
     * @throws ConfigurationException when annotation-based configuration cannot
     *                                be loaded
     */
    Configuration appendFromAnnotation(
            Class<?> configuredClass
    );

    /**
     * Appends configuration declared by a concrete
     * {@link AnnotatedConfiguration} annotation instance.
     *
     * @param annotation annotation declaring configuration sources
     * @return merged configuration
     * @throws ConfigurationException when annotation-based configuration cannot
     *                                be loaded
     */
    Configuration appendFromAnnotation(
            AnnotatedConfiguration annotation
    );

    /**
     * Create a new configuration resulting in the merge the current configuration
     * with the configuration from the environment properties
      *
      * @return the resulting value
     */
    Configuration appendFromEnvironment();

    /**
     * Create a new configuration resulting in the merge the current configuration
     * with the configuration from the {@link System} properties
      *
      * @return the resulting value
     */
    Configuration appendFromSystem();

    /**
     * Create a new configuration resulting in the merge the current configuration
     * with the configuration from the resource of the specified path
     *
     * @throws ConfigurationException if the configuration was not loaded
      *
      * @param path the path value
      * @return the resulting value
     */
    Configuration appendFromPath(
            Path path
    );

    /**
     * Create a new configuration resulting in the merge the current configuration
     * with the configuration from the specified URI
     *
     * @throws ConfigurationException if the configuration was not loaded
      *
      * @param uri the uri value
      * @return the resulting value
     */
    Configuration appendFromURI(
            URI uri
    );

    /**
     * Create a new configuration resulting in the merge the current configuration
     * with the configuration from a {@link Properties} object
      *
      * @param properties the properties value
      * @return the resulting value
     */
    Configuration appendFromProperties(
            Properties properties
    );

    /**
     * Create a new configuration resulting in the merge the current configuration
     * with the configuration from a {@link Map} object
      *
      * @param propertyMap the property map value
      * @return the resulting value
     */
    Configuration appendFromMap(
            Map<String, ?> propertyMap
    );

    /**
     * Appends properties loaded from classpath resources matched by path.
     * <p>
     * Resource resolution uses {@link ClassLoader#getResources(String)} and
     * merges every discovered resource in encounter order.
     * </p>
     *
     * @param resourcePath classpath resource location
     * @param classLoader  class loader used for resource resolution
     * @return merged configuration
     */
    Configuration appendFromResource(
            String resourcePath,
            ClassLoader classLoader
    );

    /**
     * Create a new configuration resulting in the merge the current configuration
     * with another one
      *
      * @param otherConfiguration the other configuration value
      * @return the resulting value
     */
    Configuration append(
            Configuration otherConfiguration
    );

    /**
     * Creates a derived configuration overriding one property value.
     * <p>
     * This method is intended for small adjustments over an existing
     * configuration rather than bulk construction.
     * </p>
     *
     * @param key   property key to add or replace
     * @param value new property value
     * @return resulting configuration
     */
    Configuration appendProperty(
            String key,
            String value
    );

    /**
     * Create a new configuration resulting of merge the current configuration with
     * the configuration from a set of directly passed strings, using each two entries as a pair of
     * <tt>key,value</tt>.
     *
     * @throws IllegalArgumentException if the number of strings is not even
      *
      * @param pairs the pairs value
      * @return the resulting value
     */
    default Configuration appendFromPairs(
            String... pairs
    ) {
        return append(ConfigurationFactory.instance().fromPairs(pairs));
    }

    /**
     * Checks whether a definition exists for the specified property key.
     *
     * @param key property key
     * @return {@code true} when a definition is available
     */
    boolean hasDefinition(
            String key
    );

    /**
     * Check whether the current value for the given property is valid according its definition.
     * If the property is multi-valued, it may return a different validation for each value
     *
     * @param key The property key
     * @return The validation messages, or empty if the value is valid
     */
    List<String> validations(
            String key
    );

    /**
     * Retrieves the property definition for the supplied key.
     *
     * @param key property key
     * @return definition for the key, or empty when undefined
     */
    Optional<PropertyDefinition> getDefinition(
            String key
    );

    /**
     * Retrieve every property definition defined for this configuration
     *
     * @return An unmodifiable map in the form of <property,definition>
     */
    Map<String, PropertyDefinition> getDefinitions();

    /**
     * Returns validation errors grouped by property key.
     * <p>
     * Keys are included only when at least one configured value is invalid
     * according to current definitions. Configurations without definitions
     * return an empty map.
     * </p>
     *
     * @return map in the form {@code property -> [validation1, ...]}
     */
    Map<String, List<String>> validations();

    /**
     * Ensures that all property values are valid according the current definition.
     * Otherwise, it will raise a {@link ConfigurationException} with a list of every
     * invalid value.
     * <p>
     * Configurations without definition will never raise an exception using this method
     * </p>
     *
     * @return The same instance, for convenience
     * @throws ConfigurationException if one or more properties have invalid values
     */
    Configuration validate() throws ConfigurationException;

    /**
     * Create a new configuration according the given property definitions.
     * <p>
     * Defined properties will be set to their default value if it exists and no current value is
     * set.
     *
     * @see PropertyDefinition
      *
      * @param definitions the definitions value
      * @return the resulting value
     */
    Configuration accordingDefinitions(
            Collection<PropertyDefinition> definitions
    );

    /**
     * Create a new configuration according the property definitions from the given path.
     * <p>
     * Defined properties will be set to their default value if it exists and no current value is
     * set.
     *
     * @see PropertyDefinition
      *
      * @param path the path value
      * @return the resulting value
     */
    Configuration accordingDefinitionsFromPath(
            Path path
    );

    /**
     * Create a new defined configuration according the property definitions from the given URI.
     * <p>
     * Defined properties will be set to their default value if it exists and no current value is
     * set.
     *
     * @see PropertyDefinition
      *
      * @param uri the uri value
      * @return the resulting value
     */
    Configuration accordingDefinitionsFromURI(
            URI uri
    );

    /**
     * Create a new defined configuration according the property definitions from the given
     * classpath resource and class loader.
     * <p>
     * Defined properties will be set to their default value if it exists and no current value is
     * set.
     *
     * @see PropertyDefinition
      *
     * @param classLoader the class loader value
     * @param resource the parameter value
     * @return the resulting configuration
     */
    Configuration accordingDefinitionsFromResource(
            String resource,
            ClassLoader classLoader
    );

    /**
     * Returns a human-readable representation of all current property
     * definitions.
     *
     * @return textual dump of property definitions
     */
    String getDefinitionsToString();

}
