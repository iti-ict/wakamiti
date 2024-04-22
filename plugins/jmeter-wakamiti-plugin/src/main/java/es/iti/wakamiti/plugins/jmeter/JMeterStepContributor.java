/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.plugins.jmeter;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.annotations.I18nResource;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.api.plan.DataTable;
import es.iti.wakamiti.api.plan.Document;
import org.apache.http.entity.ContentType;
import org.apache.jmeter.protocol.http.util.HTTPConstantsInterface;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslJsonExtractor;
import us.abstracta.jmeter.javadsl.core.threadgroups.DslDefaultThreadGroup;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;


@Extension(provider = "es.iti.wakamiti", name = "jmeter", version = "1.1")
@I18nResource("es_iti_wakamiti_jmeter")
public class JMeterStepContributor implements StepContributor {

    private DslDefaultThreadGroup threadGroup = threadGroup();

    private boolean basicTest = true;
    private String baseUrl;
    private TestPlanStats lastTestStats;
    private boolean resultsTreeEnabled;
    private boolean influxDBEnabled;
    private boolean csvEnabled;
    private boolean htmlEnabled;
    private String influxDBUrl;
    private String csvPath;
    private String htmlPath;
    private String username;
    private String password;
    private static final String ERROR_MESSAGE = "No test results stored to verify the response times.";

    private void resetThreadGroup() {
        this.threadGroup = null;
        this.basicTest = true;
    }
    private List<String> getCSVHeaders(String fichero) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(fichero))) {
            return Arrays.asList(br.readLine().split(","));
        }
    }

    private String buildRequestBody(List<String> columnNames) {
        return columnNames.stream()
                .map(name -> String.format("\"%s\": \"${%s}\"", name, name))
                .collect(Collectors.joining(", ", "{", "}"));
    }
    private void setListeners(){

        if (resultsTreeEnabled) {
            threadGroup.children(resultsTreeVisualizer());
        }

        if (influxDBEnabled) {
            threadGroup.children(influxDbListener(influxDBUrl));
        }

        if (csvEnabled) {
            threadGroup.children(jtlWriter(csvPath));
        }

        if (htmlEnabled) {
            threadGroup.children(htmlReporter(htmlPath));
        }
    }

    private void runTest() throws IOException {

        if(basicTest)
        {
            lastTestStats = testPlan(
                    threadGroup.children(
                            httpSampler(baseUrl)
                    )).run();
        }
        else {
            lastTestStats = testPlan(threadGroup).run();
        }
    }
    @Step(value = "jmeter.define.baseURL")
    public void setBaseURL(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setResultsTree(Boolean enable) { this.resultsTreeEnabled = enable; }

    public void setInfluxDB(Boolean enable) { this.influxDBEnabled = enable; }

    public void setHTML(Boolean enable) { this.htmlEnabled = enable; }

    public void setCSV(Boolean enable) { this.csvEnabled = enable; }

    public void setInfluxDBUrl(String url) { this.influxDBUrl = url; }

    public void setHTMLPath(String path) { this.htmlPath= path; }

    public void setCSVPath(String path) { this.csvPath = path; }

    public void setUsername(String username) { this.username = username; }

    public void setPassword(String password) { this.password = password; }


    @Step(value = "jmeter.define.csvinput", args = { "file:text" })
    public void setCSVInput(String file) throws IOException {

        List<String> columnNames = getCSVHeaders(file);
        String requestBody = buildRequestBody(columnNames);

        threadGroup.children(csvDataSet(file));
        threadGroup.children(
                httpSampler(baseUrl)
                        .post(requestBody,
                                ContentType.APPLICATION_JSON)
        );
        basicTest = false;
    }
    @Step(value = "jmeter.define.csvinputvariables", args = { "file:text" })
    public void setCSVInputVariables(String file, DataTable variables) throws IOException {

        List<String> csvHeaders = getCSVHeaders(file);

        List<String> filter = Arrays.stream(variables.getValues())
                .map(row -> row[0])
                .filter(csvHeaders::contains)
                .collect(Collectors.toList());
        String requestBody = buildRequestBody(filter);

        threadGroup.children(csvDataSet(file));
        threadGroup.children(
                httpSampler(baseUrl)
                        .post(requestBody,
                                ContentType.APPLICATION_JSON)
        );
        basicTest = false;
    }
    @Step(value = "jmeter.define.auth.basic", args = { "username:text" , "password:text" })
    public void setAuthBasic(String username, String password){

       threadGroup.children(httpAuth().basicAuth(baseUrl,System.getenv(username), System.getenv(password)));
    }
    @Step(value = "jmeter.define.auth.default")
    public void setAuthBasicDefault(){

        threadGroup.children(httpAuth().basicAuth(baseUrl,System.getenv(username), System.getenv(password)));
    }
    @Step(value = "jmeter.define.cookies")
    public void disableCookies(){

        threadGroup.children(httpCookies().disable());
    }
    @Step(value = "jmeter.define.cache")
    public void disableCache(){

        threadGroup.children(httpCache().disable());
    }
    @Step(value = "jmeter.define.get", args = { "service:text" })
    public void getRequest(String service) {
        threadGroup.children(httpSampler(baseUrl+service));
        basicTest = false;
    }

    @Step(value = "jmeter.define.put", args = { "service:text" })
    public void putRequest(String service, Document body) {

        String requestBody = body.getContent();
        threadGroup.children(
                httpSampler(baseUrl+service)
                        .method(HTTPConstantsInterface.PUT)
                        .contentType(ContentType.APPLICATION_JSON)
                        .body(requestBody)
        );
        basicTest = false;
    }
    @Step(value = "jmeter.define.post", args = { "service:text" })
    public void postRequest(String service, Document body) {

        String requestBody = body.getContent();
        threadGroup.children(
                httpSampler(baseUrl+service)
                        .method(HTTPConstantsInterface.POST)
                        .contentType(ContentType.APPLICATION_JSON)
                        .body(requestBody)
        );
        basicTest = false;
    }
    @Step(value = "jmeter.extract.regex.get", args = { "service:text", "variableName:text", "regex:text"})
    public void getExtractRegex(String service, String variableName, String regex){
        threadGroup.children(httpSampler(baseUrl+service),(regexExtractor(variableName,regex)));
        basicTest = false;
    }
    @Step(value = "jmeter.extract.boundaries.get", args = { "service:text", "variableName:text", "leftBoundarie:text"," rightBoundarie:text"})
    public void getExtractBoundaries(String service, String variableName, String leftBoundarie, String rightBoundarie){
        threadGroup.children(httpSampler(baseUrl+service)).children(boundaryExtractor(variableName,leftBoundarie,rightBoundarie));
        basicTest = false;
    }
    @Step(value = "jmeter.extract.json.get", args = { "service:text", "variableName:text", "jsonPath:text"})
    public void getExtractJson(String service, String variableName, String jsonPath){
        threadGroup.children(httpSampler(baseUrl+service).children(jsonExtractor(variableName,jsonPath).queryLanguage(DslJsonExtractor.JsonQueryLanguage.JSON_PATH)));
        basicTest = false;
    }
    @Step(value = "jmeter.extract.put", args = { "service:text", "variableName:text"})
    public void putWithVariable(String service, String variableName) {
        threadGroup.children(
                httpSampler(baseUrl + service)
                        .method(HTTPConstantsInterface.PUT)
                        .contentType(ContentType.APPLICATION_JSON)
                        // Aquí es donde utilizas la variable extraída anteriormente.
                        // Asegúrate de que el nombre de la variable coincida con el que extrajiste.
                        .body("${" + variableName + "}")
        );
        basicTest = false;
    }
    @Step(value = "jmeter.extract.post", args = { "service:text", "variableName:text"})
    public void postWithVariable(String service, String variableName) {
        threadGroup.children(
                httpSampler(baseUrl + service)
                        .method(HTTPConstantsInterface.POST)
                        .contentType(ContentType.APPLICATION_JSON)
                        // Aquí es donde utilizas la variable extraída anteriormente.
                        // Asegúrate de que el nombre de la variable coincida con el que extrajiste.
                        .body("${" + variableName + "}")
        );
        basicTest = false;
    }
    @Step(value = "jmeter.extract.endpoint.get", args = { "service:text", "variableName:text"})
    public void getWithEndpointExtracted(String service, String variableName) {
        String endpointExtracted = service + "/${" + variableName + "}";
        threadGroup.children(httpSampler(baseUrl+endpointExtracted));
        basicTest = false;
    }
    @Step(value = "jmeter.extract.endpoint.put", args = { "service:text", "variableName:text"})
    public void putWithEndpointExtracted(String service, String variableName, Document body) {
        String endpointExtracted = service + "/${" + variableName + "}";
        String requestBody = body.getContent();
        threadGroup.children(
                httpSampler(baseUrl+endpointExtracted)
                        .method(HTTPConstantsInterface.PUT)
                        .contentType(ContentType.APPLICATION_JSON)
                        .body(requestBody)
        );
        basicTest = false;
    }
    @Step(value = "jmeter.extract.endpoint.post", args = { "service:text", "variableName:text"})
    public void postWithEndpointExtracted(String service, String variableName, Document body) {
        String endpointExtracted = service + "/${" + variableName + "}";
        String requestBody = body.getContent();
        threadGroup.children(
                httpSampler(baseUrl+endpointExtracted)
                        .method(HTTPConstantsInterface.POST)
                        .contentType(ContentType.APPLICATION_JSON)
                        .body(requestBody)
        );
        basicTest = false;
    }
    @Step(value = "jmeter.define.connectiontimeout", args = { "duration:int" })
    public void setConnectionTimeout(Integer duration) {

       threadGroup.children(httpDefaults().connectionTimeout(Duration.ofSeconds(duration)));
    }
    @Step(value = "jmeter.define.responsetimeout", args = { "duration:int" })
    public void setResponseTimeout(Integer duration) {

        threadGroup.children(httpDefaults().responseTimeout(Duration.ofMinutes(duration)));
    }
    @Step(value = "jmeter.define.resources")
    public void downloadEmbeddedResources() {

        threadGroup.children(httpDefaults().downloadEmbeddedResources());
    }
    @Step(value = "jmeter.define.proxy", args = { "url:text" })
    public void setResponseTimeout(String url) {

        threadGroup.children(httpDefaults().proxy(url));
    }


    @Step(value = "jmeter.test.jmxfile", args = { "file:text" })
    public void runJMXTest(String file) throws IOException {

        lastTestStats = DslTestPlan.fromJmx(file).run();

    }

    @Step(value = "jmeter.test.foamtest")
    public void runFoamTest() throws IOException {

        threadGroup = threadGroup(1, 1);

        setListeners();

        runTest();

    }

    @Step(value = "jmeter.test.loadtest", args = {"users:int", "duration:int"})
    public void runLoadTest(Integer users, Integer duration) throws IOException {


         threadGroup = threadGroup.rampToAndHold(users, Duration.ofSeconds(0), Duration.ofMinutes(duration));

         setListeners();

         runTest();

    }
    @Step(value = "jmeter.test.limitetest", args = {"users:int", "usersIncrease:int", "maxUsers:int", "duration:int"})
    public void runLimitTest(Integer users, Integer usersIncrease, Integer maxUsers, Integer duration) throws IOException {

        int usuariosActuales = users;

        while (usuariosActuales <= maxUsers) {
            threadGroup= threadGroup.rampTo(usuariosActuales, Duration.ofMinutes(duration));
            usuariosActuales += usersIncrease;
        }

        setListeners();

        runTest();

    }
    @Step(value = "jmeter.test.stresstest", args = {"users:int", "usersIncrease:int", "maxUsers:int", "duration:int"})
    public void runLoadTest(Integer users, Integer usersIncrease, Integer maxUsers, Integer duracion) throws IOException {

        // Calcula el número total de pasos necesarios para llegar de 'usuarios' a 'maxUsuarios' en incrementos de 'incrementoUsuarios'
        int totalPasos = (maxUsers - users) / usersIncrease;
        // Crea el grupo de hilos con los usuarios iniciales, incrementando usuarios cada periodo de tiempo especificado
        int actualUsers = users;
        for (int paso = 0; paso <= totalPasos; paso++) {
            threadGroup = threadGroup.rampToAndHold(actualUsers, Duration.ofSeconds(10), Duration.ofMinutes(duracion));
            actualUsers += usersIncrease;
        }

        //Disminuir a 0 'usuarios'
        threadGroup = threadGroup.rampTo(0, Duration.ofSeconds(20));

        setListeners();

        runTest();

    }
    @Step(value = "jmeter.test.peaktest", args = {"peaks:int", "peakUsers:int", "nonPeakUsers:int", "duration:int"})
    public void runPeakTest(Integer peaks, Integer peakUsers, Integer nonPeakUsers, Integer duration) throws IOException {

        threadGroup = threadGroup.rampToAndHold(nonPeakUsers, Duration.ofSeconds(20), Duration.ofMinutes(duration));

        for (int i = 0; i < peaks; i++) {
            // Sube al pico
            threadGroup = threadGroup.rampTo(peakUsers, Duration.ofSeconds(20));
            // Baja al número de usuarios fuera del pico
            threadGroup = threadGroup.rampTo(nonPeakUsers, Duration.ofSeconds(20));
            // Mantiene el número de usuarios fuera del pico
            threadGroup = threadGroup.holdFor(Duration.ofMinutes(duration));
        }

        //Disminuir a 0 'usuarios'
        threadGroup = threadGroup.rampTo(0, Duration.ofSeconds(20));

        setListeners();

        runTest();

    }

    @Step(value = "jmeter.assert.percentil", args = {"percentile:int", "duration:int"})
    public void setPercentile(Integer percentile, Integer duration) {

        if (lastTestStats == null) {
            throw new IllegalStateException(ERROR_MESSAGE);
        }

        switch (percentile){
            case 50:
                assertThat(lastTestStats.overall().sampleTime().median()).isLessThan(Duration.ofSeconds(duration));
                break;
            case 90:
                assertThat(lastTestStats.overall().sampleTime().perc90()).isLessThan(Duration.ofSeconds(duration));
                break;
            case 95:
                assertThat(lastTestStats.overall().sampleTime().perc95()).isLessThan(Duration.ofSeconds(duration));
                break;
            case 99:
                assertThat(lastTestStats.overall().sampleTime().perc99()).isLessThan(Duration.ofSeconds(duration));
                break;
            default:
                throw new IllegalArgumentException("Unsupported percentile: " + percentile);
        }
        resetThreadGroup();
    }
    @Step(value = "jmeter.assert.responseTime", args = "duracionTest:int")
    public void setResponseTime(Integer duration){

        if (lastTestStats == null) {
            throw new IllegalStateException(ERROR_MESSAGE);
        }

        assertThat(lastTestStats.overall().sampleTime().mean()).isLessThan(Duration.ofSeconds(duration));
        resetThreadGroup();
    }
    @Step(value = "jmeter.assert.percentilms", args = {"percentil:int", "duracionTest:int"})
    public void setPercentilems(Integer percentile, Integer duration) {

        if (lastTestStats == null) {
            throw new IllegalStateException(ERROR_MESSAGE);
        }

        switch (percentile){
            case 50:
                assertThat(lastTestStats.overall().sampleTime().median()).isLessThan(Duration.ofMillis(duration));
                break;
            case 90:
                assertThat(lastTestStats.overall().sampleTime().perc90()).isLessThan(Duration.ofMillis(duration));
                break;
            case 95:
                assertThat(lastTestStats.overall().sampleTime().perc95()).isLessThan(Duration.ofMillis(duration));
                break;
            case 99:
                assertThat(lastTestStats.overall().sampleTime().perc99()).isLessThan(Duration.ofMillis(duration));
                break;
            default:
                throw new IllegalArgumentException("Unsupported percentile: " + percentile);
        }
        resetThreadGroup();
    }
    @Step(value = "jmeter.assert.responseTimems", args = "duracionTest:int")
    public void setResponseTimems(Integer duration){

        if (lastTestStats == null) {
            throw new IllegalStateException(ERROR_MESSAGE);
        }

        assertThat(lastTestStats.overall().sampleTime().mean()).isLessThan(Duration.ofMillis(duration));
        resetThreadGroup();
    }


    @Step(value = "jmeter.assert.errors", args = "errors:int")
    public void setResponseErrors(Integer errors){

        if (lastTestStats == null) {
            throw new IllegalStateException(ERROR_MESSAGE);
        }
        assertThat(lastTestStats.overall().errorsCount()).isLessThan(errors);
        resetThreadGroup();
    }


}