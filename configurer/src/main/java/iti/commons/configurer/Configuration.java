package iti.commons.configurer;

import org.apache.commons.configuration2.BaseConfiguration;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Configuration {

    private static final ConfigurationBuilder builder = new ConfigurationBuilder();
    
    private final org.apache.commons.configuration2.Configuration conf;
    private final String prefix;
    
    
    protected Configuration(org.apache.commons.configuration2.Configuration conf) {
        this.conf = conf;
        this.prefix = "";
    }
    
    protected Configuration(org.apache.commons.configuration2.Configuration conf, String prefix) {
        this.conf = conf;
        this.prefix = prefix;
    }
    
    public Configuration append (Configuration otherConf) {
        BaseConfiguration newConf = new BaseConfiguration();
        newConf.append(conf);
        newConf.copy(otherConf.conf);
        return new Configuration(newConf);
    }
     
    
    public Configuration appendFromAnnotation(Class<?> configuredClass) throws ConfigurationException {
        return append(builder.buildFromAnnotation(configuredClass));
    }

    public Configuration appendFromAnnotation(Configurator configurer) throws ConfigurationException {
        return append(builder.buildFromAnnotation(configurer));    
    }
      
    public Configuration appendFromPath(String path) throws ConfigurationException {
        return append(builder.buildFromPath(path));
    }
    
    public Configuration appendFromPath(String path, String innerKeyPrefix) throws ConfigurationException {
        return append(builder.buildFromPath(path).inner(innerKeyPrefix));
    }

    public Configuration appendFromProperties(Properties properties) {
        return append(builder.buildFromProperties(properties));
    }

    public Configuration appendFromMap(Map<String,?> properties) {
        return append(builder.buildFromMap(properties));
    }

    public Configuration appendPrefix(String keyPrefix) {
        BaseConfiguration innerConf = new BaseConfiguration();
        conf.getKeys().forEachRemaining(key -> 
            innerConf.addProperty(keyPrefix+"."+key, conf.getProperty(key))
        );
        return new Configuration(innerConf,keyPrefix);
    }
    
    
    public Configuration filter(String keyPrefix) {
        BaseConfiguration innerConf = new BaseConfiguration();
        conf.getKeys(keyPrefix).forEachRemaining(key -> {
            if (key.startsWith(keyPrefix)) {
                innerConf.addProperty(key, conf.getProperty(key));
            }
        });
        return new Configuration(innerConf,keyPrefix);
    }
    
    public Configuration inner(String keyPrefix) {
        return new Configuration(conf.subset(keyPrefix));
    }
    
    
    public String prefix() {
        return prefix;
    }
    
    public boolean isEmpty() {
        return conf.isEmpty();
    }

    public int size() {
        return conf.size();
    }

    
    public boolean hasProperty(String key) {
        return conf.containsKey(key);
    }

       
    
    public Iterator<String> keys() {
        return conf.getKeys();
    }


    public Optional<Boolean> getBoolean(String key) {
        return Optional.ofNullable(conf.getBoolean(key,null));
    }
    
    public Optional<Double> getDouble(String key) {
        return Optional.ofNullable(conf.getDouble(key,null));
    }

    public Optional<Float> getFloat(String key) {
        return Optional.ofNullable(conf.getFloat(key,null));
    }
    
    public Optional<Integer> getInteger(String key) {
        return Optional.ofNullable(conf.getInteger(key,null));
    }
       
    public Optional<Long> getLong(String key) {
        return Optional.ofNullable(conf.getLong(key,null));
    }
    
    public Optional<BigDecimal> getBigDecimal(String key) {
        return Optional.ofNullable(conf.getString(key,null)).map(BigDecimal::new);
    }

    public Optional<BigInteger> getBigInteger(String key) {
        return Optional.ofNullable(conf.getBigInteger(key,null));
    }

    public Optional<String> getString(String key) {
        return Optional.ofNullable(conf.getString(key,null));
    }

    
    public List<Boolean> getBooleanList(String key) {
        return Optional.ofNullable(conf.getList(Boolean.class, key)).orElse(Collections.emptyList());
    }
    
    public List<Double> getDoubleList(String key) {
        return Optional.ofNullable(conf.getList(Double.class, key)).orElse(Collections.emptyList());
    }

    public List<Float> getFloatList(String key) {
        return Optional.ofNullable(conf.getList(Float.class, key)).orElse(Collections.emptyList());
    }
    
    public List<Integer> getIntegerList(String key) {
        return Optional.ofNullable(conf.getList(Integer.class, key)).orElse(Collections.emptyList());
    }
       
    public List<Long> getLongList(String key) {
        return Optional.ofNullable(conf.getList(Long.class, key)).orElse(Collections.emptyList());
    }
    
    public List<BigDecimal> getBigDecimalList(String key) {
        return Stream.of(conf.getStringArray(key)).map(BigDecimal::new).collect(Collectors.toList());
    }

    public List<BigInteger> getBigIntegerList(String key) {
        return Optional.ofNullable(conf.getList(BigInteger.class, key)).orElse(Collections.emptyList());
    }

    public List<String> getStringList(String key) {
        return Optional.ofNullable(conf.getList(String.class, key)).orElse(Collections.emptyList());
    }

    
    public String toString() {
        StringBuilder string = new StringBuilder("configuration:\n---------------\n");
        if (!prefix.isEmpty()) {
            string.append("[ inherited from ").append(prefix).append(" ]\n");
        }
        conf.getKeys().forEachRemaining( key -> {
            final String[] values = conf.getStringArray(key);
            string
            .append(key)
            .append(" : ")
            .append(values.length > 1 ? Arrays.toString(values): values[0])
            .append("\n");
        });
        return string.append("---------------").toString();
    }

    
    
    public Properties asProperties() {
        Properties properties = new Properties();
        conf.getKeys().forEachRemaining(key -> properties.put(key, conf.getString(key)));
        return properties;
    }


    public Map<String,String> asMap() {
        Map<String,String> map = new HashMap<>();
        conf.getKeys().forEachRemaining(key -> map.put(key, conf.getString(key)));
        return map;
    }
}
