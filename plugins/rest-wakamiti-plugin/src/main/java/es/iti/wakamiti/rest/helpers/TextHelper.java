/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.rest.helpers;


import org.junit.ComparisonFailure;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.rest.ContentTypeHelper;
import es.iti.wakamiti.rest.MatchMode;
import io.restassured.http.ContentType;


@Extension(
        provider = "es.iti.wakamiti",
        name = "rest-text-helper",
        version = "2.6",
        extensionPoint = "es.iti.wakamiti.rest.ContentTypeHelper"
)
public class TextHelper implements ContentTypeHelper {

    @Override
    public ContentType contentType() {
        return ContentType.TEXT;
    }

    @Override
    public void assertContent(
            String expected,
            String actual,
            MatchMode matchMode
    ) {
        switch (matchMode) {
            case STRICT:
            case STRICT_ANY_ORDER:
                if (!actual.trim().equals(expected.trim())) {
                    throw new ComparisonFailure("Text differences", expected, actual);
                }
                break;
            case LOOSE:
                if (!actual.trim().contains(expected.trim())) {
                    throw new ComparisonFailure("Text differences", expected, actual);
                }
        }
    }

}
