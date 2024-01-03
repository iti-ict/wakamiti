package es.iti.wakamiti.azure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;


public class AzureApi {


    public static final String APIS_TEST_PLANS = "/_apis/test/plans/";
    public static final String APIS_TEST_RUNS = "/_apis/test/runs/";
    public static final String APIS_TESTPLAN_PLANS = "/_apis/testplan/plans/";
    public static final String APIS_WIT_WORKITEMS = "/_apis/wit/workitems/";
    public static final String ID = "$.id";
    public static final String APPLICATION_JSON = "application/json";
    public static final String JSON_OUTCOME = "outcome";
    public static final String JSON_COMMENT = "comment";
    public static final String JSON_ERROR_MESSAGE = "errorMessage";

    private final String urlBase;
    private final String credentials;
    private final String apiVersion;
    private final String testCaseType;
    private final HttpClient httpClient;
    private final Logger logger;



    public AzureApi(String urlBase, String user, String password, String apiVersion, String testCaseType, Logger logger) {
        this.urlBase = urlBase;
        this.testCaseType = testCaseType;
        this.credentials = Base64.getEncoder().encodeToString((user+":"+password).getBytes(StandardCharsets.UTF_8));
        this.apiVersion = apiVersion;
        this.httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))
            .build();
        this.logger = logger;
    }



    public Optional<AzurePlan> getPlanByProperties(String planName, String area, String iteration) {
        try {
            String response = get(APIS_TESTPLAN_PLANS);
            String id = extract(
                response,
                valueBy("name", planName, "areaPath", area, "iteration",iteration)+".id",
                "Cannot find a test plan with name '" + planName + "'"
            );
            String rootSuiteID = extract(response, valueBy("id", id)+".rootSuite.id");
            return Optional.of(new AzurePlan(id,planName,area,iteration,rootSuiteID));
        } catch (NoSuchElementException e) {
            logger.debug(e.getMessage());
            return Optional.empty();
        }
    }



    public Optional<AzurePlan> getPlanById(String id) {
        try {
            logger.debug("getPlanById id ='{}'",id);
            String response = get(APIS_TESTPLAN_PLANS);
            String planName = extract(response,valueBy("id",id)+".name");
            String area = extract(response,valueBy("id",id)+".areaPath");
            String iteration = extract(response, valueBy("id",id)+".iteration");
            String rootSuiteID = extract(response, valueBy("id", id)+".rootSuite.id");
            return Optional.of(new AzurePlan(id,planName,area,iteration,rootSuiteID));
        } catch (NoSuchElementException e) {
            logger.debug(e.getMessage());
            return Optional.empty();
        }
    }



    public Optional<AzureSuite> getTestSuiteById(AzurePlan plan, String suiteID, AzureSuite parent) {
        try {
            logger.debug("getTestSuiteById suiteId='{}' parent='{}'", suiteID, parent);
            String url = APIS_TESTPLAN_PLANS + plan.id() + "/suites";
            String response = get(url);
            String suiteName;
            if (parent == null) {
                suiteName = extract(
                    response,
                    valueBy("id", suiteID) + ".name",
                    "Cannot find a test suite with id '" + suiteID + "'"
                );
            } else {
                suiteName = extract(
                    response,
                    valueBy("id", suiteID, "parentSuite.id", parent.id()) + ".id",
                    "Cannot find a test suite with id '" + suiteID + "'"
                );
            }
            return Optional.of(new AzureSuite(suiteID,suiteName,plan,null));
        } catch (NoSuchElementException e) {
            logger.debug(e.getMessage());
            return Optional.empty();
        }
    }



    public Optional<AzureSuite> getTestSuiteByName(AzurePlan plan, String suiteName, AzureSuite parent) {
        try {
            logger.debug("getTestSuiteByName suiteName='{}' parent='{}'", suiteName, parent);
            String url = APIS_TESTPLAN_PLANS + plan.id() + "/suites";
            String response = get(url);
            String suiteID;
            if (parent == null) {
                suiteID = extract(
                    response,
                    valueBy("name", suiteName) + ".id",
                    "Cannot find a test suite with name '" + suiteName + "'"
                );
            } else {
                suiteID = extract(
                    response,
                    valueBy("name", suiteName, "parentSuite.id", parent.id()) + ".id",
                    "Cannot find a test suite with name '" + suiteName + "'"
                );
            }
            return Optional.of(new AzureSuite(suiteID,suiteName,plan,null));
        } catch (NoSuchElementException e) {
            logger.debug(e.getMessage());
            return Optional.empty();
        }
    }




    public void updateTestSuiteName(AzurePlan plan, String suiteId, String suiteName) {
        logger.debug("updateTestSuiteName suiteId='{}' suiteName='{}'", suiteId, suiteName);
        String payload = "{ \"name\": \""+ suiteName +"\"}";
        patch(APIS_TEST_PLANS+plan.id()+"/suites/"+suiteId, payload);
    }




    public Optional<AzureTestCase> getTestCaseByName(AzureSuite suite, String testCaseName) {
        String url = APIS_TEST_PLANS + suite.plan().id() + "/suites" + suite.idPath()+ "/points";
        String response = get(url);
        try {
            String id = extract(
                response,
                "$..[?(@.name == '" + testCaseName + "')].id",
                "Cannot find a test case with name '" + testCaseName + "'"
            );
            return Optional.of(new AzureTestCase(id,testCaseName));
        } catch (NoSuchElementException e) {
            logger.debug(e.getMessage());
            return Optional.empty();
        }
    }




    public String getTestPointID(String planID, String suiteIDPath, String testCaseID) {
        String url = APIS_TEST_PLANS +planID+"/suites"+suiteIDPath+"/points?testCaseId="+testCaseID;
        String response = get(url);
        return extract(
            response,
            "$.value.[0].id",
            "Cannot find a test point for the test case '"+testCaseID+"'"
        );
    }



    public boolean existsTestCaseID(AzureSuite suite, String testCaseID) {
        try {
            getTestPointID(suite.plan().id(), suite.idPath(), testCaseID);
            return true;
        } catch (Exception e) {
            logger.trace(e.getMessage(),e);
            return false;
        }
    }




    public AzurePlan createPlan(String name, String area, String iteration) {
        logger.debug(" => createPlan (name='{}' area='{}' iteration='{}')",name,area,iteration);
        String payload = "{ \"area\": { \"name\": \""+area+"\"}, \"iteration\": \""+iteration+"\", \"name\": \""+name+"\" }";
        String response = post(APIS_TESTPLAN_PLANS, payload);
        String planID = extract(response, ID, "Cannot find the id of the new plan");
        String planRootSuiteID = extract(response, "$.rootSuite.id", "Cannot find the root suite id of the new plan");
        AzurePlan result = new AzurePlan(planID,name,area,iteration,planRootSuiteID);
        logger.debug(" <= createPlan (name='{}' area='{}' iteration='{}') === {}",name,area,iteration,result);
        return result;
    }



    public void updatePlanName(String planId, String planName) {
        logger.debug("updatePlanName planId='{}' planName='{}'", planId, planName);
        String payload = "{ \"name\": \""+ planName +"\"}";
        patch(APIS_TEST_PLANS+planId, payload);
    }




    public AzureSuite createSuite(AzurePlan azurePlan, String suiteName, AzureSuite parent) {
        logger.debug("=> createSuite (plan='{}' name='{}' parent='{}')", azurePlan, suiteName, parent);
        String url = APIS_TESTPLAN_PLANS + azurePlan.id() +"/suites";
        String parentSuiteId = (parent == null ? azurePlan.rootSuiteID() : parent.id());
        String parentSuiteName = (parent == null ? azurePlan.name() : parent.name());
        if (parent != null) {
            url += "?suiteId="+parent.id();
        }
        String payload = "{ \"suiteType\": \"staticTestSuite\", \"name\": \""+suiteName+"\", \"parentSuite\": { \"id\": "+parentSuiteId+", \"name\": \""+parentSuiteName+"\" } }";
        String response = post(url, payload);
        String suiteID = extract(response, ID);
        AzureSuite result = new AzureSuite(suiteID,suiteName,azurePlan);
        logger.debug("<= createSuite (plan='{}' name='{}' parent='{}') === {}", azurePlan, suiteName, parent, result);
        return result;
    }



    public AzureTestCase createTestCase(AzureSuite suite, String testName) {
        logger.debug(" => createTestCase (suite='{}' name='{}')", suite, testName);
        String workItemID = createTestCaseWorkItem(testName);
        appendTesCase(suite, workItemID);
        AzureTestCase result = new AzureTestCase(workItemID,testName);
        logger.debug("<=  createTestCase (suite='{}' name='{}') === {}", suite, testName, result);
        return result;
    }



    public void updateTestCaseName(String testCaseID, String testName) {
        String url = APIS_WIT_WORKITEMS + testCaseID;
        String payload = "[{ \"op\":\"replace\", \"path\":\"/fields/System.Title\", \"from\": null, \"value\": \""+testName+"\" }]";
        patch(url, payload, "application/json-patch+json");
    }



    private String createTestCaseWorkItem(String testName) {
        String url = APIS_WIT_WORKITEMS + "$"+ URLEncoder.encode(testCaseType,StandardCharsets.UTF_8).replace("+","%20");
        String payload = "[{ \"op\":\"add\", \"path\":\"/fields/System.Title\", \"from\": null, \"value\": \""+testName+"\" }]";
        String response = post(url, payload, "application/json-patch+json");
        return extract(response, ID, "Error creating a new test case");
    }


    private void appendTesCase(AzureSuite suite, String workItemID) {
        String url = APIS_TESTPLAN_PLANS + suite.plan().id() + "/Suites/" + suite.id() + "/TestCase";
        String payload = "[{ \"workItem\": { \"id\": "+workItemID+" } } ]";
        post(url, payload);
    }



    public String createRun(String planID, Set<String> testPoints, String runName, String startDate, String finishDate) {
        String pointIDs = String.join(",", testPoints);
        String payload = String.format("{" +
                "\"name\":\"%s\"," +
                "\"plan\":{\"id\":%s}," +
                "\"pointIds\":[%s], " +
                "\"automated\":true, " +
                "\"startDate\":\"%s\"," +
                "\"completeDate\":\"%s\"" +
            " }",
                runName,
                planID,
                pointIDs,
                startDate,
                finishDate
        );

        String response = post(APIS_TEST_RUNS,payload);
        return extract(response, ID, "Error creating a new run");
    }


    public void attachFile(String runID, Path report) {
        try {
            String reportEnconded = Base64.getEncoder().encodeToString(Files.readAllBytes(report));
            String urlFile = APIS_TEST_RUNS + runID + "/attachments";
            String payloadFile = "{ \"attachmentType\": \"GeneralAttachment\", \"comment\": \"Resultados ejecución\", \"fileName\": \""+report.getFileName()+"\" , \"stream\": \"" + reportEnconded + "\" }";
            post(urlFile, payloadFile);
        } catch (IOException e) {
            throw new WakamitiException("Cannot post report attachment",e);
        }

    }



    public void updateRunResults(String runID, Map<String,PlanNodeSnapshot> nodeByTestPoint) {


        String response = get(APIS_TEST_RUNS+runID+"/results");

        List<String> payloads = new LinkedList<>();
        nodeByTestPoint.forEach((testPointID, node)-> {
            String status = node.getResult().name();
            String resultID = extract(
                response,
                "$.value[?(@.testPoint.id==" + testPointID + ")].id",
                "Cannot get the test result for the run"
            );
            long duration = Duration.between(
                LocalDateTime.parse(node.getStartInstant()),
                LocalDateTime.parse(node.getFinishInstant())
            ).toMillis();

            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> value = new HashMap<>();
            value.put("id",resultID);
            value.put("state","Completed");
            value.put("duration",duration);

            try {
                if (status.equalsIgnoreCase("PASSED")) {
                    value.put(JSON_OUTCOME,"Passed");
                    value.put(JSON_COMMENT,"Execution Successful");
                    payloads.add(mapper.writeValueAsString(value));
                } else if (status.equalsIgnoreCase("FAILED")) {
                    value.put(JSON_OUTCOME,"Failed");
                    value.put(JSON_COMMENT,"Execution Failed");
                    value.put(JSON_ERROR_MESSAGE,node.getErrorMessage());
                    payloads.add(mapper.writeValueAsString(value));
                } else {
                    value.put(JSON_OUTCOME,"Unspecified");
                    value.put(JSON_COMMENT,"Execution Error");
                    value.put(JSON_ERROR_MESSAGE,node.getErrorMessage());
                    payloads.add(mapper.writeValueAsString(value));
                }
            } catch (JsonProcessingException e) {
                throw new WakamitiException(e);
            }
        });


        patch(APIS_TEST_RUNS +runID+"/results","["+String.join(",",payloads)+"]");


        patch(APIS_TEST_RUNS+runID, "{ \"state\": \"Completed\" }");


    }



    private String extract(String json, String path, String errorMessage) {
        logger.debug("checking path {}",path);
        Object object = JsonPath.read(json, path);
        if (object == null) {
            throw new NoSuchElementException(errorMessage);
        }
        String extracted;
        if (object instanceof List<?>) {
            List<?> list = (List<?>) object;
            if (list.isEmpty()) {
                throw new NoSuchElementException(errorMessage);
            }
            extracted = list.get(0).toString();
        } else {
            extracted = object.toString();
        }
        logger.debug(extracted);
        return extracted;
    }



    private String extract(String json, String path) {
        return extract(json,path,"Cannot extract path "+path+" from response");
    }



    private String get(String uri) {
        return send(request("GET",uri),"");
    }



    private String post(String uri, String payload) {
        return post(uri,payload, APPLICATION_JSON);
    }


    private String patch(String uri, String payload) {
        return patch(uri,payload, APPLICATION_JSON);
    }


    private String post(String uri, String payload, String contentType) {
        return send(request("POST",uri,payload,contentType),payload);
    }


    private String patch(String uri, String payload, String contentType) {
        return send(request("PATCH",uri,payload,contentType),payload);
    }



    private HttpRequest request(String method, String uri) {
        return HttpRequest.newBuilder()
                .method(method, HttpRequest.BodyPublishers.noBody())
                .uri(url(uri))
                .header("Authorization", "Basic "+credentials)
                .header("Accept", APPLICATION_JSON)
                .build();
    }


    private HttpRequest request(String method, String uri, String payload, String contentType) {
        return HttpRequest.newBuilder()
                .method(method, HttpRequest.BodyPublishers.ofString(payload))
                .uri(url(uri))
                .header("Authorization", "Basic "+credentials)
                .header("Content-Type", contentType)
                .header("Accept", APPLICATION_JSON)
                .build();
    }




    private URI url(String uri) {
        return URI.create(urlBase+uri+(uri.contains("?") ? "&" : "?")+"api-version="+apiVersion);
    }




    private String valueBy(String... args) {
        List<String> criteria = new LinkedList<>();
        for (int i = 0; i < args.length; i+=2) {
            criteria.add("@."+args[i]+"=='"+args[i+1]+"'");
        }
        return "$.value[?("+String.join(" && ",criteria)+")]";
    }



    private String send(HttpRequest request, String payload) {
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("Azure call => {} {} {} ", request.method(), request.uri(), payload);
            }
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (logger.isTraceEnabled()) {
                logger.trace("Azure response => {} {}", response.statusCode(), response.body());
            }
            if (response.statusCode() >= 400) {
                throw new WakamitiException("The Azure API returned a non-OK response");
            }
            return response.body();
        } catch (IOException e) {
            throw new WakamitiException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WakamitiException(e);
        }
    }



}
