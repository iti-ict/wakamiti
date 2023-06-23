import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.mail.MailConfigContributor;

module es.iti.wakamiti.mail {
    requires java.mail;
    requires activation;
    requires java.naming;
    requires java.xml;
    requires es.iti.wakamiti.api;
    requires junit;
    requires java.security.jgss;
    requires imconfig;
    requires org.junit.jupiter.api;

    opens es.iti.wakamiti.mail to javafx.fxml;
    exports es.iti.wakamiti.mail;

    uses ConfigContributor;
    uses StepContributor;

    provides ConfigContributor with MailConfigContributor;

}