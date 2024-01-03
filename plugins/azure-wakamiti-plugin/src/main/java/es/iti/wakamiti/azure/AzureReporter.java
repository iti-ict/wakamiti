package es.iti.wakamiti.azure;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.extensions.Reporter;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.azure.internal.Util;
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
import java.util.Optional;
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
    public static final String AZURE_PLAN_ID = "azurePlanId";
    public static final String AZURE_SUITE = "azureSuite";
    public static final String AZURE_SUITE_ID = "azureSuiteId";
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

        System.out.println("azure report disabled:" +disabled);
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


        Map<AzurePlan,Map<AzureSuite,List<PlanNodeSnapshot>>> testCases = getOrCreateTestCases(result, new HashMap<>(), api);
        if (testCases.isEmpty()) {
            return;
        }


        for (var testCaseEntry: testCases.entrySet()) {

            AzurePlan testPlan = testCaseEntry.getKey();

            Map<String,PlanNodeSnapshot> testPoints = new HashMap<>();

            for (var suiteEntry : testCaseEntry.getValue().entrySet()) {
                AzureSuite azureSuite = suiteEntry.getKey();
                List<PlanNodeSnapshot> nodes = suiteEntry.getValue();
                testPoints.putAll( getTestPointsStatus(azureSuite, nodes, api) );
            }

            String runID = api.createRun(
                testPlan.id(),
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




    private Map<AzurePlan,Map<AzureSuite,List<PlanNodeSnapshot>>> getOrCreateTestCases(
        PlanNodeSnapshot node,
        Map<AzurePlan,Map<AzureSuite,List<PlanNodeSnapshot>>> result,
        AzureApi api
    ) {
        boolean matchAzureTestCase = (testCasePerFeature ?
            node.getNodeType() == NodeType.AGGREGATOR && "feature".equals(node.getProperties().get("gherkinType")) :
            node.getNodeType() == NodeType.TEST_CASE
        );

        if (matchAzureTestCase && node.getTags().contains(azureTag)) {

            Pair<String,String> testPlan = Util.getPropertyIdAndName(node, AZURE_PLAN);
            List<Pair<String, String>> testSuitePath = Util.getListPropertyIdAndName(node, AZURE_SUITE);
            String area = Util.property(node, AZURE_AREA, null);
            String iteration = Util.property(node, AZURE_ITERATION, null);

            if (testPlan != null && testSuitePath != null) {

                AzurePlan azurePlan = getOrCreateAzurePlan(testPlan,area,iteration,api);
                if (azurePlan == null) {
                    return result;
                }

                AzureSuite azureSuite = getOrCreateAzureSuite(azurePlan, testSuitePath, api);
                if (azureSuite == null) {
                    return result;
                }

                Map<AzureSuite,List<PlanNodeSnapshot>> suites = result.computeIfAbsent(azurePlan, x -> new HashMap<>());
                List<PlanNodeSnapshot> nodes = suites.computeIfAbsent(azureSuite, x -> new LinkedList<>());
                nodes.add(node);
            }

        } else if (node.getChildren() != null){
            node.getChildren().forEach(child -> getOrCreateTestCases(child,result,api));
        }
        return result;
    }








    private Map<String,PlanNodeSnapshot> getTestPointsStatus(AzureSuite suite, List<PlanNodeSnapshot> planTestCases, AzureApi api) {
        Map<String,PlanNodeSnapshot> testPoints = new HashMap<>();
        for (PlanNodeSnapshot testCase : planTestCases) {

            AzureTestCase azureTestCase = getOrCreateTestCase(
                suite,
                Util.getPropertyValueIdAndName(testCase, AZURE_TEST, null),
                api
            );

            if (azureTestCase == null) {
                continue;
            }
            testPoints.put(api.getTestPointID(
                suite.plan().id(),
                suite.idPath(),
                azureTestCase.id()
            ), testCase);
        }
        return testPoints;
    }





    private AzurePlan getOrCreateAzurePlan(Pair<String,String> nameAndId, String area, String iteration, AzureApi api) {

        Optional<AzurePlan> azurePlan;

        String planId = nameAndId.value();
        String planName = nameAndId.key();

        if (planId != null) {
            azurePlan = api.getPlanById(planId);
            azurePlan.ifPresentOrElse(
                it -> api.updatePlanName(planId, planName),
                ()-> LOGGER.warn("Test Plan id {} not present in Azure", planId)
            );
        } else {
            azurePlan = api.getPlanByProperties(planName, area, iteration);
        }

        return azurePlan.orElseGet(()-> {
            if (createItemsIfAbsent) {
                LOGGER.info("Creating new test plan '{}' [ {} / {} ]", planName, area, iteration);
                return api.createPlan(planName, area, iteration);
            } else {
                LOGGER.warn("Test plan '{}' [ {} / {} ] is not defined and will be ignored", planName, area, iteration);
                return null;
            }
        });
    }



    private AzureSuite getOrCreateAzureSuite(AzurePlan azurePlan, List<Pair<String,String>> suitePath, AzureApi api) {

        AzureSuite parent = null;
        AzureSuite azureSuite = null;

        for (Pair<String,String> nameAndId : suitePath) {

            LOGGER.debug("getOrCreateAzureSuite (path = [{}] {})", nameAndId.value(), nameAndId.key());

            String suiteName = nameAndId.key();
            String suiteId = nameAndId.value();

            if (suiteId != null) {
                azureSuite = api.getTestSuiteById(azurePlan,suiteId, parent).orElse(null);
                if (azureSuite != null) {
                    api.updateTestSuiteName(azurePlan, suiteId, suiteName);
                } else {
                    LOGGER.warn("Test Suite id {} not present in Azure", suiteId);
                }
            } else {
                azureSuite = api.getTestSuiteByName(azurePlan, suiteName, parent).orElse(null);
            }

            if (azureSuite == null) {
                if (createItemsIfAbsent) {
                    LOGGER.info("Creating new test suite '{}'", suiteName);
                    azureSuite = api.createSuite(azurePlan, suiteName, parent);
                } else {
                    LOGGER.warn("Test suite '{}' is not defined and will be ignored", suiteName);
                    return null;
                }
            }

            parent = azureSuite;
        }

        return azureSuite;
    }






    private AzureTestCase getOrCreateTestCase(AzureSuite suite, Pair<String,String> nameAndId, AzureApi api) {

        String testCaseId = nameAndId.value();
        String testCaseName = nameAndId.key();
        AzureTestCase azureTestCase;

        if (testCaseId != null) {
            if (!api.existsTestCaseID(suite,testCaseId)) {
                LOGGER.warn("Test Case id '{}' not present in Azure", testCaseId);
                return null;
            } else {
                api.updateTestCaseName(testCaseId, testCaseName);
                azureTestCase = new AzureTestCase(testCaseId, testCaseName);
            }
        } else {
            azureTestCase = api.getTestCaseByName(suite, testCaseName).orElse(null);
        }

        if (testCaseId == null) {
            if (createItemsIfAbsent) {
                LOGGER.info("Creating new test case '{}'", testCaseName);
                azureTestCase = api.createTestCase(suite, testCaseName);
            } else {
                LOGGER.warn("Test case '{}' is not defined and will be ignored", testCaseName);
            }
        }

        return azureTestCase;

    }









}
