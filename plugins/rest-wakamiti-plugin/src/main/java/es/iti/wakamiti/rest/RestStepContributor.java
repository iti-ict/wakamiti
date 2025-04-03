/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.rest;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.annotations.I18nResource;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.datatypes.Assertion;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.api.plan.DataTable;
import es.iti.wakamiti.api.plan.Document;
import es.iti.wakamiti.api.util.MatcherAssertion;
import es.iti.wakamiti.api.util.ResourceLoader;
import es.iti.wakamiti.api.util.http.oauth.GrantType;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

import static es.iti.wakamiti.api.matcher.CharSequenceLengthMatcher.length;


/**
 * Provides methods to configure and execute REST API requests and assertions.
 * It includes methods for setting request parameters, headers, authentication,
 * and executing various HTTP methods.
 *
 * @see RestSupport
 * @see StepContributor
 */
@Extension(provider = "es.iti.wakamiti", name = "rest-steps", version = "2.7")
@I18nResource("iti_wakamiti_wakamiti-rest")
public class RestStepContributor extends RestSupport implements StepContributor {

    private static final String USERNAME_PARAM = "username";
    private static final String PASSWORD_PARAM = "password";

    /**
     * Sets the content type for the request.
     * Accepted values are:
     * <ul>
     *   <li>{@code ANY}</li>
     *   <li>{@code TEXT}</li>
     *   <li>{@code JSON}</li>
     *   <li>{@code XML}</li>
     *   <li>{@code HTML}</li>
     *   <li>{@code URLENC}</li>
     *   <li>{@code BINARY}</li>
     *   <li>{@code MULTIPART}</li>
     * </ul>
     *
     * @param contentType the content type to be set.
     *
     * @see ContentType
     */
    @Step(value = "rest.define.contentType", args = "word")
    public void setContentType(String contentType) {
        specifications.add(request ->
                request.contentType(parseContentType(contentType)));
    }

    /**
     * Sets the base URL for the request.
     *
     * @param url the base URL to be set.
     */
    @Step(value = "rest.define.baseURL", args = "url")
    public void setBaseURL(URL url) {
        checkURL(url);
        this.baseURL = url;
    }

    /**
     * Sets the service path for the request.
     *
     * @param service the service path to be set.
     */
    @Step("rest.define.service")
    public void setService(String service) {
        this.path = (service.startsWith("/") ? service.substring(1) : service);
    }

    /**
     * Concatenates the subject to the service path.
     *
     * @param subject The entity identification
     *
     * @deprecated Use {@link
     * RestStepContributor#setPathParameter(String, String)} and {@link
     * RestStepContributor#setPathParameters(DataTable)} instead.
     */
    @Step("rest.define.subject")
    @Deprecated(forRemoval = true)
    public void setSubject(String subject) {
        this.subject = (subject.startsWith("/") ? subject.substring(1) : subject);
    }

    /**
     * Sets the collection of request parameters from a two-column table
     * in name-value format.
     *
     * @param dataTable the DataTable containing the request parameters.
     */
    @Step("rest.define.request.parameters")
    public void setRequestParameters(DataTable dataTable) {
        specifications.add(request -> request.params(tableToMap(dataTable)));
    }

    /**
     * Sets a single request parameter.
     *
     * @param name  the name of the parameter.
     * @param value the value of the parameter.
     */
    @Step(value = "rest.define.request.parameter", args = {"name:text", "value:text"})
    public void setRequestParameter(String name, String value) {
        specifications.add(request -> request.param(name, value));
    }

    /**
     * Sets the collection of query parameters from a two-column table
     * in name-value format.
     *
     * @param dataTable the DataTable containing the query parameters.
     */
    @Step("rest.define.query.parameters")
    public void setQueryParameters(DataTable dataTable) {
        specifications.add(request -> request.queryParams(tableToMap(dataTable)));
    }

    /**
     * Sets a single query parameter.
     *
     * @param name  the name of the parameter.
     * @param value the value of the parameter.
     */
    @Step(value = "rest.define.query.parameter", args = {"name:text", "value:text"})
    public void setQueryParameter(String name, String value) {
        specifications.add(request -> request.queryParam(name, value));
    }

    /**
     * Sets the collection of path parameters from a two-column table
     * in name-value format.
     *
     * @param dataTable the DataTable containing the path parameters.
     */
    @Step("rest.define.path.parameters")
    public void setPathParameters(DataTable dataTable) {
        specifications.add(request -> request.pathParams(tableToMap(dataTable)));
    }

    /**
     * Sets a single path parameter.
     *
     * @param name  the name of the parameter.
     * @param value the value of the parameter.
     */
    @Step(value = "rest.define.path.parameter", args = {"name:text", "value:text"})
    public void setPathParameter(String name, String value) {
        specifications.add(request -> request.pathParam(name, value));
    }

    /**
     * Sets the collection of headers from a two-column table in name-value
     * format.
     *
     * @param dataTable the DataTable containing the headers.
     */
    @Step("rest.define.headers")
    public void setHeaders(DataTable dataTable) {
        specifications.add(request -> tableToMap(dataTable)
                .forEach((k,v) -> header(request, k, v)));
    }

    /**
     * Sets a single header.
     *
     * @param name  the name of the header.
     * @param value the value of the header.
     */
    @Step(value = "rest.define.header", args = {"name:text", "value:text"})
    public void setHeader(String name, String value) {
        specifications.add(request -> header(request, name, value));
    }

    /**
     * Sets the default requests timeout.
     *
     * @param duration the duration of the timeout.
     */
    @Step("rest.define.timeout")
    public void setTimeout(Duration duration) {
        config(
                RestAssured.config()
                        .httpClient(HttpClientConfig.httpClientConfig()
                                .setParam("http.socket.timeout", (int) duration.toMillis())
                                .setParam("http.connection.timeout", (int) duration.toMillis()))
        );
    }

    /**
     * Sets a limit on HTTP response codes.
     * <p>
     * Whenever a REST call returns an HTTP code equal to or greater than this
     * value, the step is automatically marked as failed without checking any
     * other conditions.
     *
     * @param httpCodeAssertion the assertion for the HTTP response code.
     */
    @Step(value = "rest.define.http.code.assertion", args = "integer-assertion")
    public void setHttpCodeAssertion(Assertion<Integer> httpCodeAssertion) {
        this.httpCodeAssertion = MatcherAssertion.asMatcher(httpCodeAssertion);
    }

    /**
     * Sets the basic authentication credentials to be sent in the
     * {@code Authorization} header for the request.
     *
     * @param username the username for authentication.
     * @param password the password for authentication.
     */
    @Step(value = "rest.define.auth.basic", args = {"username:text", "password:text"})
    public void setBasicAuth(String username, String password) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Setting header [Authorization: {}:{}]", username, "*".repeat(password.length()));
        }
        authSpecification = Optional.of(request -> request.auth().preemptive().basic(username, password));
    }

    /**
     * Sets bearer token authentication to be sent in the {@code Authorization}
     * header for the request.
     *
     * @param token the bearer token for authentication.
     */
    @Step("rest.define.auth.bearer.token")
    public void setBearerAuth(String token) {
        LOGGER.trace("Setting header [Authorization: Bearer {}]", token);
        authSpecification = Optional.of(request -> request.auth().preemptive().oauth2(token));
    }

    /**
     * Sets bearer token authentication to be sent in the {@code Authorization}
     * header for the request.
     *
     * @param file the file containing the token.
     */
    @Step("rest.define.auth.bearer.token.file")
    public void setBearerAuthFile(File file) {
        assertFileExists(file);
        setBearerAuth(resourceLoader().readFileAsString(file).trim());
    }

    /**
     * Disables authentication for the request.
     */
    @Step("rest.define.auth.none")
    public void setNoneAuth() {
        authSpecification = Optional.of(request -> request.auth().none());
    }

    /**
     * Sets bearer token authentication to be sent in the {@code Authorization}
     * header, which is previously retrieved from the configured oauth2 service,
     * for the request.
     */
    @Step("rest.define.auth.bearer.default")
    public void setBearerDefault() {
        authSpecification = Optional.of(request -> request.auth().preemptive().oauth2(oauth2Provider.getAccessToken()));
    }

    /**
     * Sets bearer token authentication to be sent in the {@code Authorization}
     * header, which is previously retrieved from the configured oauth2 service,
     * using the indicated credentials, for the request.
     *
     * @param username the username for authentication.
     * @param password the password for authentication.
     */
    @Step(value = "rest.define.auth.bearer.password", args = {"username:text", "password:text"})
    public void setBearerAuthPassword(String username, String password) {
        oauth2Provider.configuration().type(GrantType.PASSWORD)
                .addParameter(USERNAME_PARAM, username)
                .addParameter(PASSWORD_PARAM, password);
        setBearerDefault();
    }

    /**
     * Sets bearer token authentication to be sent in the {@code Authorization}
     * header, which is previously retrieved from the configured oauth2 service,
     * using the indicated credentials, for the request.
     * <p>
     * Additional parameters supported by {@code Oauth2} can also be added using
     * a two-column table in name-value format.
     *
     * @param username the username for authentication.
     * @param password the password for authentication.
     * @param params   additional parameters for authentication.
     */
    @Step(value = "rest.define.auth.bearer.password.parameters", args = {"username:text", "password:text"})
    public void setBearerAuthPassword(String username, String password, DataTable params) {
        oauth2Provider.configuration().type(GrantType.PASSWORD)
                .addParameter(USERNAME_PARAM, username)
                .addParameter(PASSWORD_PARAM, password);
        tableToMap(params).forEach(oauth2Provider.configuration()::addParameter);
        setBearerDefault();
    }

    /**
     * Sets bearer token authentication to be sent in the {@code Authorization}
     * header, which is previously retrieved from the configured oauth2 service,
     * using client data, for the following requests.
     */
    @Step("rest.define.auth.bearer.client")
    public void setBearerAuthClient() {
        oauth2Provider.configuration().type(GrantType.CLIENT_CREDENTIALS);
        setBearerDefault();
    }

    /**
     * Sets bearer token authentication to be sent in the {@code Authorization}
     * header, which is previously retrieved from the configured oauth2 service,
     * using client data, for the following requests.
     * <p>
     * Additional parameters supported by {@code Oauth2} can also be added using
     * a two-column table in name-value format.
     *
     * @param params additional parameters for authentication.
     */
    @Step("rest.define.auth.bearer.client.parameters")
    public void setBearerAuthClient(DataTable params) {
        oauth2Provider.configuration().type(GrantType.CLIENT_CREDENTIALS);
        tableToMap(params).forEach(oauth2Provider.configuration()::addParameter);
        setBearerDefault();
    }

    /**
     * Sets the default subtype for multipart requests.
     * Available values are:
     * <ul>
     *   <li>{@code form-data}</li>
     *   <li>{@code alternative}</li>
     *   <li>{@code byteranges}</li>
     *   <li>{@code digest}</li>
     *   <li>{@code mixed}</li>
     *   <li>{@code parallel}</li>
     *   <li>{@code related}</li>
     *   <li>{@code report}</li>
     *   <li>{@code signed}</li>
     *   <li>{@code encrypted}</li>
     * </ul>
     *
     * @param subtype the subtype to set
     */
    @Step("rest.define.multipart.subtype")
    public void setMultipartSubtype(String subtype) {
        assertSubtype(subtype);
        config(
                RestAssured.config().multiPartConfig(
                        RestAssured.config().getMultiPartConfig().defaultSubtype(subtype)
                )
        );
    }

    /**
     * Sets the default filename for multipart content.
     *
     * @param name the filename to set
     */
    @Step("rest.define.multipart.filename")
    public void setFilename(String name) {
        config(
                RestAssured.config().multiPartConfig(
                        RestAssured.config().getMultiPartConfig().defaultFileName(name)
                )
        );
    }

    /**
     * Attaches the document content as a file to the multipart {@code form-data}
     * request.
     *
     * @param name     the name of the multipart field
     * @param document the content to be attached
     *
     * @throws IOException if an I/O error occurs
     */
    @Step(value = "rest.define.attached.data", args = "name:text")
    public void setAttachedFile(String name, Document document) throws IOException {
        String ext = Optional.ofNullable(document.getContentType()).orElse("txt");
        ContentType mimeType = ContentType.fromContentType(
                ResourceLoader.contentTypeFromExtension.get(ext).getMimeType());

        File tempFile = new File(System.getProperty("java.io.tmpdir"),
                RestAssured.config().getMultiPartConfig().defaultFileName() + "." + ext);
        tempFile.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            IOUtils.copy(new ByteArrayInputStream(document.getContent().getBytes(StandardCharsets.UTF_8)), out);
        }

        specifications.add(request ->
                request.contentType("multipart/" + RestAssured.config().getMultiPartConfig().defaultSubtype()));
        specifications.add(request ->
                request.multiPart(name, tempFile, mimeType.getContentTypeStrings()[0])
        );
    }

    /**
     * Attaches a file to the multipart {@code form-data} request.
     *
     * @param name the name of the multipart field
     * @param file the file to attach
     */
    @Step(value = "rest.define.attached.file", args = {"name:text", "file"})
    public void setAttachedFile(String name, File file) {
        assertFileExists(file);

        resourceLoader();
        ContentType mimeType = ContentType.fromContentType(
                ResourceLoader.getContentType(file).getMimeType());

        specifications.add(request ->
                request.contentType("multipart/" + RestAssured.config().getMultiPartConfig().defaultSubtype()));
        specifications.add(request ->
                request.multiPart(name, file, mimeType.getContentTypeStrings()[0])
        );
    }

    /**
     * Sets the collection of form parameters from a two-column table in
     * name-value format.
     * <p>
     * This step will also force the request to use the
     * {@code application/x-www-form-urlencoded} content type.
     *
     * @param table the table of form parameters
     */
    @Step(value = "rest.define.form.parameters")
    public void setFormParameters(DataTable table) {
        specifications.add(request -> request.contentType(ContentType.URLENC));
        specifications.add(request -> request.formParams(tableToMap(table)));
    }

    @Step(value = "rest.define.form.parameter", args = {"name:text", "value:text"})
    public void setFormParameter(String name, String value) {
        specifications.add(request -> request.contentType(ContentType.URLENC));
        specifications.add(request -> request.formParam(name, value));
    }

    @Step("rest.execute.GET.query")
    public Object executeGetQuery() {
        executeRequest(RequestSpecification::get);
        return parsedResponse();
    }

    @Step("rest.execute.GET.subject")
    public Object executeGetSubject() {
        return executeGetQuery();
    }

    @Step("rest.execute.DELETE.subject")
    public Object executeDeleteSubject() {
        executeRequest(RequestSpecification::delete);
        return parsedResponse();
    }

    @Step("rest.execute.PUT.subject.from.document")
    public Object executePutSubjectUsingDocument(Document document) {
        executeRequest(RequestSpecification::put, document.getContent());
        return parsedResponse();
    }

    @Step("rest.execute.PUT.subject.from.file")
    public Object executePutSubjectUsingFile(File file) {
        assertFileExists(file);
        executeRequest(RequestSpecification::put, readFile(file));
        return parsedResponse();
    }

    @Step("rest.execute.PUT.subject.empty")
    public Object executePutSubject() {
        executeRequest(RequestSpecification::put);
        return parsedResponse();
    }

    @Step("rest.execute.PATCH.subject.from.document")
    public Object executePatchSubjectUsingDocument(Document document) {
        executeRequest(RequestSpecification::patch, document.getContent());
        return parsedResponse();
    }

    @Step("rest.execute.PATCH.subject.from.file")
    public Object executePatchSubjectUsingFile(File file) {
        assertFileExists(file);
        executeRequest(RequestSpecification::patch, readFile(file));
        return parsedResponse();
    }

    @Step("rest.execute.PATCH.subject.empty")
    public Object executePatchSubject() {
        executeRequest(RequestSpecification::patch);
        return parsedResponse();
    }

    @Step("rest.execute.POST.subject.from.file")
    public Object executePostSubjectUsingFile(File file) {
        assertFileExists(file);
        executeRequest(RequestSpecification::post, readFile(file));
        return parsedResponse();
    }

    @Step("rest.execute.POST.subject.from.document")
    public Object executePostSubjectUsingDocument(Document document) {
        executeRequest(RequestSpecification::post, document.getContent());
        return parsedResponse();
    }

    @Step("rest.execute.POST.subject.empty")
    public Object executePostSubject() {
        executeRequest(RequestSpecification::post);
        return parsedResponse();
    }

    @Step("rest.execute.POST.data.from.document")
    public Object executePostDataUsingDocument(Document document) {
        return executePostSubjectUsingDocument(document);
    }

    @Step("rest.execute.POST.data.from.file")
    public Object executePostDataUsingFile(File file) {
        executePostSubjectUsingFile(file);
        return parsedResponse();
    }

    @Step("rest.execute.POST.data.empty")
    public Object executePostData() {
        return executePostSubject();
    }

    @Step("rest.execute.DELETE.data.from.document")
    public Object executeDeleteDataUsingDocument(Document document) {
        executeRequest(RequestSpecification::delete, document.getContent());
        return parsedResponse();
    }

    @Step("rest.execute.DELETE.data.from.file")
    public Object executeDeleteDataUsingFile(File file) {
        assertFileExists(file);
        executeRequest(RequestSpecification::delete, readFile(file));
        return parsedResponse();
    }

    @Step("rest.assert.response.body.strict.from.document")
    public void assertBodyStrictComparison(Document document) {
        assertResponseNotNull();
        assertContentIs(document, MatchMode.STRICT);
    }

    @Step("rest.assert.response.body.strict.from.document.any-order")
    public void assertBodyStrictComparisonAnyOrder(Document document) {
        assertResponseNotNull();
        assertContentIs(document, MatchMode.STRICT_ANY_ORDER);
    }

    @Step("rest.assert.response.body.loose.from.document")
    public void assertBodyLooseComparison(Document document) {
        assertResponseNotNull();
        assertContentIs(document, MatchMode.LOOSE);
    }

    @Step("rest.assert.response.body.strict.from.file")
    public void assertStrictFileContent(File file) {
        assertResponseNotNull();
        assertContentIs(file, MatchMode.STRICT);
    }

    @Step("rest.assert.response.body.strict.from.file.any-order")
    public void assertStrictFileContentAnyOrder(File file) {
        assertResponseNotNull();
        assertContentIs(file, MatchMode.STRICT_ANY_ORDER);
    }

    @Step("rest.assert.response.body.loose.from.file")
    public void assertLooseFileContent(File file) {
        assertResponseNotNull();
        assertContentIs(file, MatchMode.LOOSE);
    }

    @Step(value = "rest.assert.response.HTTP.code", args = "integer-assertion")
    public void assertHttpCode(Assertion<Integer> assertion) {
        assertResponseNotNull();
        validatableResponse.statusCode(MatcherAssertion.asMatcher(assertion));
    }

    @Step(value = "rest.assert.response.body.contentType", args = "word")
    public void assertResponseContentType(String contentType) {
        assertResponseNotNull();
        validatableResponse.contentType(parseContentType(contentType));
    }

    @Step(value = "rest.assert.response.body.length", args = {"matcher:integer-assertion"})
    public void assertResponseLength(Assertion<Integer> assertion) {
        assertResponseNotNull();
        validatableResponse.body(length(MatcherAssertion.asMatcher(assertion)));
    }

    @Step(value = "rest.assert.response.body.header.text", args = {"name:word", "matcher:text-assertion"})
    public void assertResponseHeaderAsText(String name, Assertion<String> assertion) {
        assertResponseNotNull();
        validatableResponse.header(name, MatcherAssertion.asMatcher(assertion));
    }

    @Step(value = "rest.assert.response.body.header.integer", args = {"name:word", "matcher:integer-assertion"})
    public void assertResponseHeaderAsInteger(String name, Assertion<Integer> assertion) {
        assertResponseNotNull();
        validatableResponse.header(name, Integer::parseInt, MatcherAssertion.asMatcher(assertion));
    }

    @Step(value = "rest.assert.response.body.header.decimal", args = {"name:word", "matcher:decimal-assertion"})
    public void assertResponseHeaderAsDecimal(String name, Assertion<BigDecimal> assertion) {
        assertResponseNotNull();
        validatableResponse.header(name, BigDecimal::new, MatcherAssertion.asMatcher(assertion));
    }

    @Step(value = "rest.assert.response.body.fragment.text", args = {"fragment:text", "matcher:text-assertion"})
    public void assertBodyFragmentAsText(String fragment, Assertion<String> assertion) {
        assertResponseNotNull();
        assertBodyFragment(fragment, assertion, String.class);
    }

    @Step(value = "rest.assert.response.body.fragment.integer", args = {"fragment:text", "matcher:integer-assertion"})
    public void assertBodyFragmentAsInteger(String fragment, Assertion<Integer> assertion) {
        assertResponseNotNull();
        assertBodyFragment(fragment, assertion, Integer.class);
    }

    @Step(value = "rest.assert.response.body.fragment.decimal", args = {"fragment:text", "matcher:decimal-assertion"})
    public void assertBodyFragmentAsDecimal(String fragment, Assertion<BigDecimal> assertion) {
        assertResponseNotNull();
        assertBodyFragment(fragment, assertion, BigDecimal.class);
    }

    @Step(value = "rest.assert.response.body.fragment.strict.from.document", args = {"fragment:text"})
    public void assertBodyFragmentStrict(String fragment, Document document) {
        assertResponseNotNull();
        assertBodyFragment(fragment, document.getContent(), MatchMode.STRICT);
    }

    @Step(value = "rest.assert.response.body.fragment.strict.from.file", args = {"fragment:text", "file"})
    public void assertBodyFragmentStrict(String fragment, File file) {
        assertResponseNotNull();
        assertBodyFragment(fragment, readFile(file), MatchMode.STRICT);
    }

    @Step(value = "rest.assert.response.body.fragment.strict.from.document.any-order", args = {"fragment:text"})
    public void assertBodyFragmentStrictAnyOrder(String fragment, Document document) {
        assertResponseNotNull();
        assertBodyFragment(fragment, document.getContent(), MatchMode.STRICT_ANY_ORDER);
    }

    @Step(value = "rest.assert.response.body.fragment.strict.from.file.any-order", args = {"fragment:text", "file"})
    public void assertBodyFragmentStrictAnyOrder(String fragment, File file) {
        assertResponseNotNull();
        assertBodyFragment(fragment, readFile(file), MatchMode.STRICT_ANY_ORDER);
    }

    @Step(value = "rest.assert.response.body.fragment.loose.from.document", args = {"fragment:text"})
    public void assertBodyFragmentLoose(String fragment, Document document) {
        assertResponseNotNull();
        assertBodyFragment(fragment, document.getContent(), MatchMode.LOOSE);
    }

    @Step(value = "rest.assert.response.body.fragment.loose.from.file", args = {"fragment:text", "file"})
    public void assertBodyFragmentLoose(String fragment, File file) {
        assertResponseNotNull();
        assertBodyFragment(fragment, readFile(file), MatchMode.LOOSE);
    }

    @Step("rest.assert.response.body.schema.from.document")
    public void assertBodyContentSchema(Document document) {
        assertResponseNotNull();
        assertContentSchema(document.getContent());
    }


    @Step(value = "rest.assert.response.body.schema.from.file")
    public void assertBodyContentSchema(File file) {
        assertResponseNotNull();
        assertFileExists(file);
        assertContentSchema(readFile(file));
    }

}