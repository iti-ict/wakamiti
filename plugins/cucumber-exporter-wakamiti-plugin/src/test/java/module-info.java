module es.iti.wakamiti.report.cucumber.test {
    requires es.iti.wakamiti.report.cucumber;
    requires junit;
    requires org.slf4j;
    requires es.iti.wakamiti.api;
    requires es.iti.wakamiti.core;
    requires org.assertj.core;
    exports es.iti.wakamiti.plugins.cucumber.test to junit, es.iti.wakamiti.core;
}