/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.plugins.jmeter;

import static org.assertj.core.api.Assertions.assertThat;
import static us.abstracta.jmeter.javadsl.JmeterDsl.*;

import es.iti.wakamiti.api.plan.DataTable;
import es.iti.wakamiti.api.plan.Document;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.entity.ContentType;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import us.abstracta.jmeter.javadsl.core.DslTestPlan;
import us.abstracta.jmeter.javadsl.core.TestPlanStats;
import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.annotations.I18nResource;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.api.util.WakamitiLogger;
import org.slf4j.Logger;
import us.abstracta.jmeter.javadsl.core.postprocessors.DslJsonExtractor;
import us.abstracta.jmeter.javadsl.core.threadgroups.DslDefaultThreadGroup;


@Extension(provider = "es.iti.wakamiti", name = "jmeter", version = "1.1")
@I18nResource("es_iti_wakamiti_jmeter")
public class JMeterStepContributor implements StepContributor {

    private final Logger logger = WakamitiLogger.forClass(JMeterStepContributor.class);

    DslDefaultThreadGroup threadGroup = threadGroup();

    private Boolean escenarioBasico = true;
    protected String baseUrl;
    public TestPlanStats lastTestStats;
    private boolean influxDBEnabled;
    private boolean csvEnabled;
    private boolean htmlEnabled;
    private String influxDBUrl;
    private String csvPath;
    private String htmlPath;
    private String username;
    private String password;

    public void configureOutputOptions(boolean influxDBEnabled, boolean csvEnabled, boolean htmlEnabled, String influxDBUrl, String csvPath, String htmlPath) {
        this.influxDBEnabled = influxDBEnabled;
        this.csvEnabled = csvEnabled;
        this.htmlEnabled = htmlEnabled;
        this.influxDBUrl = influxDBUrl;
        this.csvPath = csvPath;
        this.htmlPath = htmlPath;
    }
    public void setAuthCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }
    private void resetThreadGroup() {
        this.threadGroup = null;
        this.escenarioBasico = true;
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
    private void configurarListeners(){

        if (influxDBEnabled) {
            threadGroup.children(influxDbListener("http://localhost:8086/write?db=jmeter"));
        }

        if (csvEnabled) {
            threadGroup.children(jtlWriter(csvPath));
        }

        if (htmlEnabled) {
            threadGroup.children(htmlReporter(htmlPath));
        }
    }

    private void ejecutarPruebas() throws IOException {

        if(escenarioBasico)
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

    @Step(value = "jmeter.define.csvinput", args = { "fichero:text" })
    public void setCSVInput(String fichero) throws IOException {

        List<String> columnNames = getCSVHeaders(fichero);
        String requestBody = buildRequestBody(columnNames);

        threadGroup.children(csvDataSet(testResource(fichero)));
        threadGroup.children(
                httpSampler(baseUrl+"/login")
                        .post(requestBody,
                                ContentType.APPLICATION_JSON)
        );
        escenarioBasico = false;
    }
    @Step(value = "jmeter.define.csvinputvariables", args = { "fichero:text" })
    public void setCSVInputVariables(String fichero, DataTable variables) throws IOException {

        List<String> csvHeaders = getCSVHeaders(fichero);

        List<String> filtroVariables = Arrays.stream(variables.getValues())
                .map(row -> row[0])
                .filter(csvHeaders::contains)
                .collect(Collectors.toList());
        String requestBody = buildRequestBody(filtroVariables);

        threadGroup.children(csvDataSet(testResource(fichero)));
        threadGroup.children(
                httpSampler(baseUrl+"/login")
                        .post(requestBody,
                                ContentType.APPLICATION_JSON)
        );
        escenarioBasico = false;
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
    public void llamadaGet(String service) {
        threadGroup.children(httpSampler(baseUrl+service));
        escenarioBasico = false;
    }

    @Step(value = "jmeter.define.put", args = { "service:text" })
    public void llamadaPut(String service, Document body) {

        String requestBody = body.getContent();
        threadGroup.children(
                httpSampler(baseUrl+service)
                        .method(HTTPConstants.PUT)
                        .contentType(ContentType.APPLICATION_JSON)
                        .body(requestBody)
        );
        escenarioBasico = false;
    }
    @Step(value = "jmeter.define.post", args = { "service:text" })
    public void llamadaPost(String service, Document body) {

        String requestBody = body.getContent();
        threadGroup.children(
                httpSampler(baseUrl+service)
                        .method(HTTPConstants.POST)
                        .contentType(ContentType.APPLICATION_JSON)
                        .body(requestBody)
        );
        escenarioBasico = false;
    }
    @Step(value = "jmeter.define.connectiontimeout", args = { "duracion:int" })
    public void setConnectionTimeout(Integer duracion) {

       threadGroup.children(httpDefaults().connectionTimeout(Duration.ofSeconds(duracion)));
    }
    @Step(value = "jmeter.define.responsetimeout", args = { "duracion:int" })
    public void setResponseTimeout(Integer duracion) {

        threadGroup.children(httpDefaults().responseTimeout(Duration.ofMinutes(duracion)));
    }
    @Step(value = "jmeter.define.resources")
    public void downloadEmbeddedResources() {

        threadGroup.children(httpDefaults().downloadEmbeddedResources());
    }
    @Step(value = "jmeter.define.proxy", args = { "URL:text" })
    public void setResponseTimeout(String URL) {

        threadGroup.children(httpDefaults().proxy(URL));
    }

    @Step(value = "jmeter.define.responsecode", args = {"responseCode:int"})
    public void setResponseCode(Integer responseCode) {
        String script = String.format("if (prev.responseCode == '%d') { prev.successful = true }", responseCode);
        threadGroup.children(jsr223PostProcessor(script));
    }

    @Step(value = "jmeter.test.jmxfile", args = { "archivo:text" })
    public void ejecutarPruebaJMX(String archivo) throws IOException {

        lastTestStats = DslTestPlan.fromJmx(archivo).run();

    }
    /*
    @Step(value = "jmeter.extract.regex", args = {"variableName:text", "regex:text"})
    public void extractRegex(String variableName, String regex){
        threadGroup.children(regexExtractor(variableName,regex));
        escenarioBasico = false;
    }
    @Step(value = "jmeter.extract.boundaries", args = {"variableName:text", "leftBoundarie:text"," rightBoundarie:text"})
    public void extractBoundaries(String variableName, String leftBoundarie, String rightBoundarie){
        threadGroup.children(boundaryExtractor(variableName,leftBoundarie,rightBoundarie));
        escenarioBasico = false;
    }
    @Step(value = "jmeter.extract.json", args = {"variableName:text", "jsonPath:text"})
    public void extractJson(String variableName, String jsonPath){
        threadGroup.children(jsonExtractor(variableName,jsonPath).queryLanguage(DslJsonExtractor.JsonQueryLanguage.JSON_PATH));
        escenarioBasico = false;
    }
    */
    @Step(value = "jmeter.test.foamtest")
    public void ejecutarPruebaHumo() throws IOException {

        threadGroup = threadGroup(1, 1);

        configurarListeners();

        ejecutarPruebas();

    }

    @Step(value = "jmeter.test.loadtest", args = {"usuarios:int", "duracion:int"})
    public void ejecutarPruebaCarga(Integer usuarios, Integer duracion) throws IOException {


         threadGroup = threadGroup.rampToAndHold(usuarios, Duration.ofSeconds(0), Duration.ofMinutes(duracion));

         configurarListeners();

         ejecutarPruebas();

    }
    @Step(value = "jmeter.test.limitetest", args = {"usuarios:int", "incrementoUsuarios:int", "maxUsuarios:int", "duracion:int"})
    public void ejecutarPruebaLimiteOperativo(Integer usuarios, Integer incrementoUsuarios, Integer maxUsuarios, Integer duracion) throws IOException {

        int usuariosActuales = usuarios;

        while (usuariosActuales <= maxUsuarios) {
            threadGroup= threadGroup.rampTo(usuariosActuales, Duration.ofMinutes(duracion));
            usuariosActuales += incrementoUsuarios;
        }

        configurarListeners();

        ejecutarPruebas();

    }
    @Step(value = "jmeter.test.stresstest", args = {"usuarios:int", "incrementoUsuarios:int", "maxUsuarios:int", "duracion:int"})
    public void ejecutarPruebaEstres(Integer usuarios, Integer incrementoUsuarios, Integer maxUsuarios, Integer duracion) throws IOException {

        // Calcula el número total de pasos necesarios para llegar de 'usuarios' a 'maxUsuarios' en incrementos de 'incrementoUsuarios'
        int totalPasos = (maxUsuarios - usuarios) / incrementoUsuarios;
        // Crea el grupo de hilos con los usuarios iniciales, incrementando usuarios cada periodo de tiempo especificado
        int usuariosActuales = usuarios;
        for (int paso = 0; paso <= totalPasos; paso++) {
            threadGroup = threadGroup.rampToAndHold(usuariosActuales, Duration.ofSeconds(10), Duration.ofMinutes(duracion));
            usuariosActuales += incrementoUsuarios;
        }

        //Disminuir a 0 'usuarios'
        threadGroup = threadGroup.rampTo(0, Duration.ofSeconds(20));

        configurarListeners();

        ejecutarPruebas();

    }
    @Step(value = "jmeter.test.peaktest", args = {"numeroPicos:int", "usuariosPico:int", "usuariosFueraPico:int", "duracion:int"})
    public void ejecutarPruebaPico(Integer numeroPicos, Integer usuariosPico, Integer usuariosFueraPico, Integer duracion) throws IOException {

        threadGroup = threadGroup.rampToAndHold(usuariosFueraPico, Duration.ofSeconds(20), Duration.ofMinutes(duracion));

        for (int i = 0; i < numeroPicos; i++) {
            // Sube al pico
            threadGroup = threadGroup.rampTo(usuariosPico, Duration.ofSeconds(20));
            // Baja al número de usuarios fuera del pico
            threadGroup = threadGroup.rampTo(usuariosFueraPico, Duration.ofSeconds(20));
            // Mantiene el número de usuarios fuera del pico
            threadGroup = threadGroup.holdFor(Duration.ofMinutes(duracion));
        }

        //Disminuir a 0 'usuarios'
        threadGroup = threadGroup.rampTo(0, Duration.ofSeconds(20));

        configurarListeners();

        ejecutarPruebas();

    }

    @Step(value = "jmeter.assert.percentil", args = {"percentil:int", "duracionTest:int"})
    public void setPruebaPercentil(Integer percentil, Integer duracionTest) throws IOException {

        if (lastTestStats == null) {
            throw new IllegalStateException("No hay resultados de pruebas almacenados para verificar el percentil 99.");
        }

        switch (percentil){
            case 50:
                assertThat(lastTestStats.overall().sampleTime().median()).isLessThan(Duration.ofSeconds(duracionTest));
                break;
            case 90:
                assertThat(lastTestStats.overall().sampleTime().perc90()).isLessThan(Duration.ofSeconds(duracionTest));
                break;
            case 95:
                assertThat(lastTestStats.overall().sampleTime().perc95()).isLessThan(Duration.ofSeconds(duracionTest));
                break;
            case 99:
                assertThat(lastTestStats.overall().sampleTime().perc99()).isLessThan(Duration.ofSeconds(duracionTest));
                break;
            default:
                throw new IllegalArgumentException("Percentil no soportado: " + percentil);
        }
        resetThreadGroup();
    }
    @Step(value = "jmeter.assert.responseTime", args = "duracionTest:int")
    public void setPruebaResponseTime(Integer duracionTest){

        if (lastTestStats == null) {
            throw new IllegalStateException("No hay resultados de pruebas almacenados para verificar el tiempos de respuesta.");
        }

        assertThat(lastTestStats.overall().sampleTime().mean()).isLessThan(Duration.ofSeconds(duracionTest));
        resetThreadGroup();
    }

    @Step(value = "jmeter.assert.errors", args = "errores:int")
    public void setPruebaResponseErrors(Integer errores){

        if (lastTestStats == null) {
            throw new IllegalStateException("No hay resultados de pruebas almacenados para verificar el tiempos de respuesta.");
        }
        assertThat(lastTestStats.overall().errorsCount()).isLessThan(errores);
        resetThreadGroup();
    }


}