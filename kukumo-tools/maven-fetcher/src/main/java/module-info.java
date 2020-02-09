module iti.commons.maven.fetcher {

    requires aether.api;
    requires aether.connector.basic;
    requires aether.impl;
    requires aether.spi;
    requires aether.transport.file;
    requires aether.transport.http;
    requires aether.util;
    requires transitive org.slf4j;
    requires transitive java.instrument;
    requires maven.aether.provider.java9;

    exports iti.commons.maven.fetcher;

}