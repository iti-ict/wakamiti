module es.iti.wakamiti.launcher {

    exports es.iti.wakamiti.launcher;

    requires es.iti.wakamiti.core;
    requires maven.fetcher;
    requires java.instrument;
    requires org.slf4j;
    requires slf4jansi;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires net.harawata.appdirs;
    requires commons.cli;
    requires es.iti.wakamiti.api;

}