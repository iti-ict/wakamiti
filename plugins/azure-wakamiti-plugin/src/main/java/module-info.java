import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.extensions.Reporter;
import es.iti.wakamiti.azure.AzureConfigContributor;
import es.iti.wakamiti.azure.AzureReporter;

module es.iti.wakamiti.azure {

    exports es.iti.wakamiti.azure;

    requires org.slf4j;
    requires imconfig;
    requires iti.commons.jext;
    requires es.iti.wakamiti.api;
    requires java.net.http;
    requires json.path;

    provides ConfigContributor with AzureConfigContributor;
    provides Reporter with AzureReporter;

}