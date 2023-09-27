module es.iti.wakamiti.report.html.test {
    requires es.iti.wakamiti.report.html;
    requires junit;
    requires org.slf4j;
    requires es.iti.wakamiti.api;
    requires es.iti.wakamiti.core;
    requires org.custommonkey.xmlunit;
    requires java.xml;
    requires org.xmlunit.assertj;
    requires net.bytebuddy;
    requires org.assertj.core;
    exports es.iti.wakamiti.report.html.test to junit, es.iti.wakamiti.core;
}