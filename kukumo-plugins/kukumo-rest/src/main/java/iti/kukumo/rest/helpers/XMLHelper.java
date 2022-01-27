/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.rest.helpers;


import iti.kukumo.api.KukumoException;
import iti.kukumo.api.datatypes.Assertion;
import iti.kukumo.util.MatcherAssertion;
import org.hamcrest.Matcher;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import iti.commons.jext.Extension;
import iti.kukumo.rest.ContentTypeHelper;
import iti.kukumo.rest.MatchMode;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.StringReader;


@Extension(provider = "iti.kukumo", name = "rest-xml-helper", extensionPoint = "iti.kukumo.rest.ContentTypeHelper")
public class XMLHelper extends JSONHelper implements ContentTypeHelper {

    private final JsonXmlDiff diff = new JsonXmlDiff(ContentType.XML);
    private final SchemaFactory schemaFactory;


    public XMLHelper() {
        try {
            schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            // to be compliant, completely disable DOCTYPE declaration:
            schemaFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            // or prohibit the use of all protocols by external entities:
            schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        } catch (Exception e) {
            throw new KukumoException(e);
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
            Validator validator = schema.newValidator();
            try {
                validator.validate(new StreamSource(new StringReader(content)));
            }
            catch (SAXParseException e) {
                throw new AssertionError(e.getMessage());
            }
        } catch (Exception e) {
            throw new KukumoException(e);
        }

    }


}