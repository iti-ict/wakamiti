package es.iti.wakamiti.xray;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.extensions.Reporter;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.api.util.WakamitiLogger;
import org.slf4j.Logger;
import java.time.LocalDateTime;
import java.util.*;

@Extension(
        provider = "com.wakamiti",
        name = "xray-reporter",
        version = "1.0",
        priority = 10
)
public class XrayReporter implements Reporter {

    private static final Logger LOGGER = WakamitiLogger.forClass(XrayReporter.class);
    public static final String XRAY_AUTH_URL = "xrayAuth";
    public static final String CLIENT_ID = "xrayClient";
    public static final String CLIENT_SECRET = "xraySecret";
    public static final String JIRA_URL = "jiraUrl";
    public static final String API_TOKEN = "xrayApiToken";
    public static final String MAIL = "xrayMail";
    public static final String PROJECT_KEY = "xrayProjectKey";
    private static final Logger LOGGER = WakamitiLogger.forClass(XrayReporter.class);
    private boolean disabled;
    private String jiraUrl;
    private String email;
    private String apiToken;

    public void setDisabled(boolean disabled) {this.disabled = disabled;}

    public void setJiraUrl(String jiraUrl) {this.jiraUrl = jiraUrl;}

    public void setEmail(String email) {this.email = email;}

    public void setApiToken(String apiToken) {this.apiToken = apiToken;}

    @Override
    public void report(PlanNodeSnapshot result) {
        if (disabled) {
            return;
        }

        XrayApi api = new XrayApi(jiraUrl, email, apiToken, LOGGER);

    }
}
