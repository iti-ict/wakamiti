package iti.kukumo.gherkin.parser;

import java.util.Collections;
import java.util.List;

import gherkin.ast.Comment;
import gherkin.ast.Location;
import gherkin.ast.Node;
import gherkin.ast.Step;

public class CommentedStep extends Step implements CommentedNode  {

    private final List<Comment> comments;

    public CommentedStep(Location location, String keyword, String text, Node argument, List<Comment> comments) {
        super(location, keyword, text, argument);
        this.comments = comments == null ? Collections.emptyList() : comments;
    }

    public List<Comment> getComments() {
        return this.comments;
    }
}
