/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.rest.helpers;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.datatypes.Assertion;
import es.iti.wakamiti.api.util.MatcherAssertion;
import es.iti.wakamiti.rest.ContentTypeHelper;
import es.iti.wakamiti.rest.MatchMode;
import io.restassured.common.mapper.ObjectDeserializationContext;
import io.restassured.http.ContentType;
import io.restassured.path.xml.config.XmlPathConfig;
import io.restassured.path.xml.mapping.XmlPathObjectDeserializer;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.apache.xmlbeans.XmlObject;
import org.hamcrest.MatcherAssert;

import static es.iti.wakamiti.api.util.XmlUtils.xml;
import static io.restassured.matcher.RestAssuredMatchers.matchesXsd;


/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
@Extension(
        provider = "es.iti.wakamiti",
        name = "rest-xml-helper",
        version = "2.4",
        extensionPoint = "es.iti.wakamiti.rest.ContentTypeHelper"
)
public class XMLHelper implements ContentTypeHelper {

    private final JsonXmlDiff diff = new JsonXmlDiff(ContentType.XML);
    private final XmlPathConfig config = XmlPathConfig.xmlPathConfig().defaultObjectDeserializer(new XmlPathObjectDeserializer() {
        @Override
        public <T> T deserialize(ObjectDeserializationContext ctx) {
            return (T) xml(ctx.getDataToDeserialize().asString());
        }
    });

    @Override
    public ContentType contentType() {
        return ContentType.XML;
    }

    @Override
    public void assertContent(String expected, String actual, MatchMode matchMode) {
        diff.assertContent(expected, actual, matchMode);
    }

    @Override
    public void assertContent(
            String fragment,
            String expected,
            ExtractableResponse<Response> response,
            MatchMode mode
    ) {
        assertContent(expected, response.xmlPath(config).getObject(fragment, XmlObject.class).toString(), mode);
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
        MatcherAssert.assertThat(content, matchesXsd(expectedSchema));
    }

}