package iti.kukumo.rest;

public class AttachedFile {

    private String name;
    private String mimeType;
    private String content;

    public AttachedFile(String name, String mimeType, String content) {
        this.name = name;
        this.mimeType = mimeType;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
