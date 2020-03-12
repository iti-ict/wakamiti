/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.rest;


import java.io.File;
import java.math.BigDecimal;
import java.net.URL;

import org.hamcrest.Matcher;

import io.restassured.specification.RequestSpecification;
import iti.commons.jext.Extension;
import iti.kukumo.api.annotations.I18nResource;
import iti.kukumo.api.annotations.Step;
import iti.kukumo.api.extensions.StepContributor;
import iti.kukumo.api.plan.Document;


@I18nResource("iti_kukumo_kukumo-rest")
@Extension(provider = "iti.kukumo", name = "rest-steps")
public class RestStepContributor extends RestSupport implements StepContributor {

    /*
     * // TODO rest.define.query.parameters=the query parameters {params:map}
     */


    @Step(value = "rest.define.contentType", args = "word")
    public void setContentType(String contentType) {
        this.requestContentType = parseContentType(contentType);
    }


    @Step(value = "rest.define.baseURL", args = "url")
    public void setBaseURL(URL url) {
        this.baseURL = url;
    }


    @Step(value = "rest.define.service")
    public void setService(String service) {
        this.path = (service.startsWith("/") ? service.substring(1) : service);
    }


    @Step("rest.define.subject")
    public void setSubject(String subject) {
        this.subject = (subject.startsWith("/") ? subject.substring(1) : subject);
    }


    @Step("rest.define.timeout.millis")
    public void setTimeoutInMillis(Integer millis) {
        this.timeoutMillis = Long.valueOf(millis);
    }


    @Step("rest.define.timeout.secs")
    public void setTimeoutInSecs(Integer secs) {
        this.timeoutMillis = Long.valueOf(secs * 1000L);
    }


    @Step(value = "rest.define.failure.http.code.assertion", args = "integer-assertion")
    public void setFailureHttpCodeAssertion(Matcher<Integer> httpCodeAssertion) {
        this.failureHttpCodeAssertion = httpCodeAssertion;
    }


    @Step(value = "rest.define.auth.basic", args = { "username:text", "password:text" })
    public void setBasicAuth(String username, String password) {
        this.authenticator = requestSpecification -> requestSpecification.auth()
            .basic(username, password);
    }


    @Step(value = "rest.define.auth.bearer")
    public void setBearerAuth(String token) {
        LOGGER.debug("Setting header [Authorization: Bearer {}]", token);
        this.authenticator = requestSpecification -> requestSpecification.auth().oauth2(token);
    }

    @Step("rest.define.auth.bearer.file")
    public void setBearerAuthFile(File file) {
        String token = resourceLoader.readFileAsString(file).trim();
        setBearerAuth(token);
    }


    @Step("rest.execute.GET.query")
    public void executeGetQuery() {
        executeRequest(RequestSpecification::get);
    }


    @Step("rest.execute.GET.subject")
    public void executeGetSubject() {
        assertSubjectDefined();
        executeRequest(RequestSpecification::get);
    }


    @Step("rest.execute.DELETE.subject")
    public void executeDeleteSubject() {
        assertSubjectDefined();
        executeRequest(RequestSpecification::delete);
    }


    @Step("rest.execute.PUT.subject.from.document")
    public void executePutSubjectUsingDocument(Document document) {
        assertSubjectDefined();
        executeRequest(RequestSpecification::put, document.getContent());
    }


    @Step("rest.execute.PUT.subject.from.file")
    public void executePutSubjectUsingFile(File file) {
        assertSubjectDefined();
        assertFileExists(file);
        executeRequest(RequestSpecification::put, resourceLoader.readFileAsString(file));
    }


    @Step("rest.execute.PATCH.subject.from.document")
    public void executePatchSubjectUsingDocument(Document document) {
        assertSubjectDefined();
        executeRequest(RequestSpecification::patch, document.getContent());
    }


    @Step("rest.execute.PATCH.subject.from.file")
    public void executePatchSubjectUsingFile(File file) {
        assertSubjectDefined();
        assertFileExists(file);
        executeRequest(RequestSpecification::patch, resourceLoader.readFileAsString(file));
    }


    @Step("rest.execute.POST.data.from.document")
    public void executePostUsingDocument(Document document) {
        executeRequest(RequestSpecification::post, document.getContent());
    }


    @Step("rest.execute.POST.subject.from.file")
    public void executePostSubjectUsingFile(File file) {
        assertSubjectDefined();
        assertFileExists(file);
        executeRequest(RequestSpecification::post, resourceLoader.readFileAsString(file));
    }


    @Step("rest.execute.POST.subject.from.document")
    public void executePostSubjectUsingDocument(Document document) {
        executeRequest(RequestSpecification::post, document.getContent());
    }


    @Step("rest.execute.POST.data.from.document")
    public void executePostDataUsingDocument(Document document) {
        executeRequest(RequestSpecification::post, document.getContent());
    }


    @Step("rest.execute.POST.data.from.file")
    public void executePostDataUsingFile(File file) {
        assertFileExists(file);
        executeRequest(RequestSpecification::post, resourceLoader.readFileAsString(file));
    }

    @Step("rest.execute.POST.empty")
    public void executePost() {
        executeRequest(RequestSpecification::post);
    }

    @Step("rest.assert.response.body.strict.from.file")
    public void assertStrictFileContent(File file) {
        assertContentIs(file, MatchMode.STRICT);
    }


    @Step("rest.assert.response.body.strict.from.file.anyorder")
    public void assertStrictFileContentAnyOrder(File file) {
        assertContentIs(file, MatchMode.STRICT_ANY_ORDER);
    }


    @Step("rest.assert.response.body.loose.from.file")
    public void assertLooseFileContent(File file) {
        assertContentIs(file, MatchMode.LOOSE);
    }


    @Step("rest.assert.response.body.strict.from.document")
    public void assertBodyStrictComparison(Document document) {
        assertContentIs(document, MatchMode.STRICT);
    }


    @Step("rest.assert.response.body.strict.from.document.anyorder")
    public void assertBodyStrictComparisonAnyOrder(Document document) {
        assertContentIs(document, MatchMode.STRICT_ANY_ORDER);
    }


    @Step("rest.assert.response.body.loose.from.document")
    public void assertBodyLooseComparison(Document document) {
        assertContentIs(document, MatchMode.LOOSE);
    }


    @Step(value = "rest.assert.response.HTTP.code", args = "integer-assertion")
    public void assertHttpCode(Matcher<Integer> matcher) {
        validatableResponse.statusCode(matcher);
    }


    @Step(value = "rest.assert.response.body.contentType", args = "word")
    public void assertResponseContentType(String contentType) {
        validatableResponse.contentType(parseContentType(contentType));
    }


    @Step(value = "rest.assert.response.body.fragment.text", args = { "fragment:text",
                    "matcher:text-assertion" })
    public void assertBodyFragmentAsText(String fragment, Matcher<String> matcher) {
        assertBodyFragment(fragment, matcher, String.class);
    }


    @Step(value = "rest.assert.response.body.fragment.integer", args = { "fragment:text",
                    "matcher:integer-assertion" })
    public void assertBodyFragmentAsInteger(String fragment, Matcher<Integer> matcher) {
        assertBodyFragment(fragment, matcher, Integer.class);
    }


    @Step(value = "rest.assert.response.body.fragment.decimal", args = { "fragment:text",
                    "matcher:decimal-assertion" })
    public void assertBodyFragmentAsDecimal(String fragment, Matcher<BigDecimal> matcher) {
        assertBodyFragment(fragment, matcher, BigDecimal.class);
    }

}
