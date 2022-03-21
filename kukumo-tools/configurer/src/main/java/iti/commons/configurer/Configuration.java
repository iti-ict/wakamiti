/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.commons.configurer;


import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;


public interface Configuration {



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
     * @return <code>true</code> if there is no properties in this configuration
     */
    boolean isEmpty();


    /** @return <code>true</code> if there is a valued property with the given key */
    boolean hasProperty(String key);


    /** @return An iterable object over all the keys of the configuration,
     *  even for those which have no value */
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


    default <T> Configuration ifPresent(String key, Class<T> type, Consumer<T> consumer) {
        get(key,type).ifPresent(consumer);
        return this;
    }


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


    /** @return A textual representation of the configuration */
    @Override
    String toString();


    /** Perform an action for each pair <code>[key,value]</code> */
    void forEach(BiConsumer<String, String> consumer);


    /**
     * Create a new configuration resulting the merge the current configuration
     * with the configuration from a class annotated with {@link AnnotatedConfiguration}
     *
     * @param configuredClass Class annotated with {@link AnnotatedConfiguration}
     * @throws ConfigurationException if the configuration was not loaded
     */
    Configuration appendFromAnnotation(Class<?> configuredClass);


    /**
     * Create a new configuration resulting the merge the current configuration
     * with the configuration from a {@link AnnotatedConfiguration} annotation
     *
     * @param annotation
     * @throws ConfigurationException if the configuration was not loaded
     */
    Configuration appendFromAnnotation(AnnotatedConfiguration annotation);


    /**
     * Create a new configuration resulting the merge the current configuration
     * with the configuration from the environment properties
     */
    Configuration appendFromEnvironment();


    /**
     * Create a new configuration resulting the merge the current configuration
     * with the configuration from the {@link System} properties
     */
    Configuration appendFromSystem();


    /**
     * Create a new configuration resulting the merge the current configuration
     * with the configuration from the resource of the specified path
     *
     * @throws ConfigurationException if the configuration was not loaded
     */
    Configuration appendFromPath(Path path);


    /**
     * Create a new configuration resulting the merge the current configuration
     * with the configuration from the specified URL
     *
     * @throws ConfigurationException if the configuration was not loaded
     */
    Configuration appendFromURL(URL url);


    /**
     * Create a new configuration resulting the merge the current configuration
     * with the configuration from the specified URI
     *
     * @throws ConfigurationException if the configuration was not loaded
     */
    Configuration appendFromURI(URI uri);


    /**
     * Create a new configuration resulting the merge the current configuration
     * with the configuration from either a Java classpath resource (if the path
     * starts with the pseudo-schema <code>classpath:</code>) or a regular URI
     * resource
     */
    Configuration appendFromClasspathResourceOrURI(String path);


    /**
     * Create a new configuration resulting the merge the current configuration
     * with the configuration from a {@link Properties} object
     */
    Configuration appendFromProperties(Properties properties);


    /**
     * Create a new configuration resulting the merge the current configuration
     * with the configuration from a {@link Map} object
     */
    Configuration appendFromMap(Map<String, ?> propertyMap);


    /**
     * Create a new configuration resulting the merge the current configuration
     * with the configuration from one or several Java class resources resolved
     * using the {@link ClassLoader#getResources(String)} method of the thread
     * static class loader
     */
    Configuration appendFromClasspathResource(String resourcePath);


    /**
     * Create a new configuration resulting the merge the current configuration
     * with the configuration from one or several Java class resources resolved
     * using the {@link ClassLoader#getResources(String)} method of the specified
     * class loader
     */
    Configuration appendFromClasspathResource(String resourcePath, ClassLoader classLoader);


    /**
     * Create a new configuration resulting the merge the current configuration
     * with another one
     */
    Configuration append(Configuration otherConfiguration);


    /**
     * Create a new configuration resulting of adding or replacing a property to
     * the current configuration. Since this method creates a new object each
     * time, it should not be used as the primary way to create large
     * configurations but rather to tweak existing ones.
     */
    Configuration appendProperty(String localRepository, String toString);




    /**
     * Create a new configuration resulting of merge the current configuration with
     * the configuration from a set of directly passed strings, using each two entries as a pair of
     * <tt>key,value</tt>.
     * @throws IllegalArgumentException if the number of strings is not even
     */
    default Configuration appendFromPairs(String... pairs) {
        return append(ConfigurationBuilder.instance().buildFromPairs(pairs));
    }




     // convenience static methods from ConfigurationBuilder



    /**
     * Create a new configuration composed of two other configurations. When the same
     * property is present in two or more configurations, the value from the
     * delta configuration will prevail (except when it has an empty value)
     */
    static Configuration merge(Configuration base, Configuration delta) {
        return ConfigurationBuilder.instance().merge(base,delta);
    }


    /**
     * Create a new empty configuration
     */
    static Configuration empty() {
        return ConfigurationBuilder.instance().empty();
    }


    /**
     * Create a new configuration from a class annotated with
     * {@link AnnotatedConfiguration}
     *
     * @param configuredClass Class annotated with {@link AnnotatedConfiguration}
     * @throws ConfigurationException if the configuration was not loaded
     */
    static Configuration fromAnnotation(Class<?> configuredClass) {
        return ConfigurationBuilder.instance().buildFromAnnotation(configuredClass);
    }


    /**
     * Create a new configuration from a {@link AnnotatedConfiguration} annotation
     *
     * @param annotation
     * @throws ConfigurationException if the configuration was not loaded
     */
    static Configuration fromAnnotation(AnnotatedConfiguration annotation) {
        return ConfigurationBuilder.instance().buildFromAnnotation(annotation);
    }


    /**
     * Create a new configuration from the OS environment properties
     */
    static Configuration fromEnvironment() {
        return ConfigurationBuilder.instance().buildFromEnvironment();
    }


    /**
     * Create a new configuration from the {@link System} properties
     */
    static Configuration fromSystem() {
        return ConfigurationBuilder.instance().buildFromSystem();
    }


    /**
     * Create a new configuration from the resource of the specified path
     *
     * @throws ConfigurationException if the configuration was not loaded
     */
    static Configuration fromPath(Path path) {
        return ConfigurationBuilder.instance().buildFromPath(path);
    }


    /**
     * Create a new configuration from the specified URL
     *
     * @throws ConfigurationException if the configuration was not loaded
     */
    static Configuration fromURL(URL url) {
        return ConfigurationBuilder.instance().buildFromURL(url);
    }


    /**
     * Create a new configuration from the specified URI
     *
     * @throws ConfigurationException if the configuration was not loaded
     */
    static Configuration fromURI(URI uri) {
        return ConfigurationBuilder.instance().buildFromURI(uri);
    }


    /**
     * Create a new configuration from either a Java classpath resource (if the
     * path starts with the pseudo-schema <code>classpath:</code>) or a regular
     * URI resource
     */
    static Configuration fromClasspathResourceOrURI(String path) {
        return ConfigurationBuilder.instance().buildFromClasspathResourceOrURI(path);
    }


    /**
     * Create a new configuration from a {@link Properties} object
     */
    static Configuration fromProperties(Properties properties) {
        return ConfigurationBuilder.instance().buildFromProperties(properties);
    }


    /**
     * Create a new configuration from a {@link Map} object
     */
    static Configuration fromMap(Map<String, ?> propertyMap) {
        return ConfigurationBuilder.instance().buildFromMap(propertyMap);
    }


    /**
     * Create a new configuration from one or several Java class resources
     * resolved using the {@link ClassLoader#getResources(String)} method of the
     * thread static class loader
     */
    static Configuration fromClasspathResource(String resourcePath) {
        return ConfigurationBuilder.instance().buildFromClasspathResource(resourcePath);
    }


    /**
     * Create a new configuration from one or several Java class resources
     * resolved using the {@link ClassLoader#getResources(String)} method of the
     * specified class loader
     */
    static Configuration fromClasspathResource(String resourcePath, ClassLoader classLoader) {
        return ConfigurationBuilder.instance().buildFromClasspathResource(resourcePath, classLoader);
    }


    /**
     * Create a new configuration from directly passed strings, using each two entries as a pair of
     * <tt>key,value</tt>.
     * @throws IllegalArgumentException if the number of strings is not even
     */
    static Configuration fromPairs(String... pairs) {
        return ConfigurationBuilder.instance().buildFromPairs(pairs);
    }
}