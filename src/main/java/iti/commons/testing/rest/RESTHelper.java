/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing.rest;

import org.hamcrest.Matcher;

import iti.commons.testing.rest.RESTMessage.ContentType;

public interface RESTHelper {


    void setHost (String host);
    void setHostPort (String host, Integer port);

    void send(RESTMessage message);

    void assertResponseHttpCode(Matcher<?> matcher);
    void assertResponseContentType(ContentType contentType);
    void assertResponseLength(Matcher<?> matcher);
    void assertResponseHeader(String header, Matcher<?> matcher);
    void assertResponseBody(String content, boolean strict);
    void assertResponseBodySegment(String segment, Matcher<?> matcher);
    void assertResponseBodyArray(String segment, Matcher<?> binaryMatcher);



}
