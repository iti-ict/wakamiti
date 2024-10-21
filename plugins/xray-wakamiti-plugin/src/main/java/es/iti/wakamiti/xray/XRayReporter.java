package es.iti.wakamiti.xray;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.extensions.Reporter;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;

@Extension(
        provider =  "es.iti.wakamiti",
        name = "xray-reporter",
        version = "2.6",
        priority = 10
)
public class XRayReporter implements Reporter {

    @Override
    public void report(PlanNodeSnapshot planNodeSnapshot) {

    }
}