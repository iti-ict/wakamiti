/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.wakamiti.rest.helpers;


import iti.wakamiti.api.datatypes.Assertion;


import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import iti.commons.jext.Extension;
import iti.wakamiti.api.util.MatcherAssertion;
import iti.wakamiti.rest.ContentTypeHelper;
import iti.wakamiti.rest.MatchMode;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

@Extension(provider = "iti.wakamiti", name = "rest-json-helper", extensionPoint = "iti.wakamiti.rest.ContentTypeHelper")
public class JSONHelper implements ContentTypeHelper {


    private final JsonXmlDiff diff = new JsonXmlDiff(ContentType.JSON);

    @Override
    public ContentType contentType() {
        return ContentType.JSON;
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
        JSONObject jsonSchema = new JSONObject(new JSONTokener(expectedSchema));
        JSONObject jsonSubject = new JSONObject(new JSONTokener(content));
        Schema schemaValidator = SchemaLoader.load(jsonSchema);
        try {
            schemaValidator.validate(jsonSubject);
        } catch (ValidationException e) {
            throw new AssertionError(e.getMessage());
        }
    }

}