package es.iti.wakamiti.xray;

public class Evidence {
    private String fileName;
    private String mimeType;
    private String content;

    public Evidence(String fileName, String mimeType, String content) {
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.content = content;
    }

    // Getters and setters
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

}