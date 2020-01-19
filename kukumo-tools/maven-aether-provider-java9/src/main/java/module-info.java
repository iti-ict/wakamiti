module maven.aether.provider.java9 {

    exports org.apache.maven.artifact;
    exports org.apache.maven.model;
    exports org.apache.maven.repository;
    exports org.apache.maven.building;

    requires aether.api;
    requires aether.connector.basic;
    requires aether.impl;
    requires aether.spi;
    requires aether.transport.file;
    requires aether.transport.http;
    requires aether.util;
    requires javax.inject;
    requires plexus.component.annotations;
    requires org.apache.commons.lang3;
    requires plexus.utils;
    requires plexus.interpolation;
    requires com.google.guice;
    requires com.google.common;



}