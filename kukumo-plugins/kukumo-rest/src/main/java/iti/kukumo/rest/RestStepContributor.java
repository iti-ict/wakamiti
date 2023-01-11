/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.rest;


import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import iti.commons.jext.Extension;
import iti.kukumo.api.annotations.I18nResource;
import iti.kukumo.api.annotations.Step;
import iti.kukumo.api.datatypes.Assertion;
import iti.kukumo.api.extensions.StepContributor;
import iti.kukumo.api.plan.DataTable;
import iti.kukumo.api.plan.Document;
import iti.kukumo.api.util.MatcherAssertion;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Optional;

import static iti.kukumo.rest.matcher.CharSequenceLengthMatcher.length;


@I18nResource("iti_kukumo_kukumo-rest")
@Extension(provider = "iti.kukumo", name = "rest-steps")
public class RestStepContributor extends RestSupport implements StepContributor {


    @Step(value = "rest.define.contentType", args = "word")
    public void setContentType(String contentType) {
        specifications.add(request ->
                request.contentType(parseContentType(contentType))
                        .accept(parseContentType(contentType))
        );
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
        if (LOGGER.isTraceEnabled())
            LOGGER.trace("Setting header [Authorization: {}:{}]", username, "*".repeat(password.length()));
        specifications.add(request -> request.auth().preemptive().basic(username, password));
    }


    @Step("rest.define.auth.bearer.token")
    public void setBearerAuth(String token) {
        LOGGER.trace("Setting header [Authorization: Bearer {}]", token);
        specifications.add(request -> request.auth().preemptive().oauth2(token));
    }


    @Step("rest.define.auth.bearer.token.file")
    public void setBearerAuthFile(File file) {
        assertFileExists(file);
        setBearerAuth(resourceLoader.readFileAsString(file).trim());
    }


    @Step(value = "rest.define.auth.bearer.password", args = {"username:text", "password:text"})
    public void setBearerAuthPassword(String username, String password) {
        String token = retrieveOauthToken(request -> request
                        .formParam(GRANT_TYPE_PARAM, "password")
                        .formParam("username", username)
                        .formParam("password", password),
                username, password
        );
        setBearerAuth(token);
    }


    @Step("rest.define.auth.bearer.client")
    public void setBearerAuthClient() {
        String token = retrieveOauthToken(request -> request
                .formParam(GRANT_TYPE_PARAM, "client_credentials")
        );
        setBearerAuth(token);
    }


    @Step("rest.define.auth.bearer.code")
    public void setBearerAuthCode(String code) {
        String token = retrieveOauthToken(request -> request
                        .formParam(GRANT_TYPE_PARAM, "authorization_code")
                        .formParam("code", code),
                code
        );
        setBearerAuth(token);
    }


    @Step("rest.define.auth.bearer.code.file")
    public void setBearerAuthCodeFile(File file) {
        assertFileExists(file);
        setBearerAuthCode(resourceLoader.readFileAsString(file).trim());
    }


    @Step("rest.define.auth.none")
    public void setNoneAuth() {
        specifications.add(request -> request.auth().none());
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
                        resourceLoader.readFileAsString(file),
                        mimeType.getContentTypeStrings()[0])
        );
    }


    @Step("rest.execute.GET.query")
    public void executeGetQuery() {
        executeRequest(RequestSpecification::get);
    }

    @Step("rest.execute.GET.subject")
    public void executeGetSubject() {
        executeRequest(RequestSpecification::get);
    }

    @Step("rest.execute.DELETE.subject")
    public void executeDeleteSubject() {
        executeRequest(RequestSpecification::delete);
    }

    @Step("rest.execute.PUT.subject.from.document")
    public void executePutSubjectUsingDocument(Document document) {
        executeRequest(RequestSpecification::put, document.getContent());
    }

    @Step("rest.execute.PUT.subject.from.file")
    public void executePutSubjectUsingFile(File file) {
        assertFileExists(file);
        executeRequest(RequestSpecification::put, resourceLoader.readFileAsString(file));
    }

    @Step("rest.execute.PUT.subject.empty")
    public void executePutSubject() {
        executeRequest(RequestSpecification::put);
    }

    @Step("rest.execute.PATCH.subject.from.document")
    public void executePatchSubjectUsingDocument(Document document) {
        executeRequest(RequestSpecification::patch, document.getContent());
    }

    @Step("rest.execute.PATCH.subject.from.file")
    public void executePatchSubjectUsingFile(File file) {
        assertFileExists(file);
        executeRequest(RequestSpecification::patch, resourceLoader.readFileAsString(file));
    }

    @Step("rest.execute.PATCH.subject.empty")
    public void executePatchSubject() {
        executeRequest(RequestSpecification::patch);
    }

    @Step("rest.execute.POST.subject.from.file")
    public void executePostSubjectUsingFile(File file) {
        assertFileExists(file);
        executeRequest(RequestSpecification::post, resourceLoader.readFileAsString(file));
    }

    @Step("rest.execute.POST.subject.from.document")
    public void executePostSubjectUsingDocument(Document document) {
        executeRequest(RequestSpecification::post, document.getContent());
    }

    @Step("rest.execute.POST.subject.empty")
    public void executePostSubject() {
        executeRequest(RequestSpecification::post);
    }

    @Step("rest.execute.POST.data.from.document")
    public void executePostDataUsingDocument(Document document) {
        executeRequest(RequestSpecification::post, document.getContent());
    }

    @Step("rest.execute.POST.data.from.file")
    public void executePostDataUsingFile(File file) {
        executePostSubjectUsingFile(file);
    }

    @Step("rest.execute.POST.data.empty")
    public void executePostData() {
        executeRequest(RequestSpecification::post);
    }


    @Step("rest.assert.response.body.strict.from.document")
    public void assertBodyStrictComparison(Document document) {
        assertContentIs(document, MatchMode.STRICT);
    }

    @Step("rest.assert.response.body.strict.from.document.any-order")
    public void assertBodyStrictComparisonAnyOrder(Document document) {
        assertContentIs(document, MatchMode.STRICT_ANY_ORDER);
    }

    @Step("rest.assert.response.body.loose.from.document")
    public void assertBodyLooseComparison(Document document) {
        assertContentIs(document, MatchMode.LOOSE);
    }

    @Step("rest.assert.response.body.strict.from.file")
    public void assertStrictFileContent(File file) {
        assertContentIs(file, MatchMode.STRICT);
    }

    @Step("rest.assert.response.body.strict.from.file.any-order")
    public void assertStrictFileContentAnyOrder(File file) {
        assertContentIs(file, MatchMode.STRICT_ANY_ORDER);
    }

    @Step("rest.assert.response.body.loose.from.file")
    public void assertLooseFileContent(File file) {
        assertContentIs(file, MatchMode.LOOSE);
    }

    @Step(value = "rest.assert.response.HTTP.code", args = "integer-assertion")
    public void assertHttpCode(Assertion<Integer> assertion) {
        validatableResponse.statusCode(MatcherAssertion.asMatcher(assertion));
    }

    @Step(value = "rest.assert.response.body.contentType", args = "word")
    public void assertResponseContentType(String contentType) {
        validatableResponse.contentType(parseContentType(contentType));
    }

    @Step(value = "rest.assert.response.body.length", args = {"matcher:integer-assertion"})
    public void assertResponseLength(Assertion<Integer> assertion) {
        validatableResponse.body(length(MatcherAssertion.asMatcher(assertion)));
    }

    @Step(value = "rest.assert.response.body.header.text", args = {"name:word", "matcher:text-assertion"})
    public void assertResponseHeaderAsText(String name, Assertion<String> assertion) {
        validatableResponse.header(name, MatcherAssertion.asMatcher(assertion));
    }

    @Step(value = "rest.assert.response.body.header.integer", args = {"name:word", "matcher:integer-assertion"})
    public void assertResponseHeaderAsInteger(String name, Assertion<Integer> assertion) {
        validatableResponse.header(name, Integer::parseInt, MatcherAssertion.asMatcher(assertion));
    }

    @Step(value = "rest.assert.response.body.header.decimal", args = {"name:word", "matcher:decimal-assertion"})
    public void assertResponseHeaderAsDecimal(String name, Assertion<BigDecimal> assertion) {
        validatableResponse.header(name, BigDecimal::new, MatcherAssertion.asMatcher(assertion));
    }

    @Step(value = "rest.assert.response.body.fragment.text", args = {"fragment:text", "matcher:text-assertion"})
    public void assertBodyFragmentAsText(String fragment, Assertion<String> assertion) {
        assertBodyFragment(fragment, assertion, String.class);
    }

    @Step(value = "rest.assert.response.body.fragment.integer", args = {"fragment:text", "matcher:integer-assertion"})
    public void assertBodyFragmentAsInteger(String fragment, Assertion<Integer> assertion) {
        assertBodyFragment(fragment, assertion, Integer.class);
    }

    @Step(value = "rest.assert.response.body.fragment.decimal", args = {"fragment:text", "matcher:decimal-assertion"})
    public void assertBodyFragmentAsDecimal(String fragment, Assertion<BigDecimal> assertion) {
        assertBodyFragment(fragment, assertion, BigDecimal.class);
    }


    @Step("rest.assert.response.body.schema.from.document")
    public void assertBodyContentSchema(Document document) {
        assertContentSchema(document.getContent());
    }


    @Step(value = "rest.assert.response.body.schema.from.file")
    public void assertBodyContentSchema(File file) {
        assertFileExists(file);
        assertContentSchema(resourceLoader.readFileAsString(file));
    }

}