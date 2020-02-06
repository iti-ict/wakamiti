/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.rest;


import java.io.File;
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

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoException;
import iti.kukumo.api.plan.Document;
import iti.kukumo.util.ResourceLoader;


public class RestSupport {

    public static final Logger LOGGER = LoggerFactory.getLogger("iti.kukumo.rest");
    public static final ResourceLoader resourceLoader = Kukumo.resourceLoader();

    protected final Map<ContentType, ContentTypeHelper> contentTypeValidators = Kukumo
        .extensionManager()
        .getExtensions(ContentTypeHelper.class)
        .collect(Collectors.toMap(ContentTypeHelper::contentType, Function.identity()));

    protected URL baseURL;
    protected ContentType requestContentType;
    protected String path;
    protected String subject;
    protected Long timeoutMillis;
    protected Consumer<RequestSpecification> authenticator;
    protected final Map<String, String> requestParams = new LinkedHashMap<>();
    protected Matcher<Integer> failureHttpCodeAssertion;
    protected Response response;
    protected ValidatableResponse validatableResponse;


    protected RequestSpecification newRequest() {
        response = null;
        validatableResponse = null;
        RequestSpecification request = RestAssured.given().accept(requestContentType).with()
            .params(requestParams);
        if (authenticator != null) {
            authenticator.accept(request);
        }
        return attachLogger(request);
    }


    private RequestSpecification attachLogger(RequestSpecification request) {
        if (LOGGER.isDebugEnabled()) {
            request.log().all(true);
            request.expect().log().all(true);
        } else {
            request.log().ifValidationFails();
            request.expect().log().ifValidationFails();
        }
        return request;
    }


    protected URI uri() {
        String base = baseURL.toString();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
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
            .statusCode(failureHttpCodeAssertion);
    }


    protected void executeRequest(BiFunction<RequestSpecification, URI, Response> function) {
        this.response = function.apply(newRequest(), uri());
        this.validatableResponse = commonResponseAssertions(response);
    }


    protected void executeRequest(
        BiFunction<RequestSpecification, URI, Response> function,
        String body
    ) {
        this.response = function.apply(newRequest().body(body), uri());
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


    protected ContentTypeHelper contentTypeHelperForResponse() {
        ContentType responseContentType = ContentType.fromContentType(response.contentType());
        if (responseContentType == null) {
            throw new KukumoException("The content type of the response is undefined");
        }
        ContentTypeHelper helper = contentTypeValidators.get(responseContentType);
        if (helper == null) {
            throw new KukumoException("There is no content type helper for " + responseContentType);
        }
        return helper;
    }


    protected void assertContentIs(Document expected, MatchMode matchMode) {
        ContentTypeHelper helper = contentTypeHelperForResponse();
        helper.assertContent(expected, validatableResponse.extract(), matchMode);
    }


    protected void assertContentIs(File expected, MatchMode matchMode) {
        ContentTypeHelper helper = contentTypeHelperForResponse();
        helper.assertContent(readFile(expected), validatableResponse.extract(), matchMode);
    }




    protected <T> void assertBodyFragment(String fragment, Matcher<T> matcher, Class<T> dataType) {
        ContentTypeHelper helper = contentTypeHelperForResponse();
        helper.assertFragment(fragment, validatableResponse, dataType, matcher);
    }


    protected ContentType parseContentType(String contentType) {
        try {
            return ContentType.valueOf(contentType.toUpperCase());
        } catch (IllegalArgumentException e) {
            String validNames = Stream.of(ContentType.values()).map(Enum::name)
                .collect(Collectors.joining(", "));
            throw new KukumoException(
                "REST content type must be one of the following: {}", validNames, e
            );
        }
    }


    private String readFile(File file) {
        return resourceLoader.readFileAsString(file);
    }

}