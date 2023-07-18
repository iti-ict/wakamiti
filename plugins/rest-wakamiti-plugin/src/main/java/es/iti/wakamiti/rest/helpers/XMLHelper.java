/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.rest.helpers;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.datatypes.Assertion;
import es.iti.wakamiti.api.util.MatcherAssertion;
import es.iti.wakamiti.rest.ContentTypeHelper;
import es.iti.wakamiti.rest.MatchMode;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.StringReader;


@Extension(provider =  "es.iti.wakamiti", name = "rest-xml-helper", extensionPoint =  "es.iti.wakamiti.rest.ContentTypeHelper")
public class XMLHelper extends JSONHelper implements ContentTypeHelper {

    private final JsonXmlDiff diff = new JsonXmlDiff(ContentType.XML);
    private final SchemaFactory schemaFactory;


    public XMLHelper() {
        try {
            schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            // to be compliant, completely disable DOCTYPE declaration:
            schemaFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            // or prohibit the use of all protocols by external entities:
//            schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
//            schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        } catch (Exception e) {
            throw new WakamitiException(e);
        }
    }


    @Override
    public ContentType contentType() {
        return ContentType.XML;
    }


    @Override
    public void assertContent(String expected, String actual, MatchMode matchMode) {
       diff.assertContent(expected, actual, matchMode);
    }


    @Override
    public <T> void assertFragment(
        String fragment,
        ValidatableResponse response,
        Class<T> dataType,
        Assertion<T> assertion
    ) {
        response.body(fragment, MatcherAssertion.asMatcher(assertion));
    }


    @Override
    public void assertContentSchema(String expectedSchema, String content) {
        try {
            Schema schema = schemaFactory.newSchema(new SAXSource(new InputSource(new StringReader(expectedSchema))));
            validate(content, schema.newValidator());
        } catch (Exception e) {
            throw new WakamitiException(e);
        }

    }

    private void validate(String content, Validator validator) throws SAXException, IOException {
        try {
            validator.validate(new StreamSource(new StringReader(content)));
        }
        catch (SAXParseException e) {
            throw new AssertionError(e.getMessage());
        }
    }


}