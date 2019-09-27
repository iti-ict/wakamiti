package iti.kukumo.util;

import java.util.ArrayList;

import io.cucumber.tagexpressions.Expression;
import io.cucumber.tagexpressions.TagExpressionParser;
import iti.kukumo.api.plan.PlanNode;

public class TagFilter {


    private Expression tagParsedExpression;

    public TagFilter(String tagExpression) {
        TagExpressionParser tagExpressionParser = new TagExpressionParser();
        this.tagParsedExpression = tagExpressionParser.parse(tagExpression);
    }


    public boolean filter (PlanNode node) {
        return tagParsedExpression.evaluate(new ArrayList<>(node.tags()));
    }


}
