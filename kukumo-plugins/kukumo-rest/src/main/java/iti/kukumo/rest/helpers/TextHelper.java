/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.rest.helpers;


import io.restassured.http.ContentType;
import iti.commons.jext.Extension;
import iti.kukumo.rest.ContentTypeHelper;
import iti.kukumo.rest.MatchMode;
import org.junit.ComparisonFailure;


@Extension(provider = "iti.kukumo", name = "rest-text-helper", extensionPoint = "iti.kukumo.rest.ContentTypeHelper")
public class TextHelper implements ContentTypeHelper {


    @Override
    public ContentType contentType() {
        return ContentType.TEXT;
    }


    @Override
    public void assertContent(String expected, String actual, MatchMode matchMode) {
        switch (matchMode) {
            case STRICT:
            case STRICT_ANY_ORDER:
                if (!expected.trim().equals(actual.trim())) {
                    throw new ComparisonFailure("Text differences", expected, actual);
                }
                break;
            case LOOSE:
                if (!expected.trim().contains(actual.trim())) {
                    throw new ComparisonFailure("Text differences", expected, actual);
                }
        }
    }

}