open module es.iti.wakamiti {

    exports es.iti.wakamiti;

    requires es.iti.wakamiti.api;
    requires org.slf4j;
    requires unirest.java;
    requires org.json;

    provides es.iti.wakamiti.api.extensions.Reporter with es.iti.wakamiti.xray.XrayReporter;
    provides es.iti.wakamiti.api.extensions.ConfigContributor with es.iti.wakamiti.XRayConfigContributor;

}