/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.rest;


import es.iti.wakamiti.api.WakamitiRunContext;
import es.iti.wakamiti.api.WakamitiStepRunContext;
import es.iti.wakamiti.api.annotations.SetUp;
import es.iti.wakamiti.rest.oauth.GrantType;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.annotations.I18nResource;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.datatypes.Assertion;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.api.plan.DataTable;
import es.iti.wakamiti.api.plan.Document;
import es.iti.wakamiti.api.util.MatcherAssertion;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Optional;

import static es.iti.wakamiti.rest.matcher.CharSequenceLengthMatcher.length;


@I18nResource("iti_wakamiti_wakamiti-rest")
@Extension(provider =  "es.iti.wakamiti", name = "rest-steps")
public class RestStepContributor extends RestSupport implements StepContributor {

    private static final String USERNAME_PARAM = "username";
    private static final String PASSWORD_PARAM = "password";



    @Step(value = "rest.define.contentType", args = "word")
    public void setContentType(String contentType) {
        specifications.add(request ->
                request.contentType(parseContentType(contentType)));
    }


    @Step(value = "rest.define.baseURL", args = "url")
    public void setBaseURL(URL url) {
        this.baseURL = url;
    }


    @Step("rest.define.service")
    public void setService(String service) {
        this.path = (service.startsWith("/") ? service.substring(1) : service);
    }


    @Step("rest.define.subject")
    public void setSubject(String subject) {
        this.subject = (subject.startsWith("/") ? subject.substring(1) : subject);
    }


    @Step("rest.define.request.parameters")
    public void setRequestParameters(DataTable dataTable) {
        specifications.add(request -> request.params(tableToMap(dataTable)));
    }


    @Step(value = "rest.define.request.parameter", args = {"name:text", "value:text"})
    public void setRequestParameter(String name, String value) {
        specifications.add(request -> request.param(name, value));
    }

    @Step("rest.define.query.parameters")
    public void setQueryParameters(DataTable dataTable) {
        specifications.add(request -> request.queryParams(tableToMap(dataTable)));
    }


    @Step(value = "rest.define.query.parameter", args = {"name:text", "value:text"})
    public void setQueryParameter(String name, String value) {
        specifications.add(request -> request.queryParam(name, value));
    }


    @Step("rest.define.path.parameters")
    public void setPathParameters(DataTable dataTable) {
        specifications.add(request -> request.pathParams(tableToMap(dataTable)));
    }


    @Step(value = "rest.define.path.parameter", args = {"name:text", "value:text"})
    public void setPathParameter(String name, String value) {
        specifications.add(request -> request.pathParam(name, value));
    }


    @Step("rest.define.headers")
    public void setHeaders(DataTable dataTable) {
        specifications.add(request -> request.headers(tableToMap(dataTable)));
    }


    @Step(value = "rest.define.header", args = {"name:text", "value:text"})
    public void setHeader(String name, String value) {
        specifications.add(request -> request.header(name, value));
    }


    @Step("rest.define.timeout.millis")
    public void setTimeoutInMillis(Integer millis) {
        config(
                RestAssured.config()
                        .httpClient(HttpClientConfig.httpClientConfig()
                                .setParam("http.socket.timeout", millis)
                                .setParam("http.connection.timeout", millis))
        );
    }


    @Step("rest.define.timeout.secs")
    public void setTimeoutInSecs(Integer secs) {
        setTimeoutInMillis(secs * 1000);
    }


    @Step(value = "rest.define.failure.http.code.assertion", args = "integer-assertion")
    public void setFailureHttpCodeAssertion(Assertion<Integer> httpCodeAssertion) {
        this.failureHttpCodeAssertion = MatcherAssertion.asMatcher(httpCodeAssertion);
    }


    @Step(value = "rest.define.auth.basic", args = {"username:text", "password:text"})
    public void setBasicAuth(String username, String password) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Setting header [Authorization: {}:{}]", username, "*".repeat(password.length()));
        }
        authSpecification = Optional.of(request -> request.auth().preemptive().basic(username, password));
    }


    @Step("rest.define.auth.bearer.token")
    public void setBearerAuth(String token) {
        LOGGER.trace("Setting header [Authorization: Bearer {}]", token);
        authSpecification = Optional.of(request -> request.auth().preemptive().oauth2(token));
    }

    @Step("rest.define.auth.bearer.token.file")
    public void setBearerAuthFile(File file) {
        assertFileExists(file);
        setBearerAuth(WakamitiRunContext.current().resourceLoader().readFileAsString(file).trim());
    }

    @Step("rest.define.auth.none")
    public void setNoneAuth() {
        authSpecification = Optional.of(request -> request.auth().none());
    }

    @Step("rest.define.auth.bearer.default")
    public void setBearerDefault() {
        authSpecification = Optional.of(request -> request.auth().preemptive().oauth2(retrieveOauthToken()));
    }

    @Step(value = "rest.define.auth.bearer.password", args = {"username:text", "password:text"})
    public void setBearerAuthPassword(String username, String password) {
        oauth2ProviderConfig.type(GrantType.PASSWORD)
                .addParameter(USERNAME_PARAM, username)
                .addParameter(PASSWORD_PARAM, password);
        setBearerDefault();
    }

    @Step(value = "rest.define.auth.bearer.password.parameters", args = {"username:text", "password:text"})
    public void setBearerAuthPassword(String username, String password, DataTable params) {
        oauth2ProviderConfig.type(GrantType.PASSWORD)
                .addParameter(USERNAME_PARAM, username)
                .addParameter(PASSWORD_PARAM, password);
        tableToMap(params).forEach(oauth2ProviderConfig::addParameter);
        setBearerDefault();
    }

    @Step("rest.define.auth.bearer.client")
    public void setBearerAuthClient() {
        oauth2ProviderConfig.type(GrantType.CLIENT_CREDENTIALS);
        setBearerDefault();
    }

    @Step("rest.define.auth.bearer.client.parameters")
    public void setBearerAuthClient(DataTable params) {
        oauth2ProviderConfig.type(GrantType.CLIENT_CREDENTIALS);
        tableToMap(params).forEach(oauth2ProviderConfig::addParameter);
        setBearerDefault();
    }


    @Step("rest.define.multipart.subtype")
    public void setMultipartSubtype(String subtype) {
        assertSubtype(subtype);
        config(
                RestAssured.config().multiPartConfig(
                        RestAssured.config().getMultiPartConfig().defaultSubtype(subtype)
                )
        );
    }


    @Step(value = "rest.define.attached.data", args = "name:text")
    public void setAttachedFile(String name, Document document) {
        ContentType mimeType = Optional.ofNullable(document.getContentType())
                .map(ContentTypeHelper.contentTypeFromExtension::get)
                .orElse(ContentType.TEXT);

        specifications.add(request ->
                request.multiPart(
                        name,
                        document.getContent(),
                        mimeType.getContentTypeStrings()[0])
        );
    }


    @Step(value = "rest.define.attached.file", args = {"name:text", "file"})
    public void setAttachedFile(String name, File file) {
        assertFileExists(file);
        ContentType mimeType = Optional.of(file.getName())
                .map(FileUtils::getExtension)
                .map(ContentTypeHelper.contentTypeFromExtension::get)
                .orElse(ContentType.TEXT);

        specifications.add(request ->
                request.multiPart(
                        name,
                        file.getName(),
                        readFile(file),
                        mimeType.getContentTypeStrings()[0])
        );
    }


    @Step("rest.execute.GET.query")
    public Object executeGetQuery() {
        executeRequest(RequestSpecification::get);
        return parsedResponse();
    }

    @Step("rest.execute.GET.subject")
    public Object executeGetSubject() {
        executeRequest(RequestSpecification::get);
        return parsedResponse();
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
        executeRequest(RequestSpecification::post, document.getContent());
        return parsedResponse();
    }

    @Step("rest.execute.POST.data.from.file")
    public Object executePostDataUsingFile(File file) {
        executePostSubjectUsingFile(file);
        return parsedResponse();
    }

    @Step("rest.execute.POST.data.empty")
    public Object executePostData() {
        executeRequest(RequestSpecification::post);
        return parsedResponse();
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