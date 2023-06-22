package es.iti.wakamiti.azure;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.extensions.Reporter;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;


@Extension(name = "")
public class AzureReporter implements Reporter {


    private String host;
    private String credentials;
    private String planApiVersion;
    private String runApiVersion;
    private String organization;
    private String project;



    public void setHost(String s) {
    }

    public void setCredentials(String s) {
    }

    public void setPlanApiVersion(String s) {
    }

    public void setRunApiVersion(String s) {
    }

    public void setOrganization(String s) {
    }

    public void setProject(String project) {
        this.setProject();
    }


    @Override
    public void report(PlanNodeSnapshot result) {

    }


}
