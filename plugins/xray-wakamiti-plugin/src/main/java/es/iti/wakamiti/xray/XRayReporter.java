package es.iti.wakamiti.xray;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.extensions.Reporter;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.util.WakamitiLogger;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

@Extension(
        provider = "es.iti.wakamiti",
        name = "xray-reporter",
        version = "2.6",
        priority = 10
)
public class XRayReporter implements Reporter {
    private static final Logger LOGGER = WakamitiLogger.forClass(XRayReporter.class);

    private boolean disabled;
    private String credentialsClientId;
    private String credentialsClientSecret;
    private String host;
    private String project;
    private String xRayTag;

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public void setCredentialsClientId(String credentialsClientId) {
        this.credentialsClientId = credentialsClientId;
    }

    public void setCredentialsClientSecret(String credentialsClientSecret) {
        this.credentialsClientSecret = credentialsClientSecret;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public void setXRayTag(String xRayTag) {
        this.xRayTag = xRayTag;
    }

    @Override
    public void report(PlanNodeSnapshot result) {

        if (disabled) {
            return;
        }

        XRayApi api = new XRayApi(
                host,
                credentialsClientId,
                credentialsClientSecret,
                project,
                LOGGER
        );

        Map<Object, Map<Object, Object>> testCases = getOrCreateTestCases(result, new HashMap<>(), api);
        if (testCases.isEmpty()) {
            return;
        }


    }

    private Map<Object, Map<Object, Object>> getOrCreateTestCases(PlanNodeSnapshot node, Map<Object, Map<Object, Object>> result, XRayApi api) {

        if (node.getTags().contains(xRayTag)) {

            return null;
        }

        return result;
    }
}