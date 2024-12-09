package es.iti.wakamiti.xray.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestRun {

    private String id;
    private TestStatus status;
    private TestCase test;

    public TestRun() {
    }

    public String getId() {
        return id;
    }

    public TestRun setId(String id) {
        this.id = id;
        return this;
    }

    public TestStatus getStatus() {
        return status;
    }

    public TestRun setStatus(TestStatus status) {
        this.status = status;
        return this;
    }

    public TestCase getTest() {
        return test;
    }

    public TestRun setTest(TestCase test) {
        this.test = test;
        return this;
    }
}
