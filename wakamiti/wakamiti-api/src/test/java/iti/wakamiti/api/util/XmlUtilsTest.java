/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package iti.wakamiti.api.util;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlRuntimeException;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.w3c.dom.Node;
import org.xmlunit.assertj3.XmlAssert;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.ElementSelectors;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class XmlUtilsTest {

    private final String xml = "<item><name>Arnold</name><age>47</age></item>";
    private final String xml_error = "<item><nameArnold</name><age>47</age></item>";

    @Test
    public void testXmlWhenStringWithSuccess() {
        XmlObject obj = XmlUtils.xml(xml);

        assertThat(obj).isNotNull();
        assertThat(obj.xmlText()).isEqualTo(xml);
    }

    @Test(expected = XmlRuntimeException.class)
    public void testXmlWhenStringWithError() {
        XmlUtils.xml(xml_error);
    }

    @Test
    public void testXmlWhenStreamWithSuccess() {
        XmlObject obj = XmlUtils.xml(new ByteArrayInputStream(xml.getBytes()));

        assertThat(obj).isNotNull();
        assertThat(obj.xmlText()).isEqualTo(xml);
    }

    @Test(expected = XmlRuntimeException.class)
    public void testXmlWhenStreamWithError() {
        XmlUtils.xml(new ByteArrayInputStream(xml_error.getBytes()));
    }

    @Test
    public void testXmlWhenNodeWithSuccess() throws XmlException, IOException {
        XmlObject obj = XmlUtils.xml(XmlObject.Factory.parse(new ByteArrayInputStream(xml.getBytes())).getDomNode());

        assertThat(obj).isNotNull();
        assertThat(obj.xmlText()).isEqualTo(xml);
    }

    @Test(expected = XmlRuntimeException.class)
    public void testXmlWhenNodeWithError() throws XmlException, IOException {
        Node node = XmlObject.Factory.parse(new ByteArrayInputStream(xml.getBytes())).getDomNode();
        try (MockedStatic<XmlObject.Factory> utilities = Mockito.mockStatic(XmlObject.Factory.class)) {
            utilities.when(() -> XmlObject.Factory.parse(any(Node.class))).thenThrow(XmlException.class);

            XmlUtils.xml(node);
        }
    }

    @Test
    public void testXmlWhenMapWithSuccess() throws XmlException, IOException {
        XmlObject obj = XmlUtils.xml("response",
                Map.of(
                        "statusCode", "200",
                        "body", XmlObject.Factory.parse(new ByteArrayInputStream(xml.getBytes())),
                        "headers", Map.of("keep-alive", "true"),
                        "other", XmlObject.Factory.parse(new ByteArrayInputStream(xml.getBytes())).getDomNode()
                ));

        assertThat(obj).isNotNull();
        assertThat(obj.xmlText()).contains("<body>" + xml + "</body>");
        assertThat(obj.xmlText()).contains("<other>" + xml + "</other>");
        assertThat(obj.xmlText()).contains("<statusCode>200</statusCode>");
        assertThat(obj.xmlText()).contains("<headers><keep-alive>true</keep-alive></headers>");
    }

    @Test(expected = XmlRuntimeException.class)
    public void testXmlWhenMapWithError() {
        try (MockedStatic<DocumentBuilderFactory> utilities = Mockito.mockStatic(DocumentBuilderFactory.class)) {
            DocumentBuilderFactory dbf = mock(DocumentBuilderFactory.class);
            when(dbf.newDocumentBuilder()).thenThrow(ParserConfigurationException.class);
            utilities.when(DocumentBuilderFactory::newInstance).thenReturn(dbf);

            XmlUtils.xml("response", Map.of());
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testReadStringValueWithSuccess() throws XmlException, IOException {
        XmlObject object = XmlUtils.xml("response",
                Map.of(
                        "statusCode", "200",
                        "body", XmlObject.Factory.parse(new ByteArrayInputStream(xml.getBytes())),
                        "headers", Map.of("keep-alive", "true", "content-type", "application/json"),
                        "other", XmlObject.Factory.parse(new ByteArrayInputStream(xml.getBytes())).getDomNode()
                ));

        String result = XmlUtils.readStringValue(object, "//content-type");
        assertThat(result).isEqualTo("<content-type>application/json</content-type>");

        result = XmlUtils.readStringValue(object, "//*/text()");
        assertThat(result).startsWith("[").endsWith("]")
                .contains("application/json", "true", "200", "Arnold", "47", "Arnold", "47");

        result = XmlUtils.readStringValue(object, "//statusCode/text()");
        assertThat(result).isEqualTo("200");

        result = XmlUtils.readStringValue(object, "//response");
        XmlAssert.assertThat(result).and("<response>"
                        + "<body><item><name>Arnold</name><age>47</age></item></body>"
                        + "<headers><content-type>application/json</content-type><keep-alive>true</keep-alive></headers>"
                        + "<statusCode>200</statusCode>"
                        + "<other><item><name>Arnold</name><age>47</age></item></other>"
                        + "</response>")
                .ignoreWhitespace()
                .normalizeWhitespace()
                .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndText))
                .areSimilar();

        result = XmlUtils.readStringValue(object, "count(//*)");
        assertThat(result).isEqualTo("13.0");

        result = XmlUtils.readStringValue(object, "//body/@id");
        assertThat(result).isEmpty();
    }

    @Test(expected = XmlRuntimeException.class)
    public void testReadStringValueWithError() throws XmlException, IOException {
        XmlObject object = XmlUtils.xml("response",
                Map.of(
                        "statusCode", "200",
                        "body", XmlObject.Factory.parse(new ByteArrayInputStream(xml.getBytes())),
                        "headers", Map.of("keep-alive", "true", "content-type", "application/json"),
                        "other", XmlObject.Factory.parse(new ByteArrayInputStream(xml.getBytes())).getDomNode()
                ));

        XmlUtils.readStringValue(object, "function(abc)");
    }
}
