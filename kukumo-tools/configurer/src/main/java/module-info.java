module iti.commons.configurer {

    exports iti.commons.configurer;

    requires org.apache.commons.configuration2;
    requires commons.beanutils;
    requires org.yaml.snakeyaml;
    requires org.slf4j;
    requires org.apache.commons.lang3;

    uses iti.commons.configurer.ConfigurationBuilder;

    provides iti.commons.configurer.ConfigurationBuilder
            with iti.commons.configurer.internal.ApacheConfiguration2Builder;

}