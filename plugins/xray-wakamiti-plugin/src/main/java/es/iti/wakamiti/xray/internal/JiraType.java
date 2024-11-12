package es.iti.wakamiti.xray.internal;

public enum JiraType {
    TEST("Test"),
    TEST_PLAN("Test Plan");


    private String name;

    JiraType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
