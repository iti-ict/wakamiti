/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.imconfig;


import java.net.URI;
import java.nio.file.Path;
import java.util.*;


/**
 * A configuration factory is responsible for creating new instances of
 * {@link Configuration} from a variety of alternative sources, such as URIs,
 * classpath resources, maps and properties objects. Every new configuration
 * object should be created using a factory.
 * <p>
 * When building a configuration from an external resource, the builder would
 * automatically detect the resource type (usually by looking at its file
 * extension) and handle the content properly, accepting multiple formats such
 * as JSON, YAML, XML, and .properties files.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public interface ConfigurationFactory {

    /**
     * Retrieves the default instance of the ConfigurationFactory.
     *
     * @return The default instance of ConfigurationFactory.
     * @throws ConfigurationException If an error occurs while retrieving the
     *                                default instance.
     */
    static ConfigurationFactory instance() {
        try {
            return ServiceLoader.load(ConfigurationFactory.class).stream()
                    .findFirst()
                    .orElseThrow()
                    .type()
                    .getConstructor()
                    .newInstance();
        } catch (ReflectiveOperationException e) {
            throw new ConfigurationException(e);
        }
    }

    /**
     * Specifies a symbol to be used to separate different values expressed in
     * one line.
     * For some types of configuration load this is not necessary because the
     * source format already has some form of multi-value (e.g. lists are YAML
     * files), but for other types it is mandatory (e.g. expressing multi-value
     * properties within a map).
     * <p>
     * By default, no separation is applied.
     *
     * @param separator The separator character, other than {@code 0}
     * @return The ConfigurationFactory instance with the specified multi-value
     * separator.
     */
    ConfigurationFactory multiValueSeparator(char separator);

    /**
     * Checks if a multi-value separator has been set.
     *
     * @return True if a multi-value separator has been set, otherwise false.
     */
    boolean hasMultiValueSeparator();

    /**
     * Get the symbol that will be used to separate different values expressed
     * in one line.
     *
     * @return The separator symbol, or {@code 0} if it is not determined.
     */
    char multiValueSeparator();

    /**
     * Creates a new configuration from two other configurations. If the same
     * property is present in two or more configurations, the value from the
     * delta configuration will prevail (unless it is an empty value).
     *
     * @param base  The base Configuration.
     * @param delta The Configuration to merge with the base.
     * @return The merged Configuration.
     */
    Configuration merge(Configuration base, Configuration delta);

    /**
     * Creates an empty Configuration instance.
     *
     * @return An empty Configuration instance.
     */
    Configuration empty();


    /**
     * Creates a new configuration instance based on a class annotated with
     * {@link AnnotatedConfiguration}
     *
     * @param configuredClass The class containing configuration annotations.
     * @return A Configuration instance based on annotations.
     */
    Configuration fromAnnotation(Class<?> configuredClass);

    /**
     * Creates a new configuration instance based on a
     * {@link AnnotatedConfiguration} annotation.
     *
     * @param annotation The AnnotatedConfiguration object.
     * @return A Configuration instance based on annotations.
     */
    Configuration fromAnnotation(AnnotatedConfiguration annotation);

    /**
     * Creates a new configuration instance based on environment variables.
     *
     * @return A Configuration instance based on environment variables.
     */
    Configuration fromEnvironment();

    /**
     * Creates a new configuration instance based on the {@link System} properties.
     *
     * @return A Configuration instance based on system properties.
     */
    Configuration fromSystem();

    /**
     * Creates a new configuration instance from the specified file path.
     *
     * @param path The file path.
     * @return A Configuration instance from the file path.
     */
    Configuration fromPath(Path path);

    /**
     * Creates a new configuration instance from the specified URI.
     *
     * @param uri The URI.
     * @return A Configuration instance from the URI.
     */
    Configuration fromURI(URI uri);

    /**
     * Creates a new configuration instance from the specified classpath
     * resource.
     *
     * @param resource    The resource name.
     * @param classLoader The class loader to use.
     * @return A Configuration instance from the resource.
     */
    Configuration fromResource(String resource, ClassLoader classLoader);

    /**
     * Creates a new configuration instance from the specified
     * {@link Properties} object.
     *
     * @param properties The Properties object.
     * @return A Configuration instance from the Properties object.
     */
    Configuration fromProperties(Properties properties);

    /**
     * Creates a new configuration instance from the specified {@link Map} object.
     *
     * @param propertyMap The Map of properties.
     * @return A Configuration instance from the Map of properties.
     */
    Configuration fromMap(Map<String, ?> propertyMap);

    /**
     * Creates a new configuration instance from key-value pairs.
     *
     * @param pairs The key-value pairs.
     * @return A Configuration instance from the key-value pairs.
     * @throws IllegalArgumentException If the number of arguments is not even.
     */
    default Configuration fromPairs(String... pairs) {
        if (pairs.length % 2 == 1) {
            throw new IllegalArgumentException("Number of arguments must be even");
        }
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put(pairs[i], pairs[i + 1]);
        }
        return fromMap(map);
    }

    /**
     * Creates a new empty configuration instance based on property definitions.
     * <p>
     * Defined properties will be set to their default value if one exists.
     *
     * @param definitions The collection of PropertyDefinition objects.
     * @return A Configuration instance based on property definitions.
     * @see PropertyDefinition
     */
    Configuration accordingDefinitions(Collection<PropertyDefinition> definitions);

    /**
     * Creates a new empty configuration instance based on property definitions
     * from a file path.
     *
     * @param path The file path containing property definitions.
     * @return A Configuration instance based on property definitions from the
     * file path.
     * @see PropertyDefinition
     */
    Configuration accordingDefinitionsFromPath(Path path);

    /**
     * Creates a new empty configuration instance based on property definitions
     * from a URI.
     * <p>
     * You can use the {@code classpath:} schema to reference classpath resources.
     * <p>
     * Defined properties will be set to their default value if one exists.
     *
     * @param uri The URI containing property definitions.
     * @return A Configuration instance based on property definitions from the URI.
     * @see PropertyDefinition
     */
    Configuration accordingDefinitionsFromURI(URI uri);

    /**
     * Creates a new empty configuration instance based on property definitions
     * from a classpath resource.
     * <p>
     * Defined properties will be set to their default value if one exists.
     *
     * @param resource    The resource containing property definitions.
     * @param classLoader The class loader to use.
     * @return A Configuration instance based on property definitions from the
     * resource.
     * @see PropertyDefinition
     */
    Configuration accordingDefinitionsFromResource(String resource, ClassLoader classLoader);

}
