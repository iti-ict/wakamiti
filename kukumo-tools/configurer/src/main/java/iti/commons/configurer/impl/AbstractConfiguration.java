/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.commons.configurer.impl;


import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import iti.commons.configurer.Configuration;
import iti.commons.configurer.ConfigurationBuilder;
import iti.commons.configurer.Configurator;


public abstract class AbstractConfiguration implements Configuration {

    protected final ConfigurationBuilder builder;


    protected AbstractConfiguration(ConfigurationBuilder builder) {
        this.builder = builder;
    }


    @Override
    public Configuration append(Configuration otherConfiguration) {
        return builder.compose(this, otherConfiguration);
    }


    @Override
    public Configuration appendFromAnnotation(Class<?> configuredClass) {
        return builder.compose(this, builder.buildFromAnnotation(configuredClass));
    }


    @Override
    public Configuration appendFromAnnotation(Configurator annotation) {
        return builder.compose(this, builder.buildFromAnnotation(annotation));
    }


    @Override
    public Configuration appendFromClasspathResource(String resourcePath) {
        return builder.compose(this, builder.buildFromClasspathResource(resourcePath));
    }


    @Override
    public Configuration appendFromClasspathResource(String resourcePath, ClassLoader classLoader) {
        return builder.compose(this, builder.buildFromClasspathResource(resourcePath, classLoader));
    }


    @Override
    public Configuration appendFromClasspathResourceOrURI(String path) {
        return builder.compose(this, builder.buildFromClasspathResourceOrURI(path));
    }


    @Override
    public Configuration appendFromEnvironment() {
        return builder.compose(this, builder.buildFromEnvironment());
    }


    @Override
    public Configuration appendFromEnvironment(boolean includeSystemProperties) {
        return builder.compose(this, builder.buildFromEnvironment(includeSystemProperties));
    }


    @Override
    public Configuration appendFromMap(Map<String, ?> propertyMap) {
        return builder.compose(this, builder.buildFromMap(propertyMap));
    }


    @Override
    public Configuration appendFromPath(Path path) {
        return builder.compose(this, builder.buildFromPath(path));
    }


    @Override
    public Configuration appendFromProperties(Properties properties) {
        return builder.compose(this, builder.buildFromProperties(properties));
    }


    @Override
    public Configuration appendFromURI(URI uri) {
        return builder.compose(this, builder.buildFromURI(uri));
    }


    @Override
    public Configuration appendFromURL(URL url) {
        return builder.compose(this, builder.buildFromURL(url));
    }


    @Override
    public Configuration appendProperty(String property, String value) {
        Map<String, String> singlePropertyMap = new HashMap<>();
        singlePropertyMap.put(property, value);
        return builder.compose(this, builder.buildFromMap(singlePropertyMap));
    }
}
