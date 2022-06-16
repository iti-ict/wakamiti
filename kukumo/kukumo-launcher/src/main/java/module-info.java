module kukumo.launcher {

    exports iti.kukumo.launcher;

    requires transitive imconfig;
    requires kukumo.core;
    requires junit;
    requires maven.fetcher;
    requires java.instrument;
    requires org.slf4j;
    requires slf4jansi;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires net.harawata.appdirs;
    requires commons.cli;
    requires kukumo.api;

}