import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.email.EmailConfigContributor;
import es.iti.wakamiti.email.EmailStepContributor;

module es.iti.wakamiti.plugin.email {

    exports es.iti.wakamiti.email;

    requires org.slf4j;
    requires es.iti.wakamiti.api;
    requires java.mail;
    requires awaitility;

    provides ConfigContributor with EmailConfigContributor;
    provides StepContributor with EmailStepContributor;
}