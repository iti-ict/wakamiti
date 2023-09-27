import es.iti.wakamiti.amqp.AmqpConfigContributor;
import es.iti.wakamiti.amqp.AmqpStepContributor;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.extensions.StepContributor;

module es.iti.wakamiti.amqp {

    exports es.iti.wakamiti.amqp;

    requires junit;
    requires awaitility;
    requires com.rabbitmq.client;
    requires imconfig;
    requires iti.commons.jext;
    requires es.iti.wakamiti.api;

    provides ConfigContributor with AmqpConfigContributor;
    provides StepContributor with AmqpStepContributor;



}