/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.core.util;


import io.cucumber.tagexpressions.Expression;
import io.cucumber.tagexpressions.TagExpressionParser;

import java.util.ArrayList;
import java.util.Collection;


public class TagFilter {

    private final Expression tagParsedExpression;

    public TagFilter(String tagExpression) {
        TagExpressionParser tagExpressionParser = new TagExpressionParser();
        this.tagParsedExpression =
                tagExpressionParser.parse(tagExpression.replace("@", "").toLowerCase());
    }


    public boolean filter(Collection<String> tags) {
        return tagParsedExpression.evaluate(new ArrayList<>(tags));
    }

}