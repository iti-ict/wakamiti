module es.iti.wakamiti.amqp.test {
    requires es.iti.wakamiti.amqp;
    requires junit;
    requires org.slf4j;
    requires es.iti.wakamiti.api;
    requires es.iti.wakamiti.core;
    requires testcontainers;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.github.dockerjava.api;
    requires org.jetbrains.annotations;
    exports es.iti.wakamiti.amqp.test to junit, es.iti.wakamiti.core;
}