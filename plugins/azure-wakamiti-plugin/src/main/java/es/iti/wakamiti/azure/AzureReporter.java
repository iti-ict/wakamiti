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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
    public static final String AZURE_AREA = "azureArea";
    public static final String AZURE_ITERATION = "azureIteration";
    public static final String AZURE_TEST_ID = "azureTestId";

    private boolean disabled;
    private String host;
    private String credentialsUser;
    private String credentialsPassword;
    private String apiVersion;
    private String organization;
    private String project;
    private String azureTag;
    private List<String> attachments;
    private boolean testCasePerFeature;
    private String testCaseType;
    private boolean createItemsIfAbsent;
    private int timeZoneAdjustment;


    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

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

    public void setTestCasePerFeature(boolean testCasePerFeature) {
        this.testCasePerFeature = testCasePerFeature;
    }

    public void setTestCaseType(String testCaseType) {
        this.testCaseType = testCaseType;
    }

    public void setCreateItemsIfAbsent(boolean createItemsIfAbsent) {
        this.createItemsIfAbsent = createItemsIfAbsent;
    }

    public void setTimeZoneAdjustment(int timeZoneAdjustment) {
        this.timeZoneAdjustment = timeZoneAdjustment;
    }


    @Override
    public void report(PlanNodeSnapshot result) {

        if (disabled) {
            return;
        }

        AzureApi api = new AzureApi(
            "https://"+host+"/"+organization+"/"+project,
            credentialsUser,
            credentialsPassword,
            apiVersion,
            testCaseType,
            LOGGER
        );


        Map<AzurePlan,Map<AzureSuite,List<PlanNodeSnapshot>>> testCases = getTestCases(result, new HashMap<>());
        if (testCases.isEmpty()) {
            return;
        }

        for (var testCaseEntry: testCases.entrySet()) {

            AzurePlan testPlan = testCaseEntry.getKey();
            AzurePlan azurePlan = getAzurePlan(testPlan,api);
            if (azurePlan == null) {
                continue;
            }

            Map<String,PlanNodeSnapshot> testPoints = new HashMap<>();

            for (var suiteEntry : testCaseEntry.getValue().entrySet()) {

                String suiteName = suiteEntry.getKey().name();
                AzureSuite azureSuite = getAzureSuite(azurePlan, suiteName, api);
                if (azureSuite == null) {
                    continue;
                }

                List<PlanNodeSnapshot> nodes = suiteEntry.getValue();

                testPoints.putAll( getTestPointsStatus(azureSuite, nodes, api) );

            }

            String runID = api.createRun(
                    azurePlan.id(),
                    testPoints.keySet(),
                    testPlan.name() + " - run by Wakamiti ",
                    adjustTimeZone(result.getStartInstant()),
                    adjustTimeZone(result.getFinishInstant())
            );
            api.updateRunResults(runID,testPoints);
            attachFiles(runID, api);

        }

    }



    private String adjustTimeZone(String datetime) {
        return LocalDateTime.parse(datetime).plusHours(timeZoneAdjustment).toString();
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




    private Map<AzurePlan,Map<AzureSuite,List<PlanNodeSnapshot>>> getTestCases(
        PlanNodeSnapshot node,
        Map<AzurePlan,Map<AzureSuite,List<PlanNodeSnapshot>>> result
    ) {
        boolean matchAzureTestCase = (testCasePerFeature ?
            node.getNodeType() == NodeType.AGGREGATOR && "feature".equals(node.getProperties().get("gherkinType")) :
            node.getNodeType() == NodeType.TEST_CASE
        );
        if (matchAzureTestCase && node.getTags().contains(azureTag)) {
            String testPlan = property(node, AZURE_PLAN);
            String suiteName = property(node, AZURE_SUITE);
            String area = property(node, AZURE_AREA, null);
            String iteration = property(node, AZURE_ITERATION, null);
            if (testPlan != null && suiteName != null) {
                AzurePlan azurePlan = new AzurePlan(testPlan,area,iteration);
                Map<AzureSuite,List<PlanNodeSnapshot>> suites = result.computeIfAbsent(azurePlan, x -> new HashMap<>());
                AzureSuite azureSuite = new AzureSuite(suiteName);
                List<PlanNodeSnapshot> nodes = suites.computeIfAbsent(azureSuite, x -> new LinkedList<>());
                nodes.add(node);
            }
        } else if (node.getChildren() != null){
            node.getChildren().forEach(child -> getTestCases(child,result));
        }
        return result;
    }





    private Map<String,PlanNodeSnapshot> getTestPointsStatus(AzureSuite suite, List<PlanNodeSnapshot> planTestCases, AzureApi api) {
        Map<String,PlanNodeSnapshot> testPoints = new HashMap<>();
        for (PlanNodeSnapshot testCase : planTestCases) {
            String testName = property(testCase, AZURE_TEST, testCase.getName());
            String definedTestId = property(testCase, AZURE_TEST_ID, null);
            String testCaseID;
            if (definedTestId != null) {
                if (!checkExistTestId(suite, definedTestId, testName, api)) {
                    continue;
                }
                testCaseID = definedTestId;
            } else {
                testCaseID = getTestCase(suite, testName, api);
            }
            if (testCaseID == null) {
                continue;
            }
            testPoints.put(api.getTestPointID(
                suite.plan().id(),
                suite.id(),
                testCaseID
            ), testCase);
        }
        return testPoints;
    }



    private AzurePlan getAzurePlan(AzurePlan testPlan, AzureApi api) {
        return api.getPlan(testPlan.name(), testPlan.area(), testPlan.iteration()).orElseGet(()-> {
            if (createItemsIfAbsent) {
                LOGGER.info("Creating new test plan '{}' [ {} / {} ]", testPlan.name(), testPlan.area(), testPlan.iteration());
                return api.createPlan(testPlan.name(), testPlan.area(), testPlan.iteration());
            } else {
                LOGGER.warn("Test plan '{}' [ {} / {} ] is not defined and will be ignored", testPlan.name(), testPlan.area(), testPlan.iteration());
                return null;
            }
        });
    }



    private AzureSuite getAzureSuite(AzurePlan azurePlan, String suiteName, AzureApi api) {
        return api.getTestSuite(azurePlan, suiteName).orElseGet(()-> {
            if (createItemsIfAbsent) {
                LOGGER.info("Creating new test suite '{}'", suiteName);
                return api.createSuite(azurePlan, suiteName);
            } else {
                LOGGER.warn("Test suite '{}' is not defined and will be ignored", suiteName);
                return null;
            }
        });
    }



    private String getTestCase(AzureSuite suite, String testName, AzureApi api) {
        return api.getTestCaseID(suite,testName).orElseGet(()-> {
            if (createItemsIfAbsent) {
                LOGGER.info("Creating new work item for test case '{}'", testName);
                return api.createTestCase(suite,testName);
            } else {
                LOGGER.warn("Test case '{}' is not defined in Azure test plan and will be ignored", testName);
                return null;
            }
        });
    }




    private boolean checkExistTestId(AzureSuite suite, String definedTestId, String testName, AzureApi api) {
        try {
            api.getTestPointID(suite.plan().id(), suite.id(), definedTestId);
            api.updateTestCaseName(definedTestId, testName);
            return true;
        } catch (Exception e) {
            return false;
        }
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
