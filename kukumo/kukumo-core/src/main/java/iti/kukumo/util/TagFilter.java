/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.util;


import java.util.ArrayList;
import java.util.Collection;

import io.cucumber.tagexpressions.Expression;
import io.cucumber.tagexpressions.TagExpressionParser;


public class TagFilter {

    private final Expression tagParsedExpression;

    public TagFilter(String tagExpression) {
        TagExpressionParser tagExpressionParser = new TagExpressionParser();
        this.tagParsedExpression = tagExpressionParser.parse(tagExpression);
    }


    public boolean filter(Collection<String> tags) {
        return tagParsedExpression.evaluate(new ArrayList<>(tags));
    }

}
