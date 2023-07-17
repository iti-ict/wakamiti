package es.iti.wakamiti.azure;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.extensions.Reporter;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.util.WakamitiLogger;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;


@Extension(
        provider =  "es.iti.wakamiti",
        name = "azure-reporter",
        version = "1.0",
        priority = 10
)
public class AzureReporter implements Reporter {

    private static final Logger LOGGER = WakamitiLogger.forClass(AzureReporter.class);
    public static final String AZURE_PLAN = "azurePlan";
    public static final String AZURE_SUITE = "azureSuite";
    public static final String AZURE_TEST = "azureTest";

    private String host;
    private String credentialsUser;
    private String credentialsPassword;
    private String apiVersion;
    private String organization;
    private String project;
    private String azureTag;
    private List<String> attachments;

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

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
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


        Map<String,List<PlanNodeSnapshot>> testCases = getTestCases(result, new HashMap<>());
        if (testCases.isEmpty()) {
            return;
        }



        testCases.forEach((testPlan, planTestCases)->{

            String planID = api.getPlanID(testPlan);
            Map<String,String> testPoints = getTestPointsStatus(planID, planTestCases, api);
            String runID = api.createRun(planID, testPoints.keySet(), testPlan + " - run by Wakamiti ");
            api.updateRunResults(runID,testPoints);
            attachFiles(runID, api);

        });

    }



    private void attachFiles(String runID, AzureApi api) {
        for (String attachment : attachments) {
            var pathMatcher = FileSystems.getDefault().getPathMatcher("glob:"+attachment);
            try (Stream<Path> walker = Files.walk(Path.of("")).filter(pathMatcher::matches)) {
                walker.forEach(file -> {
                    LOGGER.info("attaching {}...", file);
                    api.attachFile(runID, file);
                });
            } catch (IOException e) {
                LOGGER.error("Cannot attach file {} : {}", attachment, e.getMessage());
                LOGGER.debug("",e);
            }
        }
    }


    private Map<String,List<PlanNodeSnapshot>> getTestCases(PlanNodeSnapshot node, Map<String,List<PlanNodeSnapshot>> result) {
        if (node.getNodeType() == NodeType.TEST_CASE && node.getTags().contains(azureTag)) {
            String testPlan = property(node, AZURE_PLAN);
            String suiteName = property(node, AZURE_SUITE);
            if (testPlan != null && suiteName != null) {
                result.computeIfAbsent(testPlan, x->new LinkedList<>()).add(node);
            }
        } else if (node.getChildren() != null){
            node.getChildren().forEach(child -> getTestCases(child,result));
        }
        return result;
    }




    private Map<String,String> getTestPointsStatus(String planID, List<PlanNodeSnapshot> planTestCases, AzureApi api) {
        Map<String,String> testPoints = new HashMap<>();
        for (PlanNodeSnapshot testCase : planTestCases) {
            String suiteName = property(testCase, AZURE_SUITE);
            String testName = property(testCase, AZURE_TEST, testCase.getName());
            String suiteID = api.getTestSuiteID(planID, suiteName);
            String testCaseID = api.getTestCaseID(planID,suiteID, testName);
            testPoints.put(api.getTestPointID(
                planID,
                suiteID,
                testCaseID
            ), testCase.getResult().name());
        }
        return testPoints;
    }




    private String property(PlanNodeSnapshot node, String property) {
        if (node.getProperties() == null || !node.getProperties().containsKey(property)) {
            LOGGER.warn("Property {} not present in test case {}", property, node.getDisplayName());
            return null;
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
