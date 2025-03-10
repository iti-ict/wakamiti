open module es.iti.wakamiti.plugins.xray {

    exports es.iti.wakamiti.xray;
    exports es.iti.wakamiti.xray.model;
    exports es.iti.wakamiti.xray.api;

    requires org.slf4j;
    requires iti.commons.jext;
    requires es.iti.wakamiti.api;
    requires java.net.http;
    requires json.path;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.text;
    requires org.apache.commons.lang3;
    requires org.apache.httpcomponents.httpcore;

}