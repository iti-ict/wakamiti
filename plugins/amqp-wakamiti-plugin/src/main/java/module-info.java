

import es.iti.wakamiti.amqp.AmqpConfigContributor;
import es.iti.wakamiti.amqp.AmqpStepContributor;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.extensions.StepContributor;

module es.iti.wakamiti.amqp {

    requires awaitility;
    requires java.naming;
    requires iti.commons.jext;
    requires es.iti.wakamiti.api;
    requires org.apache.commons.lang3;
    requires jakarta.messaging;
    requires qpid.jms.client;
    requires com.rabbitmq.client;

    provides StepContributor with AmqpStepContributor;
    provides ConfigContributor with AmqpConfigContributor;

}
