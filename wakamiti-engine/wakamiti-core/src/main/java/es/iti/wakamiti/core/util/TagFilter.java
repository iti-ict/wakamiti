/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.util;


import io.cucumber.tagexpressions.Expression;
import io.cucumber.tagexpressions.TagExpressionParser;

import java.util.Collection;
import java.util.stream.Collectors;


/**
 * The TagFilter class provides a mechanism to filter a collection
 * of tags based on a tag expression. It uses the Cucumber tag expressions
 * library to parse and evaluate tag expressions.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 * @author María Galbis Calomarde - mgalbis@iti.es
 */
public class TagFilter {

    private final Expression tagParsedExpression;

    /**
     * Constructs a TagFilter with the specified tag expression.
     *
     * @param tagExpression The tag expression to be parsed and used for filtering.
     */
    public TagFilter(String tagExpression) {
        TagExpressionParser tagExpressionParser = new TagExpressionParser();
        this.tagParsedExpression = tagExpressionParser.parse(
                tagExpression.replace("@", "").toLowerCase());
    }

    /**
     * Filters a collection of tags based on the previously specified
     * tag expression.
     *
     * <p>The method evaluates whether the given collection of tags
     * satisfies the tag expression.
     *
     * @param tags The collection of tags to be filtered.
     * @return {@code true} if the collection of tags satisfies the expression, {@code false} otherwise.
     */
    public boolean filter(Collection<String> tags) {
        return tagParsedExpression.evaluate(
                tags.stream()
                        .map(String::toLowerCase)
                        .collect(Collectors.toList())
        );
    }

}