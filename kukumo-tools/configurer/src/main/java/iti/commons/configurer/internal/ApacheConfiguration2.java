/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.commons.configurer.internal;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import iti.commons.configurer.Configuration;
import iti.commons.configurer.ConfigurationBuilder;
import org.apache.commons.configuration2.BaseConfiguration;


public class ApacheConfiguration2 extends AbstractConfiguration {

    final org.apache.commons.configuration2.Configuration conf;


    protected ApacheConfiguration2(
                    ConfigurationBuilder builder,
                    org.apache.commons.configuration2.Configuration conf
    ) {
        super(builder);
        this.conf = conf;
    }


    @Override
    public Configuration withPrefix(String keyPrefix) {
        BaseConfiguration innerConf = new BaseConfiguration();
        conf.getKeys().forEachRemaining(
            key -> innerConf.addProperty(keyPrefix + "." + key, conf.getProperty(key))
        );
        return new ApacheConfiguration2(builder, innerConf);
    }


    @Override
    public Configuration filtered(String keyPrefix) {
        BaseConfiguration innerConf = new BaseConfiguration();
        conf.getKeys(keyPrefix).forEachRemaining(key -> {
            if (key.startsWith(keyPrefix)) {
                innerConf.addProperty(key, conf.getProperty(key));
            }
        });
        return new ApacheConfiguration2(builder, innerConf);
    }


    @Override
    public Configuration inner(String keyPrefix) {
        if (keyPrefix == null || keyPrefix.isEmpty()) {
            return this;
        }
        return new ApacheConfiguration2(builder, conf.subset(keyPrefix));
    }


    @Override
    public boolean isEmpty() {
        return conf.isEmpty();
    }


    @Override
    public boolean hasProperty(String key) {
        return conf.containsKey(key);
    }


    @Override
    public Iterable<String> keys() {
        return keyList();
    }


    @Override
    public Iterator<String> keyIterator() {
        return conf.getKeys();
    }


    @Override
    public Stream<String> keyStream() {
        return keyList().stream();
    }


    private List<String> keyList() {
        List<String> keys = new ArrayList<>();
        conf.getKeys().forEachRemaining(keys::add);
        return keys;
    }


    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        String raw = conf.getString(key);
        boolean empty = (raw == null || "".equals(raw));
        return Optional.ofNullable(empty ? null : conf.get(type, key));
    }


    @Override
    public <T> List<T> getList(String key, Class<T> type) {
        return Optional.ofNullable(conf.getList(type, key)).orElse(Collections.emptyList());
    }


    @Override
    public <T> Set<T> getSet(String key, Class<T> type) {
        return new HashSet<>(getList(key, type));
    }


    @Override
    public <T> Stream<T> getStream(String key, Class<T> type) {
        return getList(key, type).stream();
    }


    @Override
    public Properties asProperties() {
        Properties properties = new Properties();
        conf.getKeys().forEachRemaining(key -> properties.put(key, conf.getString(key)));
        return properties;
    }


    @Override
    public Map<String, String> asMap() {
        Map<String, String> map = new HashMap<>();
        conf.getKeys().forEachRemaining(key -> map.put(key, conf.getString(key)));
        return map;
    }


    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("configuration:\n---------------\n");
        conf.getKeys().forEachRemaining(key -> {
            final String[] values = conf.getStringArray(key);
            String value = "<undefined>";
            if (values.length == 1) {
                value = values[0];
            } else if (values.length > 1) {
                value = Arrays.toString(values);
            }
            string
                .append(key)
                .append(" : ")
                .append(value)
                .append("\n");
        });
        return string.append("---------------").toString();
    }


    @Override
    public void forEach(BiConsumer<String, String> consumer) {
        conf.getKeys().forEachRemaining(key -> consumer.accept(key, conf.get(String.class, key)));
    }

}
