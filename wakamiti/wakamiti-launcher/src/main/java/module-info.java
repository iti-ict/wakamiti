module wakamiti.launcher {

    exports iti.wakamiti.launcher;

    requires transitive imconfig;
    requires wakamiti.core;
    requires junit;
    requires maven.fetcher;
    requires java.instrument;
    requires org.slf4j;
    requires slf4jansi;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires net.harawata.appdirs;
    requires commons.cli;
    requires wakamiti.api;

}