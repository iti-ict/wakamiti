module es.iti.wakamiti.plugin.email.test {
    requires es.iti.wakamiti.plugin.email;
    requires junit;
    requires org.slf4j;
    requires es.iti.wakamiti.api;
    requires es.iti.wakamiti.core;
    requires greenmail;
    requires awaitility;
    requires java.mail;
    exports es.iti.wakamiti.email.test to junit, es.iti.wakamiti.core;
}