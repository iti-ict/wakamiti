/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.rest;


import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.datatypes.Assertion;
import es.iti.wakamiti.api.plan.DataTable;
import es.iti.wakamiti.api.plan.Document;
import es.iti.wakamiti.api.util.*;
import es.iti.wakamiti.rest.log.RestAssuredLogger;
import es.iti.wakamiti.rest.oauth.Oauth2ProviderConfig;
import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.apache.xmlbeans.XmlObject;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.slf4j.Logger;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
public class RestSupport {

    public static final Logger LOGGER = WakamitiLogger.forName("es.iti.wakamiti.rest");


    protected final Map<ContentType, ContentTypeHelper> contentTypeValidators = WakamitiAPI.instance()
            .extensionManager()
            .getExtensions(ContentTypeHelper.class)
            .collect(Collectors.toMap(ContentTypeHelper::contentType, Function.identity()));


    protected URL baseURL;
    protected String path;
    protected String subject;

    protected Matcher<Integer> failureHttpCodeAssertion;
    protected Response response;
    protected ValidatableResponse validatableResponse;
    protected Oauth2ProviderConfig oauth2ProviderConfig = new Oauth2ProviderConfig();
    protected Optional<Consumer<RequestSpecification>> authSpecification = Optional.empty();
    protected List<Consumer<RequestSpecification>> specifications = new LinkedList<>();

    protected static void config(RestAssuredConfig config) {
        RestAssured.config = config;
    }

    protected RequestSpecification newRequest() {
        response = null;
        validatableResponse = null;
        RequestSpecification request = RestAssured.given()
                .accept(ContentType.ANY);
        specifications.forEach(specification -> specification.accept(request));
        authSpecification.ifPresent(specification -> specification.accept(request));
        return attachLogger(request);
    }

    private RequestSpecification attachLogger(RequestSpecification request) {
        RestAssuredLogger logFilter = new RestAssuredLogger();
        if (LOGGER.isDebugEnabled()) {
            request.log().all().filter(logFilter);
            request.expect().log().all();
        } else {
            request.log().ifValidationFails().filter(logFilter);
            request.expect().log().ifValidationFails();
        }
        return request;
    }

    protected String uri() {
        if (baseURL == null) throw new WakamitiException("Missing required base URL.");
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
        return url.toString();
    }

    protected ValidatableResponse commonResponseAssertions(Response response) {
        return response.then()
                .statusCode(failureHttpCodeAssertion);
    }

    protected String retrieveOauthToken() {
        final String ACCESS_TOKEN = "access_token";
        return oauth2ProviderConfig.findCachedToken()
                .orElseGet(() -> {
                    oauth2ProviderConfig.checkParameters();
                    RequestSpecification request = RestAssured.given().contentType(ContentType.URLENC)
                            .auth().preemptive()
                            .basic(oauth2ProviderConfig.clientId(), oauth2ProviderConfig.clientSecret())
                            .formParams(oauth2ProviderConfig.parameters());
                    String token = attachLogger(request)
                            .with().post(oauth2ProviderConfig.url())
                            .then().statusCode(200)
                            .body(ACCESS_TOKEN, Matchers.notNullValue())
                            .extract().body().jsonPath().getString(ACCESS_TOKEN);
                    return oauth2ProviderConfig.storeTokenAndGet(token);
                });
    }

    protected void executeRequest(BiFunction<RequestSpecification, String, Response> function) {
        this.response = function.apply(newRequest(), uri());
        this.validatableResponse = commonResponseAssertions(response);
    }

    protected void executeRequest(
            BiFunction<RequestSpecification, String, Response> function,
            String body
    ) {
        this.response = function.apply(newRequest().body(body), uri());
        this.validatableResponse = commonResponseAssertions(response);
    }

    protected void assertFileExists(File file) {
        if (!file.exists()) {
            throw new WakamitiException("File '{}' not found", file.getAbsolutePath());
        }
    }

    protected void assertResponseNotNull() {
        if (response == null) {
            throw new WakamitiException("The request has not been executed");
        }
    }

    protected Map<String, String> tableToMap(DataTable dataTable) {
        if (dataTable.columns() != 2) {
            throw new WakamitiException("Table must have 2 columns [name, value]");
        }
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 1; i < dataTable.rows(); i++) {
            map.put(dataTable.value(i, 0), dataTable.value(i, 1));
        }
        return map;
    }

    protected Object parsedResponse() {
        Object body = doTry(
                () -> XmlUtils.xml(response.body().asString()),
                () -> JsonUtils.json(response.body().asString()),
                () -> response.body().asString()
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("headers", response.headers().asList().stream()
                .collect(Collectors.toMap(Header::getName, Header::getValue, this::collectIfDuplicated)));
        result.put("body", body);
        result.put("statusCode", response.statusCode());
        result.put("statusLine", response.statusLine());

        return body instanceof XmlObject
                || ContentType.fromContentType(response.contentType()) == ContentType.XML
                ? XmlUtils.xml("response", result)
                : JsonUtils.json(result);
    }

    @SuppressWarnings("unchecked")
    private Object collectIfDuplicated(Object oldObj, Object newObj) {
        if (oldObj instanceof List) {
            ((List<Object>) oldObj).add(newObj);
        } else {
            oldObj = new LinkedList<>(List.of(oldObj, newObj));
        }
        return oldObj;
    }

    private Object doTry(ThrowableSupplier<?>... suppliers) {
        for (ThrowableSupplier<?> supplier : suppliers) {
            try {
                Object result = supplier.get();
                if (result == null || result.toString().isEmpty()) {
                    throw new Exception();
                }
                return result;
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    protected ContentTypeHelper contentTypeHelperForResponse() {
        ContentType responseContentType = ContentType.fromContentType(response.contentType());
        if (responseContentType == null) {
            throw new WakamitiException("The content type of the response is undefined");
        }
        ContentTypeHelper helper = contentTypeValidators.get(responseContentType);
        if (helper == null) {
            throw new WakamitiException("There is no content type helper for '{}'", responseContentType);
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

    protected <T> void assertBodyFragment(String fragment, Assertion<T> assertion, Class<T> dataType) {
        ContentTypeHelper helper = contentTypeHelperForResponse();
        helper.assertFragment(fragment, validatableResponse, dataType, assertion);
    }

    protected void assertBodyFragment(String fragment, String expected, MatchMode matchMode) {
        ContentTypeHelper helper = contentTypeHelperForResponse();
        helper.assertContent(fragment, expected, validatableResponse.extract(), matchMode);
    }

    protected ContentType parseContentType(String contentType) {
        try {
            return ContentType.valueOf(contentType.toUpperCase());
        } catch (IllegalArgumentException e) {
            String validNames = Stream.of(ContentType.values()).map(Enum::name)
                    .collect(Collectors.joining(", "));
            throw new WakamitiException(
                    "REST content type must be one of the following: {}", validNames, e
            );
        }
    }

    protected void assertContentSchema(String expectedSchema) {
        ContentTypeHelper helper = contentTypeHelperForResponse();
        helper.assertContentSchema(expectedSchema, validatableResponse.extract().asString());
    }

    protected void assertSubtype(String subtype) {
        List<String> subtypes = Stream.of(ContentType.MULTIPART.getContentTypeStrings())
                .map(contentType -> contentType.split("/")[1]).collect(Collectors.toList());
        if (!subtypes.contains(subtype)) {
            throw new WakamitiException("'{}' is not a valid subtype. Possible values: {}", subtype, subtypes);
        }
    }

    String readFile(File file) {
        return resourceLoader().readFileAsString(file);
    }


    protected ResourceLoader resourceLoader() {
        return WakamitiAPI.instance().resourceLoader();
    }
}