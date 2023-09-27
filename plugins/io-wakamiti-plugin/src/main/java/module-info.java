import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.files.FilesStepConfigurator;
import es.iti.wakamiti.files.FilesStepContributor;

module es.iti.wakamiti.io {

    exports es.iti.wakamiti.files;

    requires es.iti.wakamiti.api;
    requires junit;
    requires org.apache.commons.io;
    requires org.assertj.core;

    provides ConfigContributor with FilesStepConfigurator;
    provides StepContributor with FilesStepContributor;


}