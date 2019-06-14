package iti.kukumo.api.plan;

import java.util.function.Function;

public class Document {

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

    public Document copy() {
        return new Document(content,contentType);
    }

    public Document copy(Function<String,String> transformer) {
        return new Document(transformer.apply(content),contentType);
    }

}
