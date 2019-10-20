/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.commons.configurer;


import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;


/** <p> * A configuration builder is responsible of creating new instances of * {@link Configuration} via multiple alternative sources such as URL, URI, * classpath resources, maps, and properties objects. * </p> * <p> * When building a configuration from a external resource, the builder would * autodetect the resource type (usually looking at its file extension) and * treat the content properly, accepting multiple formats as JSON, YAML, XML, * and .properties files. * </p> */
public interface ConfigurationBuilder {

    static ConfigurationBuilder instance() {
        return ServiceLoader.load(ConfigurationBuilder.class).iterator().next();
    }


    /**
     * Create a new configuration composed of other configurations. When the same
     * property is present in two or more configurations, the value from the
     * outer configuration will prevail.
     */
    Configuration compose(Configuration... configurations);


    /**
     * Create a new empty configuration
     */
    Configuration empty();


    /**
     * Create a new configuration from a class annotated with
     * {@link Configurator}
     *
     * @param configuredClass Class annotated with {@link Configurator}
     * @throws ConfigurationException if the configuration was not loaded
     */
    Configuration buildFromAnnotation(Class<?> configuredClass);


    /**
     * Create a new configuration from a {@link Configurator} annotation
     *
     * @param annotation
     * @throws ConfigurationException if the configuration was not loaded
     */
    Configuration buildFromAnnotation(Configurator annotation);


    /**
     * Create a new configuration from the environment properties
     */
    Configuration buildFromEnvironment();


    /**
     * Create a new configuration from the environment properties
     *
     * @param includeSystemProperties When the value is <tt>true</tt>, the
     *                                configuration will include also every
     *                                system variable.
     */
    Configuration buildFromEnvironment(boolean includeSystemProperties);


    /**
     * Create a new configuration from the resource of the specified path
     *
     * @throws ConfigurationException if the configuration was not loaded
     */
    Configuration buildFromPath(Path path);


    /**
     * Create a new configuration from the specified URL
     *
     * @throws ConfigurationException if the configuration was not loaded
     */
    Configuration buildFromURL(URL url);


    /**
     * Create a new configuration from the specified URI
     *
     * @throws ConfigurationException if the configuration was not loaded
     */
    Configuration buildFromURI(URI uri);


    /**
     * Create a new configuration from either a Java classpath resource (if the
     * path starts with the pseudo-schema <code>classpath:</code>) or a regular
     * URI resource
     */
    Configuration buildFromClasspathResourceOrURI(String path);


    /**
     * Create a new configuration from a {@link Properties} object
     */
    Configuration buildFromProperties(Properties properties);


    /**
     * Create a new configuration from a {@link Map} object
     */
    Configuration buildFromMap(Map<String, ?> propertyMap);


    /**
     * Create a new configuration from one or several Java class resources
     * resolved using the {@link ClassLoader#getResources(String)} method of the
     * thread default class loader
     */
    Configuration buildFromClasspathResource(String resourcePath);


    /**
     * Create a new configuration from one or several Java class resources
     * resolved using the {@link ClassLoader#getResources(String)} method of the
     * specified class loader
     */
    Configuration buildFromClasspathResource(String resourcePath, ClassLoader classLoader);

}
