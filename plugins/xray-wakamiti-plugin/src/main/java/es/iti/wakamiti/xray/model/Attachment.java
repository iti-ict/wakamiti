package es.iti.wakamiti.xray.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Attachment {

    @JsonProperty
    private String file;

    public Attachment file(String fileName) {
        this.file = fileName;
        return this;
    }

    public String file() {
        return file;
    }

}
