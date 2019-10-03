package iti.kukumo.core.plan;

import iti.kukumo.api.plan.PlanNodeData;

import java.util.function.UnaryOperator;

public class Document implements PlanNodeData {

    private final String content;
    private final String contentType;

    public Document (String content) {
        this.content = content;
        this.contentType = null;
    }
    public Document (String content, String contentType) {
        this.content = content;
        this.contentType = contentType;
    }


    public String getContent() {
        return content;
    }

    public String getContentType() {
        return contentType;
    }

    @Override
    public PlanNodeData copy() {
        return new Document(content,contentType);
    }

    @Override
    public PlanNodeData copyReplacingVariables(UnaryOperator<String> replacer) {
        return new Document(replacer.apply(content),contentType);
    }

}
