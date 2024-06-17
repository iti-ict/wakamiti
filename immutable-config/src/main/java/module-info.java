
/**
 This module provides a simple interface in order to load and consume configurations,
 which are mainly a set of valued properties that can be parsed from a wide range of sources
 (such as JSON, YAML or .properties files, Map and Properties objects, or even plain pairs of
 values) to specific Java types.
 <p>
 The primary focus of the module is null-safety, immutability, and fluency.
 */
module imconfig {

    exports es.iti.commons.imconfig;

    requires org.apache.commons.configuration2;
    requires org.yaml.snakeyaml;
    requires org.slf4j;
    requires org.apache.commons.lang3;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    uses es.iti.commons.imconfig.ConfigurationFactory;

    provides es.iti.commons.imconfig.ConfigurationFactory with es.iti.commons.imconfig.internal.ApacheConfiguration2Factory;
}