package es.iti.wakamiti.azure;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.extensions.Reporter;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.util.WakamitiLogger;
import org.slf4j.Logger;


@Extension(
        provider =  "es.iti.wakamiti",
        name = "azure-reporter",
        version = "1.0",
        priority = 10
)
public class AzureReporter implements Reporter {

    private static final Logger LOGGER = WakamitiLogger.forClass(AzureReporter.class);

    private String host;
    private String credentialsUser;
    private String credentialsPassword;
    private String apiVersion;
    private String runApiVersion;
    private String organization;
    private String project;
    private String azureTag;


    public void setHost(String host) {
        this.host = host;
    }

    public void setCredentialsUser(String credentialsUser) {
        this.credentialsUser = credentialsUser;
    }

    public void setCredentialsPassword(String credentialsPassword) {
        this.credentialsPassword = credentialsPassword;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public void setAzureTag(String azureTag) {
        this.azureTag = azureTag;
    }

    @Override
    public void report(PlanNodeSnapshot result) {

        AzureApi api = new AzureApi(
            "https://"+host+"/"+organization+"/"+project,
            credentialsUser,
            credentialsPassword,
            apiVersion,
            LOGGER
        );


        reportNode(result,api);

    }

    private void reportNode(PlanNodeSnapshot node, AzureApi api) {
        if (node.getNodeType() == NodeType.TEST_CASE && node.getTags().contains(azureTag)) {
            updateTestCase(node,api);
        } else if (node.getChildren() != null){
            node.getChildren().forEach(child -> reportNode(child,api));
        }
    }



    private void updateTestCase(PlanNodeSnapshot testCase, AzureApi api) {
        try {
            String runID = api.createRun(
                property(testCase, "azurePlan"),
                property(testCase, "azureSuite"),
                property(testCase, "azureTest", testCase.getName()),
                testCase.getFinishInstant()
            );
            api.updateTestResult(runID, testCase.getResult().name());
        } catch (RuntimeException e) {
            LOGGER.error("Cannot update test case result in Azure: {}",e.getMessage());
            LOGGER.debug("",e);
        }

    }


    private String property(PlanNodeSnapshot node, String property) {
        if (node.getProperties() == null || !node.getProperties().containsKey(property)) {
            throw new WakamitiException("Property {} not present in test case {}", property, node.getDisplayName());
        }
        return node.getProperties().get(property);
    }



    private String property(PlanNodeSnapshot node, String property, String defaultValue) {
        if (node.getProperties() == null || !node.getProperties().containsKey(property)) {
            return defaultValue;
        }
        return node.getProperties().get(property);
    }


}
