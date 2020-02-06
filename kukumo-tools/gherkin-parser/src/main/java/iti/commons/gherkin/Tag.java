package iti.commons.gherkin;

public class Tag extends Node {
    private final String name;

    public Tag(Location location, String name) {
        super(location);
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
