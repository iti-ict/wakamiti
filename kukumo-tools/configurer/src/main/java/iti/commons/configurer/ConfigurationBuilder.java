/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.commons.configurer;


import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;


/** <p> * A configuration builder is responsible of creating new instances of * {@link Configuration} via multiple alternative sources such as URL, URI, * classpath resources, maps, and properties objects. * </p> * <p> * When building a configuration from a external resource, the builder would * autodetect the resource type (usually looking at its file extension) and * treat the content properly, accepting multiple formats as JSON, YAML, XML, * and .properties files. * </p> */
public interface ConfigurationBuilder {

    static ConfigurationBuilder instance() {
        return ServiceLoader.load(ConfigurationBuilder.class).iterator().next();
    }


    /**
     * Create a new configuration composed of two other configurations. When the same
     * property is present in two or more configurations, the value from the
     * delta configuration will prevail (except when it has an empty value)
     */
    Configuration merge(Configuration base, Configuration delta);


    /**
     * Create a new empty configuration
     */
    Configuration empty();


    /**
     * Create a new configuration from a class annotated with
     * {@link AnnotatedConfiguration}
     *
     * @param configuredClass Class annotated with {@link AnnotatedConfiguration}
     * @throws ConfigurationException if the configuration was not loaded
     */
    Configuration buildFromAnnotation(Class<?> configuredClass);


    /**
     * Create a new configuration from a {@link AnnotatedConfiguration} annotation
     *
     * @param annotation
     * @throws ConfigurationException if the configuration was not loaded
     */
    Configuration buildFromAnnotation(AnnotatedConfiguration annotation);


    /**
     * Create a new configuration from the OS environment properties
     */
    Configuration buildFromEnvironment();


    /**
     * Create a new configuration from the {@link System} properties
     */
    Configuration buildFromSystem();


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



    /**
     * Create a new configuration from directly passed strings, using each two entries as a pair of
     * <tt>key,value</tt>.
     * @throws IllegalArgumentException if the number of strings is not even
     */
    default Configuration buildFromPairs(String... pairs) {
        if (pairs.length % 2 == 1) {
            throw new IllegalArgumentException("Number of arguments must be even");
        }
        Map<String,String> map = new LinkedHashMap<>();
        for (int i=0;i<pairs.length;i+=2) {
            map.put(pairs[i],pairs[i+1]);
        }
        return buildFromMap(map);
    }

}
