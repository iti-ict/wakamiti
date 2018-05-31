/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing.rest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Locale;

import org.hamcrest.Matchers;

import cucumber.api.java8.En;
import iti.commons.testing.AbstractLocaleSteps;
import iti.commons.testing.TestingException;
import iti.commons.testing.rest.RESTMessage.ContentType;

public class RestSteps extends AbstractLocaleSteps<RESTHelper> implements En {

    private static final String SET_HOST ="rest.set.host";
    private static final String SET_HOST_PORT ="rest.set.host.port";
    private static final String SET_SERVICE ="rest.set.service";
    private static final String SET_ENTITY ="rest.set.entity";
    private static final String SET_CHARSET ="rest.set.charset";
    private static final String GET_ID_SERVICE ="rest.action.get.id.service";
    private static final String GET_ID_PREDEFINED_SERVICE ="rest.action.get.id.predefined.service";
    private static final String GET_PREDEFINED_ID_SERVICE ="rest.action.get.predefined.id.service";
    private static final String GET_PREDEFINED_ID_PREDEFINED_SERVICE ="rest.action.get.predefined.id.predefined.service";
    private static final String PUT_ID_SERVICE_FROM_TEXT ="rest.action.put.id.service.from.text";
    private static final String PUT_ID_PREDEFINED_SERVICE_FROM_TEXT ="rest.action.put.id.predefined.service.from.text";
    private static final String PUT_PREDEFINED_ID_SERVICE_FROM_TEXT ="rest.action.put.predefined.id.service.from.text";
    private static final String PUT_PREDEFINED_ID_PREDEFINED_SERVICE_FROM_TEXT ="rest.action.put.predefined.id.predefined.service.from.text";
    private static final String PUT_ID_SERVICE_FROM_FILE ="rest.action.put.id.service.from.file";
    private static final String PUT_ID_PREDEFINED_SERVICE_FROM_FILE ="rest.action.put.id.predefined.service.from.file";
    private static final String PUT_PREDEFINED_ID_SERVICE_FROM_FILE ="rest.action.put.predefined.id.service.from.file";
    private static final String PUT_PREDEFINED_ID_PREDEFINED_SERVICE_FROM_FILE ="rest.action.put.predefined.id.predefined.service.from.file";
    private static final String POST_SERVICE_FROM_TEXT ="rest.action.post.service.from.text";
    private static final String POST_PREDEFINED_SERVICE_FROM_TEXT ="rest.action.post.predefined.service.from.text";
    private static final String POST_SERVICE_FROM_FILE ="rest.action.post.service.from.file";
    private static final String POST_PREDEFINED_SERVICE_FROM_FILE ="rest.action.post.predefined.service.from.file";
    private static final String DELETE_ID_SERVICE ="rest.action.delete.id.service";
    private static final String DELETE_ID_PREDEFINED_SERVICE ="rest.action.delete.id.predefined.service";
    private static final String DELETE_PREDEFINED_ID_SERVICE ="rest.action.delete.predefined.id.service";
    private static final String DELETE_PREDEFINED_ID_PREDEFINED_SERVICE ="rest.action.delete.predefined.id.predefined.service";

    private static final String ASSERT_HTTP_CODE = "rest.assert.http.code";
    private static final String ASSERT_HTTP_CODE_MATCHER_NUMBER = "rest.assert.http.code.matcher.number";
    private static final String ASSERT_HTTP_CONTENT_TYPE = "rest.assert.http.contentType";
    private static final String ASSERT_HTTP_LENGTH = "rest.assert.http.length";
    private static final String ASSERT_HTTP_LENGTH_EMPTY = "rest.assert.http.length.empty";
    private static final String ASSERT_HTTP_LENGTH_MATCHER_NUMBER = "rest.assert.http.length.matcher.number";
    private static final String ASSERT_HTTP_HEADER = "rest.assert.http.header";
    private static final String ASSERT_HTTP_HEADER_MATCHER_NUMBER = "rest.assert.http.header.matcher.number";
    private static final String ASSERT_HTTP_HEADER_MATCHER_STRING = "rest.assert.http.header.matcher.string";

    private static final String ASSERT_BODY_LOOSE_FROM_TEXT ="rest.assert.body.full.loose.from.text";
    private static final String ASSERT_BODY_STRICT_FROM_TEXT ="rest.assert.body.full.strict.from.text";
    private static final String ASSERT_BODY_LOOSE_FROM_FILE ="rest.assert.body.full.loose.from.file";
    private static final String ASSERT_BODY_STRICT_FROM_FILE ="rest.assert.body.full.strict.from.file";
    private static final String ASSERT_SEGMENT_MATCHER_UNARY ="rest.assert.body.segment.matcher.unary";
    private static final String ASSERT_SEGMENT_MACTHER_BINARY_STRING ="rest.assert.body.segment.matcher.binary.string";
    private static final String ASSERT_SEGMENT_MATCHER_BINARY_NUMBER ="rest.assert.body.segment.matcher.binary.number";
    private static final String ASSERT_ARRAY_SIZE_MATCHER_BINARY_NUMBER ="rest.assert.body.array.size.matcher.binary.number";


    private String predefinedEntityID;
    private String predefinedService;
    private ContentType predefinedContentType;
    private String predefinedCharset = "utf-8";


    public RestSteps (RESTHelper helper) {
        super(helper, "restSteps");
    }

    public RestSteps(RESTHelper helper, ClassLoader classLoader, Locale locale, String localeDefinitionFile) {
        super(helper, classLoader, locale, localeDefinitionFile);
    }

    public RestSteps(RESTHelper helper, String localeDefinitionFile) {
        super(helper, localeDefinitionFile);
    }

    @Override
    public void registerSteps() {
        Given(resolve(SET_HOST),         helper::setHost);
        Given(resolve(SET_HOST_PORT),    helper::setHostPort);
        Given(resolve(SET_SERVICE),      this::setPredefinedService);
        Given(resolve(SET_ENTITY),       this::setPredefinedEntityID);
        Given(resolve(SET_CHARSET),      this::setPredefinedCharset);

        When(resolve(GET_ID_SERVICE), (String entityID, String contentType, String service)->
            helper.send(RESTMessage.get(service, Arrays.asList(entityID))));
        When(resolve(GET_ID_PREDEFINED_SERVICE), (String entityID)->
            helper.send(RESTMessage.get(predefinedService(), Arrays.asList(entityID))));
        When(resolve(GET_PREDEFINED_ID_SERVICE), (String contentType, String service)->
            helper.send(RESTMessage.get(service, Arrays.asList(predefinedEntityID()))));
        When(resolve(GET_PREDEFINED_ID_PREDEFINED_SERVICE), ()->
            helper.send(RESTMessage.get(predefinedService(), Arrays.asList(predefinedEntityID()))));

        When(resolve(PUT_ID_SERVICE_FROM_TEXT), (String entityId, String contentType, String service, String body)->
            helper.send(RESTMessage.put(service, Arrays.asList(entityId), contentType(contentType), predefinedCharset(), body)));
        When(resolve(PUT_ID_PREDEFINED_SERVICE_FROM_TEXT), (String entityId, String body)->
            helper.send(RESTMessage.put(predefinedService(), Arrays.asList(entityId), predefinedContentType(), predefinedCharset(), body)));
        When(resolve(PUT_PREDEFINED_ID_SERVICE_FROM_TEXT), (String contentType, String service, String body)->
            helper.send(RESTMessage.put(service, Arrays.asList(predefinedEntityID()), contentType(contentType), predefinedCharset(), body)));
        When(resolve(PUT_PREDEFINED_ID_PREDEFINED_SERVICE_FROM_TEXT), (String body)->
            helper.send(RESTMessage.put(predefinedService(), Arrays.asList(predefinedEntityID()), predefinedContentType(), predefinedCharset(), body)));
        When(resolve(PUT_ID_SERVICE_FROM_FILE), (String entityId, String contentType, String service, String file)->
            helper.send(RESTMessage.put(service, Arrays.asList(entityId), contentType(contentType), predefinedCharset(), readFile(file))));
        When(resolve(PUT_ID_PREDEFINED_SERVICE_FROM_FILE), (String entityId, String file)->
            helper.send(RESTMessage.put(predefinedService(), Arrays.asList(entityId), predefinedContentType(), predefinedCharset(), readFile(file))));
        When(resolve(PUT_PREDEFINED_ID_SERVICE_FROM_FILE), (String contentType, String service, String file)->
            helper.send(RESTMessage.put(service, Arrays.asList(predefinedEntityID()), contentType(contentType), predefinedCharset(), readFile(file))));
        When(resolve(PUT_PREDEFINED_ID_PREDEFINED_SERVICE_FROM_FILE), (String file)->
            helper.send(RESTMessage.put(predefinedService(), Arrays.asList(predefinedEntityID()), predefinedContentType(), predefinedCharset(), readFile(file))));

        When(resolve(POST_SERVICE_FROM_TEXT), (String contentType, String service, String body)->
            helper.send(RESTMessage.post(service, Arrays.asList(), contentType(contentType), predefinedCharset(), body)));
        When(resolve(POST_PREDEFINED_SERVICE_FROM_TEXT), (String body)->
            helper.send(RESTMessage.post(predefinedService(), Arrays.asList(), predefinedContentType(), predefinedCharset(), body)));
        When(resolve(POST_SERVICE_FROM_FILE), (String contentType, String service, String file)->
            helper.send(RESTMessage.post(service, Arrays.asList(), contentType(contentType), predefinedCharset(), readFile(file))));
        When(resolve(POST_PREDEFINED_SERVICE_FROM_FILE), (String file)->
            helper.send(RESTMessage.post(predefinedService(), Arrays.asList(), predefinedContentType(), predefinedCharset(), readFile(file))));


        When(resolve(DELETE_ID_SERVICE), (String entityId, String contentType, String service)->
            helper.send(RESTMessage.delete(service, Arrays.asList(entityId))));
        When(resolve(DELETE_ID_PREDEFINED_SERVICE), (String entityId)->
            helper.send(RESTMessage.delete(predefinedService(), Arrays.asList(entityId))));
        When(resolve(DELETE_PREDEFINED_ID_SERVICE), (String contentType, String service)->
            helper.send(RESTMessage.delete(service, Arrays.asList(predefinedEntityID()))));
        When(resolve(DELETE_PREDEFINED_ID_PREDEFINED_SERVICE), ()->
            helper.send(RESTMessage.delete(predefinedService(), Arrays.asList(predefinedEntityID()))));


        Then(resolve(ASSERT_HTTP_CODE), (Integer httpCode)->
            helper.assertResponseHttpCode(Matchers.equalTo(httpCode)));
        Then(resolve(ASSERT_HTTP_CODE_MATCHER_NUMBER), (String matcher, Integer number)->
            helper.assertResponseHttpCode(matchParser.binaryMatcher(matcher, number)));
        Then(resolve(ASSERT_HTTP_CONTENT_TYPE), (String contentType)->
            helper.assertResponseContentType(contentType(contentType)));
        Then(resolve(ASSERT_HTTP_LENGTH), (Long length)->
            helper.assertResponseLength(Matchers.equalTo(length)));
        Then(resolve(ASSERT_HTTP_LENGTH_EMPTY), ()->
            helper.assertResponseLength(Matchers.equalTo("0")));
        Then(resolve(ASSERT_HTTP_LENGTH_MATCHER_NUMBER), (String matcher, Long value)->
            helper.assertResponseLength(matchParser.binaryMatcher(matcher, value)));
        Then(resolve(ASSERT_HTTP_HEADER), (String header)->
            helper.assertResponseHeader(header, Matchers.not(Matchers.isEmptyOrNullString())));
        Then(resolve(ASSERT_HTTP_HEADER_MATCHER_NUMBER), (String header, String matcher, Integer value)->
            helper.assertResponseHeader(header, matchParser.binaryMatcher(matcher, value)));
        Then(resolve(ASSERT_HTTP_HEADER_MATCHER_STRING), (String header, String matcher, String value)->
            helper.assertResponseHeader(header, matchParser.binaryMatcher(matcher, value)));

        Then(resolve(ASSERT_BODY_LOOSE_FROM_TEXT), (String content)->
            helper.assertResponseBody(content, false));
        Then(resolve(ASSERT_BODY_STRICT_FROM_TEXT), (String content)->
            helper.assertResponseBody(content, true));
        Then(resolve(ASSERT_BODY_LOOSE_FROM_FILE), (String file)->
            helper.assertResponseBody(readFile(file), false));
        Then(resolve(ASSERT_BODY_STRICT_FROM_FILE), (String file)->
            helper.assertResponseBody(readFile(file), true));

        Then(resolve(ASSERT_SEGMENT_MATCHER_UNARY), (String segment, String matcher)->
            helper.assertResponseBodySegment(segment, matchParser.unaryMatcher(segment)));
        Then(resolve(ASSERT_SEGMENT_MACTHER_BINARY_STRING), (String segment, String matcher, String value)->
            helper.assertResponseBodySegment(segment, matchParser.binaryMatcher(matcher, value)));
        Then(resolve(ASSERT_SEGMENT_MATCHER_BINARY_NUMBER), (String segment, String matcher, Integer value)->
            helper.assertResponseBodySegment(segment, matchParser.binaryMatcher(matcher, value)));
        Then(resolve(ASSERT_ARRAY_SIZE_MATCHER_BINARY_NUMBER), (String segment, String matcher, Integer length)->
            helper.assertResponseBodyArray(segment, matchParser.binaryMatcher(matcher, length)));

    }

    protected String readFile (String filename) {
        try {
            try (InputStreamReader reader = new InputStreamReader(
                    classLoader.getResourceAsStream(filename),predefinedCharset())) {
                StringBuilder content = new StringBuilder();
                char[] buffer = new char[1024];
                int readed = 0;
                while ((readed = reader.read(buffer)) != -1) {
                    content.append(buffer, 0, readed);
                }
                return content.toString();
            }
        } catch (IOException e) {
            throw new TestingException(e);
        }
    }


    protected void setPredefinedService(String contentType, String predefinedService) {
        this.predefinedService = predefinedService;
        this.predefinedContentType = contentType(contentType);
    }

    protected ContentType contentType(String contentType) {
        return ContentType.valueOf(contentType);
    }


    protected void setPredefinedEntityID(String predefinedEntityID) {
        this.predefinedEntityID = predefinedEntityID;
    }


    protected String predefinedService() {
        if (predefinedService == null) {
            throw new TestingException("Must define service first");
        }
        return predefinedService;
    }

    protected String predefinedEntityID() {
        if (predefinedEntityID == null) {
            throw new TestingException("Must define an entity ID first");
        }

        return predefinedEntityID;
    }


    protected ContentType predefinedContentType() {
        if (predefinedContentType == null) {
            throw new TestingException("Must define the content-type first");
        }
        return predefinedContentType;
    }

    protected void setPredefinedCharset(String predefinedCharset) {
        this.predefinedCharset = predefinedCharset;
    }

    protected String predefinedCharset() {
        return predefinedCharset;
    }
}
