package es.iti.wakamiti.xray.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestStatus {
    String name;

    public TestStatus() {
        // Empty constructor
    }

    public String getName() {
        return name;
    }

    public TestStatus setName(String name) {
        this.name = name;
        return this;
    }
}
