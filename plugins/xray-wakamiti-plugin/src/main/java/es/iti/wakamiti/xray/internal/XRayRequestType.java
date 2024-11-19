package es.iti.wakamiti.xray.internal;

public enum XRayRequestType {
    QUERY("query"),
    MUTATION("mutation");


    private String name;

    XRayRequestType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
