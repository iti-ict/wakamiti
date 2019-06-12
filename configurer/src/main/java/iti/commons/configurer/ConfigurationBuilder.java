package iti.commons.configurer;

import org.apache.commons.configuration2.*;
import org.apache.commons.configuration2.builder.fluent.Configurations;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;



public class ConfigurationBuilder {


    private final Configurations factory = new Configurations();


    /**
     * Create a new configuration composed of other configurations
     */
    public Configuration compose(Configuration... configurations) {
        if (configurations == null || configurations.length == 0) {
            throw new IllegalArgumentException("No configurations to compose");
        }
        Configuration configuration = configurations[0];
        for (int i=1; i<configurations.length; i++) {
            configuration = configuration.append(configurations[i]);
        }
        return configuration;
    }



    /**
     * Create a new empty configuration
     */
    public Configuration empty() {
        return new Configuration(new BaseConfiguration());
    }


    /**
     * Create a new configuration from a class annotated with {@link Configurator}
     * @param configuredClass Class annotated with {@link Configurator}
     * @throws ConfigurationException if the configuration was not loaded
     */
    public Configuration buildFromAnnotation(Class<?> configuredClass) throws ConfigurationException {
        final Configurator configurator = configuredClass.getAnnotation(Configurator.class);
        if (configurator == null) {
            throw new ConfigurationException(configuredClass+" is not annotated with @Configurator");
        }
        return buildFromAnnotation(configurator);
    }



    /**
     * Create a new configuration from a {@link Configurator} annotation
     * @param annotation
     * @throws ConfigurationException if the configuration was not loaded
     */
    public Configuration buildFromAnnotation(Configurator annotation) throws ConfigurationException {
        final BaseConfiguration configuration = new BaseConfiguration();
        for (final Property property : annotation.properties()) {
            final String[] value = property.value();
            if (value.length == 1) {
                configuration.addProperty(property.key(), value[0]);
            } else {
                configuration.addProperty(property.key(), value);
            }
        }
        if (annotation.path() != null && !annotation.path().isEmpty()) {
            return new Configuration(configuration).append(buildFromPath(annotation.path()));
        } else {
            return new Configuration(configuration);
        }
        
    }


    /**
     * Create a new configuration from the environment properties
     * @param includeSystemProperties When the value is <tt>true</tt>, the configuration will include
     * also every system variable.
     */
    public Configuration buildFromEnvironment(boolean includeSystemProperties) {
        final CompositeConfiguration configuration = new CompositeConfiguration();
        if (includeSystemProperties ) {
            configuration.addConfiguration(new SystemConfiguration());
        }
        configuration.addConfiguration(new EnvironmentConfiguration());
        return new Configuration(configuration);
    }



    /**
     * Create a new configuration from the environment properties
     */
    public Configuration buildFromEnvironment() {
        return buildFromEnvironment(false);
    }




    public Configuration buildFromPath(String path) throws ConfigurationException {
        if (path.startsWith("classpath:")) {
            return buildFromResource(path);
        } else {
            return buildFromURI(path);
        }
    }


    public Configuration buildFromProperties(Properties properties) {
        final BaseConfiguration configuration = new BaseConfiguration();
        for (final Entry<Object, Object> property : properties.entrySet()) {
            configuration.addProperty(property.getKey().toString(), property.getValue());
        }
        return new Configuration(configuration);
    }


    public Configuration buildFromMap(Map<String,?> properties) {
        final BaseConfiguration configuration = new BaseConfiguration();
        for (final Entry<String, ?> property : properties.entrySet()) {
            configuration.addProperty(property.getKey(), property.getValue());
        }
        return new Configuration(configuration);
    }

    
    
    public Configuration buildFromClasspathResource(String resourcePath, ClassLoader classLoader) 
    throws ConfigurationException {
        try {
	    	Configuration conf = empty();
	        List<Configuration> urlConfs = buildFromURLEnum(
	            classLoader.getResources(resourcePath),
	            resourcePath
	        );
	        for (Configuration urlConf : urlConfs) {
	            conf = conf.append(urlConf);
	        }
	        return conf;
        } catch (IOException e) {
        	throw new ConfigurationException(e);
        }
    }
    
    
    
    public Configuration buildFromClasspathResource(String resourcePath) throws ConfigurationException {
        return buildFromClasspathResource(resourcePath,getClass().getClassLoader());
    }

    

    public Configuration buildFromURI(URI uri) throws ConfigurationException {
        try {
            return buildFromURL(uri.toURL());
        } catch (final MalformedURLException e) {
            throw new ConfigurationException(e);
        }
    }


    public Configuration buildFromURL(URL url) throws ConfigurationException {
        Configuration configuration;
        if (url.getFile().endsWith(".properties")) {
            configuration = buildFromPropertiesFile(url);
        } else if (url.getFile().endsWith(".json")) {
            configuration = buildFromJSON(url);
        } else if (url.getFile().endsWith(".xml")) {
            configuration = buildFromXML(url);
        } else if (url.getFile().endsWith(".yaml")) {
            configuration = buildFromYAML(url);
        } else {
            throw new ConfigurationException("Cannot determine resource type of "+ url);
        }
        return configuration;
    }






    private List<Configuration> buildFromURLEnum(Enumeration<URL> urls, String resourcePath)
            throws ConfigurationException {
        final List<Configuration> configurations = new ArrayList<>();
        if (!urls.hasMoreElements()) {
            throw new ConfigurationException("Cannot find resource "+resourcePath);
        } else {
            while (urls.hasMoreElements()) {
                final URL url = urls.nextElement();
                configurations.add(buildFromURL(url));
            }
        }
        return configurations;
    }

    private Configuration buildFromJSON(URL url) throws ConfigurationException {
        try (InputStream stream = url.openStream()) {
            JSONConfiguration json = new JSONConfiguration();
            json.read(stream);
            return new Configuration(json);
        } catch (IOException | org.apache.commons.configuration2.ex.ConfigurationException e) {
            throw new ConfigurationException(e);
        }
    }



    private Configuration buildFromYAML(URL url) throws ConfigurationException {
        try (InputStream stream = url.openStream()) {
            YAMLConfiguration yaml = new YAMLConfiguration();
            yaml.read(stream);
            return new Configuration(yaml);
        } catch (IOException | org.apache.commons.configuration2.ex.ConfigurationException e) {
            throw new ConfigurationException(e);
        }
    }



    private Configuration buildFromPropertiesFile(URL url) throws ConfigurationException {
        try {
            return new Configuration(factory.properties(url));
        } catch (org.apache.commons.configuration2.ex.ConfigurationException e) {
            throw new ConfigurationException(e);
        }
    }



    private Configuration buildFromXML(URL url) throws ConfigurationException {
        try {
            return new Configuration(factory.xml(url));
        } catch (org.apache.commons.configuration2.ex.ConfigurationException e) {
            throw new ConfigurationException(e);
        }
    }




   



}
