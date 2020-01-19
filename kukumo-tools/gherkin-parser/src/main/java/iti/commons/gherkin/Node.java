package iti.commons.gherkin;

public abstract class Node {

    protected final String type = getClass().getSimpleName();
    protected final Location location;

    protected Node(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }


}
