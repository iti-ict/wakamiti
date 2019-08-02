package iti.commons.configurer;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public interface Configuration {

    /** Create a new configuration resulting the merge the current configuration with another one */
    Configuration mergedWith (Configuration otherConfiguration);
    
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

    /** @return An iteratable object over all the keys of the configuration */
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

    /** @return A stream with double values, empty if the key does not exist */
    DoubleStream getDoubleStream(String key);

    /** @return A stream with int values, empty if the key does not exist */
    IntStream getIntegerStream(String key);

    /** @return A stream with long values, empty if the key does not exist */
    LongStream getLongStream(String key);

    /** @return The configuration represented as a {@link Properties} object  */
    Properties asProperties();

    /** @return The configuration represented as a {@link Map} object */
    Map<String,String> asMap();

    /** Perform an action for each pair <code>[key,value]</code> */
    void forEach(BiConsumer<String,String> consumer);


}
