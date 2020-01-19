package iti.commons.gherkin;

import java.util.List;

public abstract class CommentedNode extends Node {

    protected final List<Comment> comments;

    protected CommentedNode(Location location, List<Comment> comments) {
        super(location);
        this.comments = comments;
    }

    public Location getLocation() {
        return location;
    }

    public List<Comment> getComments() {
        return comments;
    }

}
