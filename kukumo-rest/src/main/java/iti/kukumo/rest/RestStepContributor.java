package iti.kukumo.rest;


import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import iti.commons.jext.Extension;
import iti.commons.jext.ExtensionManager;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoException;
import iti.kukumo.api.annotations.I18nResource;
import iti.kukumo.api.annotations.Step;
import iti.kukumo.api.extensions.StepContributor;
import iti.kukumo.api.plan.Document;
import iti.kukumo.util.ResourceLoader;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@I18nResource("iti_kukumo_kukumo-rest")
@Extension(provider = "iti.kukumo", name = "rest", version = "1.0.0")
public class RestStepContributor implements StepContributor {

    public static final Logger LOGGER = LoggerFactory.getLogger("iti.kukumo.rest");
    public static final ResourceLoader resourceLoader = Kukumo.getResourceLoader();

    private Map<ContentType,ContentTypeHelper> contentTypeValidators = new ExtensionManager()
            .findExtensions(ContentTypeHelper.class).stream()
            .collect(Collectors.toMap(ContentTypeHelper::contentType, Function.identity()));

    private URL baseURL;
    private ContentType contentType;
    private String path;
    private String subject;
    private Long timeoutMillis;
    private Consumer<RequestSpecification> authenticator;
    private Map<String,String> requestParams = new LinkedHashMap<>();
    private Matcher<Integer> failureHttpCodeAssertion;
    private Response response;
    private ValidatableResponse validatableResponse;





    protected RequestSpecification newRequest() {
        response = null;
        validatableResponse = null;
        RequestSpecification request = RestAssured.given().accept(contentType).with().params(requestParams);
        if (authenticator != null) {
            authenticator.accept(request);
        }
        return attachLogger(request);
    }


    private RequestSpecification attachLogger(RequestSpecification request) {
        if (LOGGER.isDebugEnabled()) {
            request.log().all(true);
            request.expect().log().all(true);
        } else if (LOGGER.isInfoEnabled()) {
            request.log().method().log().uri();
            request.expect().log().status();
        } else {
            request.log().ifValidationFails();
            request.expect().log().ifValidationFails();
        }
        return request;
    }


    protected URI uri() {
        String base = baseURL.toString();
        if (base.endsWith("/")) {
            base = base.substring(0,base.length()-1);
        }
        StringBuilder url = new StringBuilder(base);
        if (path != null) {
            url.append("/").append(path);
        }
        if (subject != null) {
            url.append("/").append(subject);
        }
        return URI.create(url.toString());
    }


    protected ValidatableResponse commonResponseAssertions(Response response) {
        return response.then()
          .time(timeoutMillis != null ? Matchers.lessThan(timeoutMillis) : Matchers.any(Long.class), TimeUnit.MILLISECONDS)
          .statusCode(failureHttpCodeAssertion)
        ;
    }


    protected void executeRequest(BiFunction<RequestSpecification,URI,Response> function) {
        this.response = function.apply(newRequest(),uri());
        this.validatableResponse = commonResponseAssertions(response);
    }

    protected void executeRequest(BiFunction<RequestSpecification,URI,Response> function, String body) {
        this.response = function.apply(newRequest().body(body),uri());
        this.validatableResponse = commonResponseAssertions(response);
    }


    protected void assertFileExists(File file) {
        if (!file.exists()) {
           throw new KukumoException("File {} not found", file.getAbsolutePath());
        }
    }




    protected void assertSubjectDefined() {
        if (subject == null) {
            throw new KukumoException("Subject not defined");
        }
    }


    protected ContentTypeHelper contentTypeHelper() {
        ContentType contentType = ContentType.fromContentType(response.contentType());
        ContentTypeHelper helper = contentTypeValidators.get(contentType);
        if (helper == null) {
            throw new KukumoException("There is no content type helper for "+contentType);
        }
        return helper;
    }


    protected void assertContentIs(Document expected, boolean exactMatch) {
        ContentTypeHelper helper = contentTypeHelper();
        helper.assertContent(expected,validatableResponse.extract(),exactMatch);
    }

    protected void assertContentIs(File expected, boolean exactMatch) {
        ContentTypeHelper helper = contentTypeHelper();
        helper.assertContent(expected,validatableResponse.extract(),exactMatch);
    }


    private <T> void assertBodyFragment(String fragment, Matcher<T> matcher, Class<T> dataType) {
        ContentTypeHelper helper = contentTypeHelper();
        helper.assertFragment(fragment,validatableResponse,dataType,matcher);
    }



    protected ContentType parseContentType(String contentType) {
        try {
            return ContentType.valueOf(contentType.toUpperCase());
        } catch (IllegalArgumentException e) {
            String validNames = Stream.of(ContentType.values()).map(Enum::name).collect(Collectors.joining(", "));
            throw new KukumoException("REST content type must be one of the following: {}", validNames, e);
        }
    }

    /*
     // TODO rest.define.query.parameters=the query parameters {params:map}
     */



    @Step(value="rest.define.contentType", args="word")
    public void setContentType(String contentType) {
        this.contentType = parseContentType(contentType);
    }


    @Step(value="rest.define.baseURL",args ="url")
    public void setBaseURL(URL url) {
        this.baseURL = url;
    }


    @Step(value="rest.define.service")
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
        this.timeoutMillis = Long.valueOf(secs * 1000);
    }


    @Step(value="rest.define.failure.http.code.assertion", args="integer-assertion")
    public void setFailureHttpCodeAssertion(Matcher<Integer> httpCodeAssertion) {
        this.failureHttpCodeAssertion = httpCodeAssertion;
    }

    @Step(value="rest.define.auth.basic", args={"username:text","password:text"})
    public void setBasicAuth(String username, String password) {
        this.authenticator = requestSpecification -> requestSpecification.auth().basic(username,password);
    }

    @Step(value="rest.define.auth.bearer")
    public void setBearerAuth(String token) {
        this.authenticator = requestSpecification -> requestSpecification.auth().oauth2(token);
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


    @Step("rest.assert.response.body.strict.from.file")
    public void assertStrictFileContent(File file) {
        assertContentIs(file, true);
    }

    @Step("rest.assert.response.body.loose.from.file")
    public void assertLooseFileContent(File file) {
        assertContentIs(file, true);
    }


    @Step("rest.assert.response.body.strict.from.document")
    public void assertBodyStrictComparison(Document document) {
        assertContentIs(document, true);
    }


    @Step("rest.assert.response.body.loose.from.document")
    public void assertBodyLooseComparison(Document document) {
        assertContentIs(document, false);
    }

    @Step(value="rest.assert.response.HTTP.code", args="integer-assertion")
    public void assertHttpCode(Matcher<Integer> matcher) {
        validatableResponse.statusCode(matcher);
    }


    @Step(value="rest.assert.response.body.contentType", args="word")
    public void assertResponseContentType(String contentType) {
        validatableResponse.contentType(parseContentType(contentType));
    }


    @Step(value="rest.assert.response.body.fragment.text", args={"fragment:text", "matcher:text-assertion"})
    public void assertBodyFragmentAsText(String fragment, Matcher<String> matcher) {
        assertBodyFragment(fragment,matcher,String.class);
    }


    @Step(value="rest.assert.response.body.fragment.integer", args={"fragment:text", "matcher:integer-assertion"})
    public void assertBodyFragmentAsInteger(String fragment, Matcher<Integer> matcher) {
        assertBodyFragment(fragment,matcher,Integer.class);
    }

    @Step(value="rest.assert.response.body.fragment.decimal", args={"fragment:text", "matcher:decimal-assertion"})
    public void assertBodyFragmentAsDecimal(String fragment, Matcher<BigDecimal> matcher) {
        assertBodyFragment(fragment,matcher,BigDecimal.class);
    }

}
