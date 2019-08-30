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
import java.util.stream.Stream;

public interface Configuration {


    /** Creates a new configuration resulting of adding the given prefix to every key*/
    Configuration withPrefix(String keyPrefix);

    /**
     * Creates a new configuration resulting of filtering the properties starting with the given
     * prefix
     */
    Configuration filtered(String keyPrefix);

    /**
     * Creates a new configuration resulting of filtering the properties starting with the given
     * prefix, and the removing it
     */
    Configuration inner(String keyPrefix);

    /** @return <code>true</code> if there is no properties in this configuration */
    boolean isEmpty();

    /** @return <code>true</code> if there is a property with the given key */
    boolean hasProperty(String key);

    /** @return An iterable object over all the keys of the configuration */
    Iterable<String> keys();

    /** @return An iterator over all the keys of the configuration */
    Iterator<String> keyIterator();

    /** @return A stream from all the keys of the configuration */
    Stream<String> keyStream();

    /** @return An optional value of the specified type, empty if the key does not exist  */
    <T> Optional<T> get(String key, Class<T> type);

    /** @return A list with values of the specified type, empty if the key does not exist  */
    <T> List<T> getList(String key, Class<T> type);

    /** @return A set with values of the specified type, empty if the key does not exist  */
    <T> Set<T> getSet(String key, Class<T> type);

    /** @return A stream with values of the specified type, empty if the key does not exist */
    <T> Stream<T> getStream(String key, Class<T> type);

    /** @return The configuration represented as a {@link Properties} object  */
    Properties asProperties();

    /** @return The configuration represented as a {@link Map} object */
    Map<String,String> asMap();

    /** @return A textual representation of the configuration */
    String toString();

    /** Perform an action for each pair <code>[key,value]</code> */
    void forEach(BiConsumer<String,String> consumer);


    /**
     * Create a new configuration resulting the merge the current configuration with 
     * the configuration from a class annotated with {@link Configurator}
     * @param configuredClass Class annotated with {@link Configurator}
     * @throws ConfigurationException if the configuration was not loaded
     */
    Configuration appendFromAnnotation(Class<?> configuredClass);


    /**
     * Create a new configuration resulting the merge the current configuration with 
     * the configuration from a {@link Configurator} annotation
     * @param annotation
     * @throws ConfigurationException if the configuration was not loaded
     */
    Configuration appendFromAnnotation(Configurator annotation);


    /**
     * Create a new configuration resulting the merge the current configuration with 
     * the configuration from the environment properties
     */
    Configuration appendFromEnvironment();


    /**
     * Create a new configuration resulting the merge the current configuration with 
     * the configuration from the environment properties
     * @param includeSystemProperties When the value is <tt>true</tt>, the configuration will 
     * include also every system variable.
     */
    Configuration appendFromEnvironment(boolean includeSystemProperties);


    /**
     * Create a new configuration resulting the merge the current configuration with 
     * the configuration from the resource of the specified path
     * @throws ConfigurationException if the configuration was not loaded
     */
    Configuration appendFromPath(Path path);


    /**
     * Create a new configuration resulting the merge the current configuration with 
     * the configuration from the specified URL
     * @throws ConfigurationException if the configuration was not loaded 
     */
    Configuration appendFromURL(URL url);


    /**
     * Create a new configuration resulting the merge the current configuration with 
     * the configuration from the specified URI
     * @throws ConfigurationException if the configuration was not loaded 
     */
    Configuration appendFromURI(URI uri);


    /**
     * Create a new configuration resulting the merge the current configuration with 
     * the configuration from either a Java classpath resource (if the path starts with the
     * pseudo-schema <code>classpath:</code>) or a regular URI resource
     */
    Configuration appendFromClasspathResourceOrURI(String path);


    /**
     * Create a new configuration resulting the merge the current configuration with 
     * the configuration from a {@link Properties} object
     */
    Configuration appendFromProperties(Properties properties);


    /**
     * Create a new configuration resulting the merge the current configuration with
     * the configuration from a {@link Map} object
     */
    Configuration appendFromMap(Map<String,?> propertyMap);


    /**
     * Create a new configuration resulting the merge the current configuration with
     * the configuration from one or several Java class resources resolved using the
     * {@link ClassLoader#getResources(String)} method of the thread default class loader
     */
    Configuration appendFromClasspathResource(String resourcePath);


    /**
     * Create a new configuration resulting the merge the current configuration with
     * the configuration from one or several Java class resources resolved using the
     * {@link ClassLoader#getResources(String)} method of the specified class loader
     */
    Configuration appendFromClasspathResource(String resourcePath, ClassLoader classLoader);
    
    
    /** Create a new configuration resulting the merge the current configuration with another one */
    Configuration append (Configuration otherConfiguration);


    /**
     *  Create a new configuration resulting of adding or replacing a property
     *  to the current configuration.
     *  Since this method creates a new object each time, it should not be used as the primary
     *  way to create large configurations but rather to tweak existing ones.
     */
    Configuration appendProperty(String localRepository, String toString);

}
