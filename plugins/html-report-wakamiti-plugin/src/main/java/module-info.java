import es.iti.wakamiti.api.extensions.ConfigContributor;
import es.iti.wakamiti.api.extensions.Reporter;
import es.iti.wakamiti.report.html.HtmlReportGenerator;
import es.iti.wakamiti.report.html.HtmlReportGeneratorConfig;

module es.iti.wakamiti.report.html {

    exports es.iti.wakamiti.report.html;

    requires es.iti.wakamiti.api;
    requires freemarker;
    requires fast.and.simple.minify;

    provides ConfigContributor with HtmlReportGeneratorConfig;
    provides Reporter with HtmlReportGenerator;


}