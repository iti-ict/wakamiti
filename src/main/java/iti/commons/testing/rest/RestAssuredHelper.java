/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing.rest;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.naming.OperationNotSupportedException;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.filter.log.LogDetail;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ValidatableResponse;
import com.jayway.restassured.specification.RequestSpecification;

import iti.commons.testing.TestingException;
import iti.commons.testing.rest.RESTMessage.ContentType;

public class RestAssuredHelper implements RESTHelper {


    private static final Logger LOGGER = LoggerFactory.getLogger(RestAssuredHelper.class);

    private RequestSpecification request;
    private Response response;
    private ValidatableResponse validatableResponse;


   @Override
    public void setHost(String host) {
        RestAssured.baseURI = host;
    }

   @Override
   public void setHostPort(String host, Integer port) {
       RestAssured.baseURI = host;
       RestAssured.port = port;
   }


   @Override
    public void send(RESTMessage message) {
        newRequest();
        if (message.contentType() != null) {
            request.accept(translate(message.contentType()));
        }
        if (message.hasBody()) {
            request.contentType(translate(message.contentType()));
            request.content(message.body());
        }
        StringBuilder urlWithParams = new StringBuilder(message.url());
        for (int i=0; i<message.path().size(); i++) {
            request.pathParam("path"+i, message.path().get(i));
            urlWithParams.append("/{path"+i+"}");
        }
        if (!message.params().isEmpty()) {
            request.queryParams(message.params());
        }

        switch(message.method()) {
        case GET: response = request.get(urlWithParams.toString()); break;
        case PUT: response = request.put(urlWithParams.toString()); break;
        case POST: response = request.post(urlWithParams.toString()); break;
        case PATCH: response = request.patch(urlWithParams.toString()); break;
        case DELETE: response = request.delete(urlWithParams.toString()); break;
        case OPTIONS: response = request.options(urlWithParams.toString()); break;
        }
    }



    protected RequestSpecification newRequest() {
        response = null;
        validatableResponse = null;
        request = createNewRequest();
        return request;
    }



    protected static RequestSpecification createNewRequest() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL);
        RestAssuredConfig.config().getLogConfig().defaultStream(System.out);
        return RestAssured.given().log().all();
    }



    protected RequestSpecification request() {
        if (request == null) {
            throw new TestingException("REST request not specified");
        }
        return request;
    }


    protected ValidatableResponse response() {
        if (validatableResponse == null) {
            validatableResponse = response.then().log().all();
            LOGGER.info(validatableResponse.extract().asString());
            request = null; // next newRequest() will generate new response
        }
        return validatableResponse;
    }


    protected com.jayway.restassured.http.ContentType translate (ContentType contentType) {
        com.jayway.restassured.http.ContentType translated = null;
        switch (contentType) {
        case JSON: translated = com.jayway.restassured.http.ContentType.JSON; break;
        case TEXT: translated = com.jayway.restassured.http.ContentType.TEXT; break;
        case XML: translated = com.jayway.restassured.http.ContentType.XML; break;
        case HTML: translated = com.jayway.restassured.http.ContentType.HTML; break;
        default: translated = com.jayway.restassured.http.ContentType.ANY; break;
        }
        return translated;
    }






    @SuppressWarnings("unchecked")
    @Override
    public void assertResponseHttpCode(Matcher<?> matcher) {
        response().statusCode((Matcher<Integer>)matcher);
    }


    @Override
    public void assertResponseBody(String content, boolean strict) {
        // TODO Auto-generated method stub
        throw new TestingException("not implemented yet");
    }

    @Override
    public void assertResponseBodyArray(String segment, Matcher<?> binaryMatcher) {
        // TODO Auto-generated method stub
        throw new TestingException("not implemented yet");
    }

    @Override
    public void assertResponseBodySegment(String segment, Matcher<?> matcher) {
        response().body(segment,matcher);
    }

    @Override
    public void assertResponseContentType(ContentType contentType) {
        response().contentType(Matchers.anyOf(
                Stream.of(translate(contentType).getContentTypeStrings())
                .map(Matchers::startsWith)
                .collect(Collectors.toList())
        ));
    }

    @Override
    public void assertResponseHeader(String header, Matcher<?> matcher) {
        response().header(header, matcher);
    }

    @Override
    public void assertResponseLength(Matcher<?> matcher) {
        response().header("Content-length",matcher);
    }

}
