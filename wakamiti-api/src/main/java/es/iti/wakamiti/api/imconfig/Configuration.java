/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.imconfig;


import com.fasterxml.jackson.core.type.TypeReference;

import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;


/**
 * The main interface used to get configuration values and create derived configurations.
 */
public interface Configuration  {


    /**
     * Return a new configuration factory. Equivalent to invoke {@link ConfigurationFactory#instance()}
     */
    static ConfigurationFactory factory() {
        return ConfigurationFactory.instance();
    }


    /**
     * Creates a new configuration resulting of adding the given prefix to every
     * key
     */
    Configuration withPrefix(String keyPrefix);


    /**
     * Creates a new configuration resulting of filtering the properties starting
     * with the given prefix
     */
    Configuration filtered(String keyPrefix);


    /**
     * Creates a new configuration resulting of filtering the properties starting
     * with the given prefix, and the removing it
     */
    Configuration inner(String keyPrefix);


    /**
     * @return <code>true</code> if there are no properties in this configuration
     */
    boolean isEmpty();


    /**
     * @return <code>true</code> if there is a valued property with the given key
     */
    boolean hasProperty(String key);


    /**
     * @return An iterable object over all the keys of the configuration,
     *  even for those which have no value
     */
    Iterable<String> keys();


    /** @return An iterator over all the keys of the configuration,
     *  even for those which have no value */
    Iterator<String> keyIterator();


    /** @return A stream from all the keys of the configuration,
     *  even for those which have no value */
    Stream<String> keyStream();


    /**
     * @return An optional value of the specified type, empty if the key does not
     *         exist
     */
    <T> Optional<T> get(String key, Class<T> type);

    <T> Optional<T> get(String key, TypeReference<T> type);

    /**
     * @return A list with values of the specified type, empty if the key does
     *         not exist
     */
    <T> List<T> getList(String key, Class<T> type);


    /**
     * @return A set with values of the specified type, empty if the key does not
     *         exist
     */
    <T> Set<T> getSet(String key, Class<T> type);


    /**
     * @return A stream with values of the specified type, empty if the key does
     *         not exist
     */
    <T> Stream<T> getStream(String key, Class<T> type);


    /** @return The configuration represented as a {@link Properties} object */
    Properties asProperties();


    /** @return The configuration represented as a {@link Map} object */
    Map<String, String> asMap();



    /** Perform an action for each pair <code>[key,value]</code> */
    void forEach(BiConsumer<String, String> consumer);


    /**
     * Create a new configuration resulting in the merge the current configuration
     * with the configuration from a class annotated with {@link AnnotatedConfiguration}
     *
     * @param configuredClass Class annotated with {@link AnnotatedConfiguration}
     * @throws ConfigurationException if the configuration was not loaded
     */
    Configuration appendFromAnnotation(Class<?> configuredClass);


    /**
     * Create a new configuration resulting in the merge the current configuration
     * with the configuration from a {@link AnnotatedConfiguration} annotation
     *
     * @throws ConfigurationException if the configuration was not loaded
     */
    Configuration appendFromAnnotation(AnnotatedConfiguration annotation);


    /**
     * Create a new configuration resulting in the merge the current configuration
     * with the configuration from the environment properties
     */
    Configuration appendFromEnvironment();


    /**
     * Create a new configuration resulting in the merge the current configuration
     * with the configuration from the {@link System} properties
     */
    Configuration appendFromSystem();


    /**
     * Create a new configuration resulting in the merge the current configuration
     * with the configuration from the resource of the specified path
     *
     * @throws ConfigurationException if the configuration was not loaded
     */
    Configuration appendFromPath(Path path);



    /**
     * Create a new configuration resulting in the merge the current configuration
     * with the configuration from the specified URI
     *
     * @throws ConfigurationException if the configuration was not loaded
     */
    Configuration appendFromURI(URI uri);


    /**
     * Create a new configuration resulting in the merge the current configuration
     * with the configuration from a {@link Properties} object
     */
    Configuration appendFromProperties(Properties properties);


    /**
     * Create a new configuration resulting in the merge the current configuration
     * with the configuration from a {@link Map} object
     */
    Configuration appendFromMap(Map<String, ?> propertyMap);


    /**
     * Create a new configuration resulting in the merge the current configuration
     * with the configuration from one or several Java class resources resolved
     * using the {@link ClassLoader#getResources(String)} method of the specified
     * class loader
     */
    Configuration appendFromResource(String resourcePath, ClassLoader classLoader);


    /**
     * Create a new configuration resulting in the merge the current configuration
     * with another one
     */
    Configuration append(Configuration otherConfiguration);


    /**
     * Create a new configuration resulting of adding or replacing a property to
     * the current configuration. Since this method creates a new object each
     * time, it should not be used as the primary way to create large
     * configurations but rather to tweak existing ones.
     */
    Configuration appendProperty(String key, String value);




    /**
     * Create a new configuration resulting of merge the current configuration with
     * the configuration from a set of directly passed strings, using each two entries as a pair of
     * <tt>key,value</tt>.
     * @throws IllegalArgumentException if the number of strings is not even
     */
    default Configuration appendFromPairs(String... pairs) {
        return append(ConfigurationFactory.instance().fromPairs(pairs));
    }


    /**
     * @return whether there is a definition for the given property
     */
    boolean hasDefinition(String key);


    /**
     * Check whether the current value for the given property is valid according its definition.
     * If the property is multi-valued, it may return a different validation for each value
     * @param key The property key
     * @return The validation messages, or empty if the value is valid
     */
    List<String> validations(String key);



    /**
     * Retrieve the property definition for a given property
     */
    Optional<PropertyDefinition> getDefinition(String key);


    /**
     * Retrieve every property definition defined for this configuration
     * @return An unmodifiable map in the form of <property,definition>
     */
    Map<String,PropertyDefinition> getDefinitions();


    /**
     * Return a map in form of <tt>property=[validation_message1,...]</tt>
     * with the validation error messages for all invalid properties values
     * according the current definition.
     * <p>
 *     Configurations without definition will always return an empty map.
     * </p>
     */
    Map<String,List<String>> validations();


    /**
     * Ensures that all property values are valid according the current definition.
     * Otherwise, it will raise a {@link ConfigurationException} with a list of every
     * invalid value.
     * <p>
 *     Configurations without definition will never raise an exception using this method
     * </p>
     * @throws ConfigurationException if one or more properties have invalid values
     * @return The same instance, for convenience
     */
    Configuration validate() throws ConfigurationException;


    /**
     * Create a new configuration according the given property definitions.
     * <p>
     * Defined properties will be set to their default value if it exists and no current value is
     * set.
     * @see PropertyDefinition
     */
    Configuration accordingDefinitions(Collection<PropertyDefinition> definitions);


    /**
     * Create a new configuration according the property definitions from the given path.
     * <p>
     * Defined properties will be set to their default value if it exists and no current value is
     * set.
     * @see PropertyDefinition
     */
    Configuration accordingDefinitionsFromPath(Path path);


    /**
     * Create a new defined configuration according the property definitions from the given URI.
     * <p>
     * Defined properties will be set to their default value if it exists and no current value is
     * set.
     * @see PropertyDefinition
     */
    Configuration accordingDefinitionsFromURI(URI uri);


    /**
     * Create a new defined configuration according the property definitions from the given
     * classpath resource and class loader.
     * <p>
     * Defined properties will be set to their default value if it exists and no current value is
     * set.
     * @see PropertyDefinition
     */
    Configuration accordingDefinitionsFromResource(String resource, ClassLoader classLoader);


    /**
     * Get a textual representation of all defined properties
     */
    String getDefinitionsToString();
}
